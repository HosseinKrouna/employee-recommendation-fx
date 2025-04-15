package com.krouna.empfehlungsapp_javafx.ui.cells;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class StatusComboBoxTableCell extends TableCell<RecommendationDTO, String> {

    private ComboBox<String> comboBox;
    // Optionen als statische Konstante, falls sie sich nie ändern
    private static final List<String> STATUS_OPTIONS = List.of("Eingereicht", "Im Prozess", "Abgesagt", "Eingestellt", "Zurückgezogen");
    private final BiConsumer<RecommendationDTO, String> updateAction;
    private static final String FINAL_STATUS_ZURUECKGEZOGEN = "Zurückgezogen";


    public StatusComboBoxTableCell(BiConsumer<RecommendationDTO, String> updateAction) {
        this.updateAction = updateAction;
//        // ComboBox wird erst bei Bedarf erstellt
//        if (FINAL_STATUS_ZURUECKGEZOGEN) {
//            setCursor(Cursor.HAND);
//            Tooltip.install(this, new Tooltip("Status ändern (Doppelklick)"));
//        }
    }

    // --- Wichtig: Auch startEdit anpassen! ---
    @Override
    public void startEdit() {
        // Zusätzliche Prüfung: Nicht editieren, wenn Status "Zurückgezogen" ist
        // oder wenn Zelle/Tabelle/Spalte nicht bearbeitbar ist
        if (FINAL_STATUS_ZURUECKGEZOGEN.equals(getItem()) ||
                isEmpty() ||
                !isEditable() ||
                !getTableView().isEditable() ||
                !getTableColumn().isEditable()) {
            // Verhindere den Start des Editierens für "Zurückgezogen"
            // oder wenn die Zelle generell nicht editierbar ist.
            System.out.println("Bearbeiten verhindert für Item: " + getItem() + " (isEditable: " + isEditable() + ")");
            return; // Breche startEdit ab
        }

        // Fahre nur fort, wenn Bearbeitung erlaubt ist
        super.startEdit();

        if (comboBox == null) {
            createComboBox(); // Erstelle ComboBox bei Bedarf
        }

        comboBox.getSelectionModel().select(getItem()); // Setze aktuellen Wert
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(comboBox);
        Platform.runLater(() -> comboBox.requestFocus()); // Fokus setzen
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        // Wechsle zurück zur Textdarstellung
        setText(getItem());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty); // Wichtig: Immer super aufrufen!

        // Tooltip-Instanz zwischenspeichern, um sie bei Bedarf zu entfernen
        Tooltip currentTooltip = getTooltip();

        if (empty || item == null) {
            // Zelle ist leer: Alles zurücksetzen
            setText(null);
            setGraphic(null);
            setCursor(Cursor.DEFAULT);
            // Tooltip entfernen, falls vorhanden
            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
            }
        } else {
            // Zelle hat Inhalt (einen Status-String 'item')

            // 1. Prüfen, ob der Status prinzipiell bearbeitbar ist
            boolean isEditableStatus = !FINAL_STATUS_ZURUECKGEZOGEN.equals(item);

            // 2. Prüfen, ob die Zelle/Spalte/Tabelle überhaupt bearbeitbar ist
            boolean isCellConfiguredEditable = isEditable() && getTableView().isEditable() && getTableColumn().isEditable();

            // 3. Entscheiden, wie die Zelle angezeigt wird

            if (isEditing() && isEditableStatus && isCellConfiguredEditable) {
                // ----- Im Bearbeitungsmodus (und Status erlaubt Bearbeitung) -----
                // ComboBox wird in startEdit() gesetzt, hier nur sicherstellen,
                // dass sie angezeigt wird und den korrekten Wert hat.
                if (comboBox != null) {
                    comboBox.getSelectionModel().select(item);
                }
                setText(null);
                setGraphic(comboBox); // Zeige die ComboBox
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setCursor(Cursor.DEFAULT); // Standard-Cursor während der Bearbeitung
                // Tooltip während der Bearbeitung entfernen
                if (currentTooltip != null) {
                    Tooltip.uninstall(this, currentTooltip);
                }

            } else {
                // ----- Im Anzeigemodus (oder Status erlaubt keine Bearbeitung) -----
                setText(item); // Zeige den Status-Text
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);

                // Setze Cursor und Tooltip NUR, wenn der Status UND die Zelle bearbeitbar sind
                if (isEditableStatus && isCellConfiguredEditable) {
                    setCursor(Cursor.HAND); // Hand-Cursor als Hinweis
                    // Installiere den Tooltip nur, wenn er noch nicht existiert
                    // um mehrfache Installationen zu vermeiden
                    if (currentTooltip == null) {
                        Tooltip tt = new Tooltip("Status ändern (Doppelklick oder Enter)");
                        // Optional: Verzögerung hinzufügen
                        // tt.setShowDelay(Duration.millis(200));
                        Tooltip.install(this, tt);
                    }
                } else {
                    // Status ist nicht bearbeitbar ("Zurückgezogen") oder Zelle ist nicht editierbar
                    setCursor(Cursor.DEFAULT); // Standard-Cursor
                    // Entferne den Tooltip, falls er existiert
                    if (currentTooltip != null) {
                        Tooltip.uninstall(this, currentTooltip);
                    }
                }
            }
        }
    }


    private void createComboBox() {
        // Filter die Optionen: "Zurückgezogen" soll nicht auswählbar sein
        List<String> selectableOptions = STATUS_OPTIONS.stream()
                .filter(option -> !FINAL_STATUS_ZURUECKGEZOGEN.equals(option))
                .collect(Collectors.toList());

        comboBox = new ComboBox<>(FXCollections.observableArrayList(selectableOptions));
        // comboBox = new ComboBox<>(FXCollections.observableArrayList(STATUS_OPTIONS)); // Alte Version
        comboBox.setPrefWidth(150);
        comboBox.setMaxWidth(Double.MAX_VALUE);

        // Listener für Wertänderung - unverändert
        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldStatus, newStatus) -> {
            if (isEditing() && newStatus != null && !newStatus.equals(oldStatus)) {
                // Prüfen, ob die Aktion aufgerufen werden soll (sicherheitshalber)
                if (!FINAL_STATUS_ZURUECKGEZOGEN.equals(newStatus)) {
                    RecommendationDTO currentDto = getTableView().getItems().get(getIndex());
                    if (updateAction != null) {
                        // Rufe die Update-Aktion auf, die im HRController definiert ist
                        updateAction.accept(currentDto, newStatus);
                    } else {
                        // Sollte nicht passieren, wenn im Controller korrekt initialisiert
                        System.err.println("UpdateAction ist null in StatusComboBoxTableCell!");
                        cancelEdit();
                    }
                } else {
                    // Sollte durch gefilterte Liste nicht passieren, aber zur Sicherheit
                    System.out.println("Versuch, 'Zurückgezogen' auszuwählen, ignoriert.");
                    // Hier nicht cancelEdit aufrufen, da der User vielleicht was anderes wählen will
                }
            }
        });


        // Event Handling für Commit/Cancel - unverändert
        comboBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Nur committen, wenn der Wert gültig ist (nicht "Zurückgezogen")
                String selectedValue = comboBox.getValue();
                if (selectedValue != null && !FINAL_STATUS_ZURUECKGEZOGEN.equals(selectedValue)) {
                    commitEdit(selectedValue);
                } else {
                    // Wenn "Zurückgezogen" irgendwie ausgewählt wurde oder nichts ausgewählt ist, abbrechen
                    cancelEdit();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                event.consume();
            }
        });

        comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && isEditing()) {
                String selectedValue = comboBox.getValue();
                if (selectedValue != null && !FINAL_STATUS_ZURUECKGEZOGEN.equals(selectedValue)) {
                    commitEdit(selectedValue);
                } else {
                    cancelEdit();
                }
            }
        });

    }

    // --- Commit Edit überschreiben  ---
    @Override
    public void commitEdit(String newValue) {
        // Zusätzliche Prüfung: Commit nicht zulassen, wenn der neue Wert "Zurückgezogen" ist
        if (FINAL_STATUS_ZURUECKGEZOGEN.equals(newValue)) {
            System.out.println("Commit für Status 'Zurückgezogen' verhindert.");
            cancelEdit(); // Breche stattdessen ab
            return;
        }

        if (!isEditing()) {
            return;
        }
        super.commitEdit(newValue);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}