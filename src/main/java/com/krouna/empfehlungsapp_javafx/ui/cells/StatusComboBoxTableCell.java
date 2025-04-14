package com.krouna.empfehlungsapp_javafx.ui.cells;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.function.BiConsumer;

public class StatusComboBoxTableCell extends TableCell<RecommendationDTO, String> {

    private ComboBox<String> comboBox;
    // Optionen als statische Konstante, falls sie sich nie ändern
    private static final List<String> STATUS_OPTIONS = List.of("Eingereicht", "Im Prozess", "Abgesagt", "Eingestellt");
    private final BiConsumer<RecommendationDTO, String> updateAction;

    public StatusComboBoxTableCell(BiConsumer<RecommendationDTO, String> updateAction) {
        this.updateAction = updateAction;
        // ComboBox wird erst bei Bedarf erstellt
    }

    @Override
    public void startEdit() {
        if (isEmpty() || !isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
            return; // Nur editieren, wenn möglich und nicht leer
        }
        super.startEdit();

        // ComboBox erstellen nur, wenn sie noch nicht existiert
        if (comboBox == null) {
            createComboBox();
        }

        // aktuellen Wert in die ComboBox setzen
        comboBox.getSelectionModel().select(getItem());

        //  Zur grafischen Darstellung (ComboBox) wechseln
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(comboBox);
        // Fokus setzen, damit KeyEvents funktionieren
        Platform.runLater(() -> comboBox.requestFocus()); // runLater für besseres Timing
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
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Im Anzeige-Modus oder wenn Edit abgebrochen wurde
            if (!isEditing()) {
                setText(item);
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            } else {
                // Im Editier-Modus (wird von startEdit/cancelEdit gesteuert)
                // Wert in ComboBox sicherheitshalber neu setzen
                if (comboBox != null) {
                    comboBox.getSelectionModel().select(item);
                }
                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(comboBox);
            }
        }
    }


    private void createComboBox() {
        comboBox = new ComboBox<>(FXCollections.observableArrayList(STATUS_OPTIONS));
        comboBox.setPrefWidth(150); // Bevorzugte Breite
        comboBox.setMaxWidth(Double.MAX_VALUE);

        // --- Listener für Wertänderung ---
        // Dieser Listener ruft NUR die updateAction auf. Commit erfolgt separat.
        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldStatus, newStatus) -> {
            // Nur wenn Edit aktiv ist, Wert neu und ungleich dem alten ist
            if (isEditing() && newStatus != null && !newStatus.equals(oldStatus)) { // Vergleiche mit oldStatus!
                RecommendationDTO currentDto = getTableView().getItems().get(getIndex());
                if (updateAction != null) {
                    // Backend-Logik aufrufen
                    updateAction.accept(currentDto, newStatus);
                    // WICHTIG: NICHT commitEdit hier aufrufen! Das macht der User durch Enter/Escape etc.
                } else {
                    cancelEdit(); // Breche ab, wenn keine Aktion definiert
                }
            }
        });

        // --- Event Handling für Commit/Cancel ---
        comboBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Bei Enter: Commit mit dem aktuell ausgewählten Wert
                System.out.println("DEBUG: Enter gedrückt, commiting: " + comboBox.getValue());
                commitEdit(comboBox.getValue());
                event.consume(); // Verhindere weitere Verarbeitung des Events
            } else if (event.getCode() == KeyCode.ESCAPE) {
                // Bei Escape: Edit abbrechen
                System.out.println("DEBUG: Escape gedrückt, canceling edit");
                cancelEdit();
                event.consume();
            }
            // Andere Tasten (Pfeiltasten etc.) normal behandeln lassen
        });

        // Fokusverlust führt zum Commit (Standardverhalten, kann man lassen oder ändern)
        // Alternative: Fokusverlust führt zum Cancel, wenn Wert nicht geändert wurde
        comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && isEditing()) { // Wenn Fokus verloren geht UND User im Edit-Modus ist
                System.out.println("DEBUG: Fokus verloren, commiting: " + comboBox.getValue());
                // Commit mit dem Wert, der gerade in der ComboBox steht
                commitEdit(comboBox.getValue());
            }
        });
    }

    // --- Commit Edit überschreiben  ---
    @Override
    public void commitEdit(String newValue) {
        System.out.println("DEBUG: commitEdit in Cell aufgerufen mit: " + newValue);
        // Nur committen, wenn User im Edit-Modus ist
        if (!isEditing()) {
            System.out.println("DEBUG: commitEdit ignoriert, nicht im Edit-Modus.");
            return;
        }
        // Wichtig:  super.commitEdit aufrufen, damit der onEditCommit der Spalte gefeuert wird!
        // Der übergebene newValue ist der Wert, der im TableView-Model landen soll.
        super.commitEdit(newValue);

        // Explizit zurück zur Textanzeige wechseln
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        // Die Aktualisierung des eigentlichen DTO-Objekts erfolgt im onEditCommit des Controllers
        // basierend auf dem Wert, der hier übergeben wird.
    }
}





















