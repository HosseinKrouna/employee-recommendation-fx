package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

// Imports für URL-Validierung und Alert
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class FormValidator {
    // Behalte deine Listen für Pflichtfelder
    private final List<TextInputControl> requiredTextFields = new ArrayList<>();
    private final List<ComboBox<?>> requiredComboBoxes = new ArrayList<>();

    // Neue Listen zum Sammeln ALLER Fehler bei der finalen Prüfung
    private final List<String> errorMessages = new ArrayList<>();
    private final List<Control> errorControls = new ArrayList<>(); // Speichert Controls mit Fehlern

    // --- Bestehende Methoden zum Hinzufügen von Pflichtfeldern ---
    public void addRequiredTextField(TextInputControl field) {
        requiredTextFields.add(field);
        // Optional: Listener hinzufügen, um Fehlerstil zu entfernen, wenn getippt wird
        field.textProperty().addListener((obs, oldVal, newVal) -> removeErrorStyleOnChange(field));
    }

    public void addRequiredComboBox(ComboBox<?> comboBox) {
        requiredComboBoxes.add(comboBox);
        // Optional: Listener hinzufügen
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> removeErrorStyleOnChange(comboBox));
    }

    // --- Hilfsmethode zum Entfernen des Fehlerstils bei Eingabe ---
    private void removeErrorStyleOnChange(Control control) {
        // Nur entfernen, wenn es aktuell rot markiert ist
        if (control.getStyle() != null && control.getStyle().contains("-fx-border-color: red;")) {
            control.setStyle(null); // Oder Standardstil wiederherstellen
        }
    }


    /**
     * Validiert das gesamte Formular bei der Einreichung.
     * Führt alle konfigurierten Prüfungen durch.
     *
     * @param scrollPane Das ScrollPane zum Navigieren.
     * @param businessLinkField Das Textfeld für den Business-Link (für die spezielle Prüfung).
     * @param businessLinkToggle Die CheckBox für den Business-Link (für die spezielle Prüfung).
     * @param emailField Das E-Mail-Feld (für die spezielle Prüfung). // Beispiel: Füge hier alle Felder hinzu, die spezielle Prüfungen benötigen
     * @return true, wenn das Formular gültig ist, sonst false.
     */
    public boolean validateForm(ScrollPane scrollPane, TextField businessLinkField, CheckBox businessLinkToggle, TextField emailField) {
        // 1. Fehlerlisten zurücksetzen
        errorMessages.clear();
        errorControls.clear();
        boolean isFormValid = true;

        // 2. Alle Validierungen durchführen und Ergebnisse sammeln
        isFormValid &= validateRequiredFieldsInternal(); // Prüft Pflichtfelder

        // --- Hier weitere spezifische Validierungen hinzufügen ---
        // Beispiel: E-Mail-Format bei Einreichung prüfen (zusätzlich zum Live-Feedback)
        isFormValid &= validateEmailFormatInternal(emailField, "E-Mail");

        // Beispiel: Business-Link prüfen
        isFormValid &= validateOptionalBusinessLinkInternal(businessLinkField, businessLinkToggle, "Business-Profil-Link");

        // Beispiel: Numerische Bereiche prüfen (falls setupNumericField nur Filterung macht)
        // isFormValid &= validateNumericRangeInternal(someNumericField, 0, 100, "Prozentwert");

        // 3. Ergebnis verarbeiten
        if (!isFormValid) {
            // Zeige gesammelte Fehlermeldungen an
            showErrorSummaryDialog();

            // Scrolle zum ersten fehlerhaften Control
            if (!errorControls.isEmpty()) {
                Control firstInvalid = errorControls.get(0);
                Platform.runLater(() -> {
                    scrollToNode(firstInvalid, scrollPane); // Dein bestehender Scroll-Code
                    firstInvalid.requestFocus();
                });
            }
            return false; // Formular ungültig
        }

        return true; // Formular gültig
    }

    // --- Interne Validierungsmethoden (werden von validateForm aufgerufen) ---

    /**
     * Prüft alle als erforderlich markierten Felder.
     * Fügt Fehler zu errorMessages und errorControls hinzu.
     * @return true, wenn alle Pflichtfelder ausgefüllt sind, sonst false.
     */
    private boolean validateRequiredFieldsInternal() {
        boolean allValid = true;
        for (TextInputControl field : requiredTextFields) {
            removeErrorStyle(field); // Stil zurücksetzen vor Prüfung
            if (field.getText().isBlank()) {
                markInvalid(field); // Markieren und zur Fehlerliste hinzufügen
                // Sinnvollen Namen für Fehlermeldung finden (z.B. aus PromptText)
                String fieldName = getFieldName(field, "Eingabefeld");
                errorMessages.add(fieldName + " darf nicht leer sein.");
                allValid = false;
            }
        }

        for (ComboBox<?> comboBox : requiredComboBoxes) {
            removeErrorStyle(comboBox); // Stil zurücksetzen vor Prüfung
            if (comboBox.getValue() == null) {
                markInvalid(comboBox);
                String fieldName = getFieldName(comboBox, "Auswahl");
                errorMessages.add(fieldName + " muss ausgewählt werden.");
                allValid = false;
            }
        }
        return allValid;
    }

    /**
     * Validiert das E-Mail-Format bei der Einreichung.
     * @param emailField Das zu prüfende Feld.
     * @param baseFieldName Name für die Fehlermeldung.
     * @return true, wenn das Format gültig ist oder das Feld leer ist, sonst false.
     */
    private boolean validateEmailFormatInternal(TextField emailField, String baseFieldName) {
        removeErrorStyle(emailField); // Reset style
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,7}$"); // Etwas strengeres Pattern
        String email = emailField.getText().trim();

        if (!email.isEmpty() && !emailPattern.matcher(email).matches()) {
            markInvalid(emailField);
            errorMessages.add(baseFieldName + ": Ungültiges E-Mail-Format.");
            return false;
        }
        return true;
    }


    /**
     * Validiert ein optionales Textfeld für einen Business-Link (URL).
     * Die Validierung erfolgt nur, wenn die zugehörige CheckBox aktiviert ist
     * und das Feld Text enthält. Prüft auf gültiges URL-Format und optional Domains.
     *
     * @param linkField Das Textfeld für den Link.
     * @param toggleCheckBox Die CheckBox, die die Eingabe aktiviert.
     * @param baseFieldName Der Name des Feldes für Fehlermeldungen.
     * @return true, wenn der Link gültig ist oder nicht validiert werden muss, sonst false.
     */
    public boolean validateOptionalBusinessLinkInternal(TextField linkField, CheckBox toggleCheckBox, String baseFieldName) {
        removeErrorStyle(linkField); // Reset style first
        boolean isValid = true;

        if (toggleCheckBox.isSelected() && linkField.isVisible()) {
            String link = linkField.getText().trim();
            if (!link.isEmpty()) {
                try {
                    // Strikte Prüfung mit Java URL (erfordert meist http/https)
                    URL url = new URL(link);

                    // Zusätzliche Domain-Prüfung (optional)
//                    String host = url.getHost().toLowerCase();
//                    if (!(host.contains("linkedin.com") || host.contains("xing.com"))) {
//                        markInvalid(linkField);
//                        errorMessages.add(baseFieldName + ": Nur LinkedIn/Xing-Links erwartet.");
//                        isValid = false;
//                    }
                    // Wenn du KEINE Domain-Prüfung willst, kommentiere den if-Block oben aus.

                } catch (MalformedURLException e) {
                    markInvalid(linkField);
                    errorMessages.add(baseFieldName + ": Ungültiges URL-Format (z.B. https:// fehlt?).");
                    isValid = false;
                }
            } else {
                // Feld ist leer, obwohl Checkbox aktiv ist. Ist das ein Fehler?
                // Wenn ja:
                // markInvalid(linkField);
                // errorMessages.add(baseFieldName + " muss angegeben werden, wenn die Option aktiv ist.");
                // isValid = false;
            }
        }
        // Wenn Checkbox nicht aktiv oder Feld nicht sichtbar -> Gültig (da optional)
        return isValid;
    }


    // --- Hilfsmethoden für Styling und Fehleranzeige ---

    /**
     * Markiert ein Control als ungültig (roter Rand) und fügt es zur Fehlerliste hinzu.
     */
    private void markInvalid(Control control) {
        control.setStyle("-fx-border-color: red; -fx-border-width: 1px;"); // Etwas dezenter
        if (!errorControls.contains(control)) { // Nur einmal hinzufügen
            errorControls.add(control);
        }
    }

    /**
     * Entfernt den Fehlerstil von einem Control.
     */
    public void removeErrorStyle(Control control) {
        control.setStyle(null); // Standardstil wiederherstellen
    }

    /**
     * Zeigt einen Dialog mit allen gesammelten Fehlermeldungen an.
     */
    private void showErrorSummaryDialog() {
        if (errorMessages.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validierungsfehler");
        alert.setHeaderText("Bitte korrigieren Sie die folgenden Eingaben:");

        // Verhindert zu lange Nachrichten im Dialog
        String content = String.join("\n", errorMessages);
        int maxLength = 800; // Maximale Zeichenlänge
        if (content.length() > maxLength) {
            content = content.substring(0, maxLength) + "\n...";
        }
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // Sorgt für passende Größe
        alert.showAndWait();
    }

    /**
     * Versucht, einen sinnvollen Namen für ein Feld zu finden (z.B. PromptText).
     */
    private String getFieldName(Control control, String defaultName) {
        if (control instanceof Labeled labeled && labeled.getText() != null && !labeled.getText().isEmpty()) {
            return labeled.getText(); // z.B. Text einer Checkbox oder eines Buttons
        }
        if (control instanceof TextInputControl tic && tic.getPromptText() != null && !tic.getPromptText().isEmpty()) {
            return tic.getPromptText(); // PromptText von TextField, TextArea
        }
        if (control instanceof ComboBoxBase<?> cbb && cbb.getPromptText() != null && !cbb.getPromptText().isEmpty()) {
            return cbb.getPromptText(); // PromptText von ComboBox
        }
        // Finde das Label, das mit diesem Control verbunden ist (komplexer, hier vereinfacht)
        // Node label = control.lookup(".label"); // Einfacher Ansatz, oft nicht zuverlässig
        // if(label instanceof Label) return ((Label) label).getText();

        return defaultName; // Fallback
    }


    // --- Bestehende Methoden (scrollToNode, setupNumericField, etc.) ---
    // Behalte deine Methoden scrollToNode, setupNumericField, setupDecimalField
    // setupEmailField kann bleiben für Live-Feedback, aber die *finale* Prüfung
    // erfolgt jetzt in validateEmailFormatInternal.

    private void scrollToNode(Node node, ScrollPane scrollPane) {
        if (scrollPane == null || node == null) return;

        Node content = scrollPane.getContent();
        if (content != null) {
            // Versuch, die Position relativ zum Scrollpane-Inhalt zu bekommen
            Bounds nodeBoundsInParent = node.localToParent(node.getBoundsInLocal());
            double nodeYInContent = nodeBoundsInParent.getMinY();

            // Finde die Gesamt-Höhe des Inhalts
            double contentHeight = content.getBoundsInLocal().getHeight();
            // Finde die Höhe des sichtbaren Bereichs
            double viewportHeight = scrollPane.getViewportBounds().getHeight();

            // Berechne den VValue (Anteil, um wie viel gescrollt werden muss)
            // Ziel: Oberkante des Nodes soll oben im Viewport sichtbar sein
            double vvalue = nodeYInContent / Math.max(1, contentHeight - viewportHeight); // Division durch 0 vermeiden

            // Begrenze den Wert zwischen 0 und 1
            scrollPane.setVvalue(Math.min(1, Math.max(0, vvalue)));
        }
    }

    public void setupNumericField(TextField field, int min, int max, String fieldName) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }

            if (Pattern.matches("\\d*", newText)) {
                // Nur filtern, keine Bereichsprüfung hier (das kann validateForm machen)
                return change;
                /* Optional: Bereichsprüfung direkt beim Tippen
                try {
                    int value = Integer.parseInt(newText);
                    if (value >= min && value <= max) {
                        return change;
                    }
                } catch (NumberFormatException e) { }
                */
            }
            return null; // Ungültige Eingabe verhindern
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    public void setupDecimalField(TextField field, double min, double max, String fieldName) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String currentText = change.getControlText();
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            // Erlaube nur Ziffern, einen Punkt oder ein Komma
            if (!Pattern.matches("[\\d.,]*", newText)) {
                return null;
            }

            // Ersetze Komma durch Punkt für die Prüfung
            String parsableText = newText.replace(',', '.');

            // Erlaube nur einen Dezimaltrenner
            if (parsableText.indexOf('.') != parsableText.lastIndexOf('.')) {
                return null;
            }

            // Versuche zu parsen, um ungültige Formate wie ".." oder "." am Anfang zu vermeiden
            try {
                if (parsableText.equals(".")) { // Sonderfall: Nur Punkt ist noch keine Zahl
                    // Erlaube es, wenn der aktuelle Text leer ist
                    return currentText.isEmpty() ? change : null;
                } else if (!parsableText.isEmpty()){
                    Double.parseDouble(parsableText); // Teste, ob es eine gültige Zahl ist
                }
                // Bereichsprüfung kann optional hier oder besser in validateForm erfolgen
                return change;

            } catch (NumberFormatException e) {
                return null; // Ungültiges Zahlenformat
            }
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }


    // setupEmailField kann für Live-Feedback bleiben
    public void setupEmailField(TextField emailField, Label feedbackLabel) {
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,7}$"); // Das strengere Pattern verwenden

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            String email = newValue.trim();
            if (email.isEmpty()) {
                feedbackLabel.setText(""); // Keine Meldung bei leerem Feld
                removeErrorStyle(emailField); // Fehlerstil entfernen
                return;
            }

            boolean isValid = emailPattern.matcher(email).matches();

            if (!isValid) {
                feedbackLabel.setText("Ungültiges E-Mail-Format.");
                feedbackLabel.setStyle("-fx-text-fill: red;");
                // Optional: roten Rahmen setzen für sofortiges Feedback
                // markInvalid(emailField); // Vorsicht: Könnte verwirrend sein, wenn es optional ist
            } else {
                feedbackLabel.setText("Format gültig.");
                feedbackLabel.setStyle("-fx-text-fill: green;");
                removeErrorStyle(emailField); // Fehlerstil entfernen
            }
        });
    }
}