//package com.krouna.empfehlungsapp_javafx.ui.cells; // Beispielpaket
//
//import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.scene.control.ComboBox;
//import javafx.scene.control.ListCell;
//import javafx.scene.control.ListView;
//import javafx.scene.control.TableCell;
//import javafx.util.Callback;
//
//import java.util.List;
//import java.util.function.BiConsumer; // Für die Update-Aktion
//
//public class StatusComboBoxTableCell extends TableCell<RecommendationDTO, String> {
//
//    private ComboBox<String> comboBox;
//    private final List<String> statusOptions = List.of("Eingereicht", "Im Prozess", "Abgesagt", "Eingestellt");
//    // Aktion, die aufgerufen wird, wenn ein neuer Status gewählt wird: nimmt (RecommendationDTO, neuerStatus)
//    private final BiConsumer<RecommendationDTO, String> updateAction;
//
//    public StatusComboBoxTableCell(BiConsumer<RecommendationDTO, String> updateAction) {
//        this.updateAction = updateAction;
//    }
//
//    @Override
//    public void startEdit() {
//        if (!isEmpty()) {
//            super.startEdit();
//            createComboBox(); // ComboBox Objekt wird erstellt
//            setText(null);
//
//            // ---> VERSUCH MIT runLater <---
//            // Setze die Grafik nicht sofort, sondern im nächsten UI-Puls
//            final ComboBox<String> finalComboBox = comboBox; // Brauchen finale Variable für Lambda
//            Platform.runLater(() -> {
//                if (isEditing()) { // Prüfe nochmal, ob wir noch im Edit-Modus sind
//                    System.out.println("DEBUG: Versuche ComboBox via runLater zu setzen...");
//                    setGraphic(finalComboBox); // Setze die erstellte ComboBox
//                }
//            });
//            // ---> ENDE VERSUCH MIT runLater <---
//
//            // Setze den aktuellen Wert (kann wahrscheinlich hier bleiben)
//            if (comboBox != null) { // Sicherheitscheck
//                comboBox.getSelectionModel().select(getItem());
//            }
//            // comboBox.show(); // Auskommentiert lassen
//        }
//    }
//    @Override
//    public void cancelEdit() {
//        super.cancelEdit();
//        // Setze den Text zurück und entferne die ComboBox
//        setText(getItem());
//        setGraphic(null);
//    }
//
//    @Override
//    public void updateItem(String item, boolean empty) {
//        super.updateItem(item, empty);
//
//        if (empty || item == null) {
//            setText(null);
//            setGraphic(null);
//        } else {
//            if (isEditing()) {
//                // Im Editier-Modus: ComboBox anzeigen (wird in startEdit erledigt)
//                if (comboBox != null) {
//                    comboBox.getSelectionModel().select(item);
//                }
//                setText(null);
//                setGraphic(comboBox);
//            } else {
//                // Im Anzeige-Modus: Nur Text anzeigen
//                setText(item);
//                setGraphic(null);
//                // Optional: Style basierend auf Status hinzufügen
//                // getStyleClass().removeAll("status-eingereicht", "status-prozess", ...); // Alte entfernen
//                // switch(item) {
//                //    case "Eingereicht": getStyleClass().add("status-eingereicht"); break;
//                //    case "Im Prozess": getStyleClass().add("status-prozess"); break;
//                //    ...
//                // }
//            }
//        }
//    }
//
//    private void createComboBox() {
//        comboBox = new ComboBox<>(FXCollections.observableArrayList(statusOptions));
//        // ---> Logging hinzufügen <---
//        System.out.println("DEBUG createComboBox: this.getWidth() = " + this.getWidth());
//        System.out.println("DEBUG createComboBox: this.getGraphicTextGap() = " + this.getGraphicTextGap());
//        // ---> Ende Logging <---
//        comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
//        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldStatus, newStatus) -> {
//            if (newStatus != null && !newStatus.equals(getItem())) {
//                // Rufe die Update-Aktion auf, wenn sich der Wert ändert
//                // Hole das DTO der aktuellen Zeile
//                RecommendationDTO currentDto = getTableView().getItems().get(getIndex());
//                if (updateAction != null) {
//                    // Führe die Aktion aus (z.B. Backend-Call)
//                    updateAction.accept(currentDto, newStatus);
//                    // Wichtig: Commit den Edit, damit die Zelle den neuen Wert anzeigt
//                    // commitEdit(newStatus); // <-- HIER AUSKOMMENTIEREN ZUM TESTEN
//                } else {
//                    // Keine Aktion definiert, breche Edit ab
//                    cancelEdit();
//                }
//            } else {
//                // Wert nicht geändert oder null -> Edit abbrechen
//                // commitEdit(newStatus); // Oder cancel? Wenn Wert gleich bleibt, commit ok.
//                cancelEdit();
//            }
//        });
//
//        // Verhindere, dass der Edit-Modus durch Klick außerhalb beendet wird, bevor Auswahl getroffen wurde (optional)
//        /*
//         comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
//            if (!isNowFocused) {
//                // Nur committen, wenn ein gültiger Wert ausgewählt wurde
//                // commitEdit(comboBox.getSelectionModel().getSelectedItem());
//                 // Sicherer ist oft cancelEdit() bei Fokusverlust ohne Änderung
//                 cancelEdit();
//            }
//        });
//        */
//
//    }
//}