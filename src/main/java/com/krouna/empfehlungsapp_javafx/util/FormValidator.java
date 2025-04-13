package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import org.springframework.expression.ParseException;

// Imports für URL-Validierung und Alert
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class FormValidator {
    // Behalte deine Listen für Pflichtfelder
    private final List<TextInputControl> requiredTextFields = new ArrayList<>();
    private final List<ComboBox<?>> requiredComboBoxes = new ArrayList<>();

    // Listen zum Sammeln ALLER Fehler bei der finalen Prüfung
    private final List<String> errorMessages = new ArrayList<>();
    private final List<Control> errorControls = new ArrayList<>(); // Speichert Controls mit Fehlern


    // Liste und Record für numerische Bereichsprüfung
    private record NumericRangeField(TextField field, int min, int max, String fieldName, boolean required) {}
    private final List<NumericRangeField> numericRangeFields = new ArrayList<>();

    private record DecimalRangeField(TextField field, double min, double max, String fieldName, boolean required) {}
    private final List<DecimalRangeField> decimalRangeFields = new ArrayList<>();

    // Formatierer, der sowohl Punkt als auch Komma als Dezimaltrenner versteht (gebietsschema-unabhängig)
    // Wir verwenden Locale.US für Punkt als Trenner intern, erlauben aber Komma bei Eingabe
    private static final NumberFormat DECIMAL_FORMAT = DecimalFormat.getNumberInstance(Locale.US);

    // --- Methoden zum Registrieren von Validierungen ---

    public void addRequiredTextField(TextInputControl field) {
        requiredTextFields.add(field);
        field.textProperty().addListener((obs, oldVal, newVal) -> removeErrorStyleOnChange(field));
    }

    public void addRequiredComboBox(ComboBox<?> comboBox) {
        requiredComboBoxes.add(comboBox);
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> removeErrorStyleOnChange(comboBox));
    }


    public void addNumericRangeValidation(TextField field, int min, int max, String fieldName, boolean isRequired) {
        numericRangeFields.add(new NumericRangeField(field, min, max, fieldName, isRequired));
        setupNumericFieldWithLiveRangeCheck(field, min, max, fieldName, isRequired);
    }


    // --- Setup-Methoden mit Live-Validierung/Formatierung ---

    public void addDecimalRangeValidation(TextField field, double min, double max, String fieldName, boolean isRequired) {
        decimalRangeFields.add(new DecimalRangeField(field, min, max, fieldName, isRequired));
        setupDecimalFieldWithLiveRangeCheck(field, min, max, fieldName, isRequired);
    }

    public void setupNumericFieldWithLiveRangeCheck(TextField field, int min, int max, String fieldName, boolean isRequired) {
        // 1. TextFormatter (filtert ungültige Zeichen)
        UnaryOperator<TextFormatter.Change> charFilter = change -> {
            String newText = change.getControlNewText();
            // Erlaube leere Eingabe oder nur Ziffern
            if (newText.isEmpty() || Pattern.matches("\\d*", newText)) {
                return change;
            }
            return null; // Blockiere andere Zeichen
        };
        field.setTextFormatter(new TextFormatter<>(charFilter));

        // 2. Listener für LIVE Bereichsprüfung
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNumericFieldLive(field, min, max, fieldName, isRequired);
        });

        // 3. Listener für Fokusverlust
        field.focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) { // Fokus verloren
                validateNumericFieldLive(field, min, max, fieldName, isRequired);
            }
        });
    }

    public void setupDecimalFieldWithLiveRangeCheck(TextField field, double min, double max, String fieldName, boolean isRequired) {
        // 1. TextFormatter (erlaubt Ziffern, Punkt, Komma, nur ein Trenner)
        UnaryOperator<TextFormatter.Change> charFilter = change -> {
            String currentText = change.getControlText();
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            // Erlaube nur Ziffern, Punkt oder Komma
            if (!Pattern.matches("^[\\d.,]*$", newText)) return null;
            // Ersetze Komma durch Punkt für interne Prüfung der Trennzeichen
            String checkText = newText.replace(',', '.');
            // Erlaube nur EINEN Punkt
            if (checkText.indexOf('.') != checkText.lastIndexOf('.')) return null;
            // Erlaube Punkt/Komma nicht am Anfang, wenn schon Text da ist (optional)
            // if ((newText.endsWith(".") || newText.endsWith(",")) && newText.length() > 1 && !Character.isDigit(newText.charAt(newText.length()-2))) return null;

            // Erlaube die Änderung (finale Wertprüfung im Listener)
            return change;
        };
        field.setTextFormatter(new TextFormatter<>(charFilter));

        // 2. Listener für LIVE Bereichsprüfung
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDecimalFieldLive(field, min, max, fieldName, isRequired);
        });

        // 3. Listener für Fokusverlust
        field.focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) { // Fokus verloren
                validateDecimalFieldLive(field, min, max, fieldName, isRequired);
            }
        });
    }

    public void setupEmailField(TextField emailField, Label feedbackLabel) {
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,7}$");
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            String email = newValue.trim();
            // Feedback Label Logik (visuelles Feedback)
            if (email.isEmpty()) {
                feedbackLabel.setText(""); // Leeres Feld -> kein Feedback
                removeErrorStyle(emailField); // Evtl. Fehlerstil entfernen
            } else if (!emailPattern.matcher(email).matches()) {
                feedbackLabel.setText("Ungültiges E-Mail-Format.");
                feedbackLabel.setStyle("-fx-text-fill: red;");
                // Optional: Feld markieren
                // markInvalid(emailField);
            } else {
                feedbackLabel.setText("Format gültig.");
                feedbackLabel.setStyle("-fx-text-fill: green;");
                removeErrorStyle(emailField); // Fehlerstil entfernen, wenn gültig
            }
        });
    }

    // Live-Validierung für Decimal
    private void validateDecimalFieldLive(TextField field, double min, double max, String fieldName, boolean isRequired) {
        String text = field.getText().trim();
        boolean isValid = true;
        String validationMessage = null;

        if (text.isEmpty()) {
            if (isRequired) {
                isValid = false;
                validationMessage = fieldName + " darf nicht leer sein.";
            }
        } else {
            try {
                String parsableText = text.replace(',', '.');
                // Prüfe auf den Fall, dass nur ein Punkt/Komma eingegeben wurde
                if (parsableText.equals(".")) {
                    // Eine einzelne Dezimaltrennung ist keine gültige Zahl für die Bereichsprüfung
                    isValid = false;
                    validationMessage = fieldName + ": Unvollständige Zahl.";
                } else {
                    // Versuche zu parsen
                    double value = Double.parseDouble(parsableText); // Kann NumberFormatException werfen

                    if (value < min || value > max) { // Bereich prüfen
                        isValid = false;
                        validationMessage = String.format(Locale.GERMAN, "%s muss zwischen %.1f und %.1f liegen.", fieldName, min, max);
                    }
                }
            } catch (NumberFormatException e) { // Fange nur NumberFormatException
                isValid = false;
                validationMessage = fieldName + ": Ungültige Dezimalzahl.";
            }
        }

        // Stil und Tooltip setzen
        if (!isValid) {
            markInvalid(field);
            setTooltip(field, validationMessage);
        } else {
            removeErrorStyle(field);
        }
    }


    // --- Live-Validierungsmethode für Numeric ---


    private void validateNumericFieldLive(TextField field, int min, int max, String fieldName, boolean isRequired) {
        String text = field.getText().trim();
        boolean isValid = true;
        String validationMessage = null; // Für Tooltip

        if (text.isEmpty()) {
            if (isRequired) {
                isValid = false;
                validationMessage = fieldName + " darf nicht leer sein.";
            }
            // Wenn nicht required und leer -> gültig
        } else {
            try {
                int value = Integer.parseInt(text); // Parsen
                if (value < min || value > max) { // Bereich prüfen
                    isValid = false;
                    validationMessage = fieldName + " muss zwischen " + min + " und " + max + " liegen.";
                }
            } catch (NumberFormatException e) {
                // Sollte durch Formatter verhindert werden, aber als Fallback
                isValid = false;
                validationMessage = fieldName + ": Ungültige Zahl.";
            }
        }

        // Stil und Tooltip setzen
        if (!isValid) {
            markInvalid(field); // Setzt roten Rand
            setTooltip(field, validationMessage); // Setzt Tooltip
        } else {
            removeErrorStyle(field); // Entfernt roten Rand und Tooltip
        }
    }


    // --- Finale Validierung beim Speichern ---

    public boolean validateForm(ScrollPane scrollPane, TextField businessLinkField, CheckBox businessLinkToggle, TextField emailField) {
        errorMessages.clear();
        errorControls.clear();
        boolean isFormValid = true;

        // Führe alle benötigten Validierungen durch
        isFormValid &= validateRequiredFieldsInternal();
        isFormValid &= validateEmailFormatInternal(emailField, "E-Mail"); // Finale Prüfung
        isFormValid &= validateOptionalBusinessLinkInternal(businessLinkField, businessLinkToggle, "Business-Profil-Link");
        isFormValid &= validateNumericRangesInternal(); // Finale Bereichsprüfung
        isFormValid &= validateDecimalRangesInternal();

        // Füge hier ggf. noch finale Prüfungen für Decimal etc. hinzu

        // Ergebnis verarbeiten
        if (!isFormValid) {
            showErrorSummaryDialog(); // Zeigt alle gesammelten Fehler
            if (!errorControls.isEmpty()) {
                Control firstInvalid = errorControls.get(0);
                Platform.runLater(() -> {
                    scrollToNode(firstInvalid, scrollPane);
                    firstInvalid.requestFocus();
                });
            }
            return false;
        }
        return true;
    }

    // --- Interne Validierungs-Hilfsmethoden ---

    private boolean validateRequiredFieldsInternal() {
        boolean allValid = true;
        for (TextInputControl field : requiredTextFields) {
            // Stil nicht zurücksetzen, da Live-Validierung aktiv sein könnte
            if (field.getText().isBlank()) {
                markInvalid(field);
                errorMessages.add(getFieldName(field, "Eingabefeld") + " darf nicht leer sein.");
                allValid = false;
            }
        }
        for (ComboBox<?> comboBox : requiredComboBoxes) {
            // Stil nicht zurücksetzen
            if (comboBox.getValue() == null) {
                markInvalid(comboBox);
                errorMessages.add(getFieldName(comboBox, "Auswahl") + " muss ausgewählt werden.");
                allValid = false;
            }
        }
        return allValid;
    }

    private boolean validateEmailFormatInternal(TextField emailField, String baseFieldName) {
        // Stil nicht zurücksetzen
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,7}$");
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !emailPattern.matcher(email).matches()) {
            markInvalid(emailField);
            errorMessages.add(baseFieldName + ": Ungültiges E-Mail-Format.");
            return false;
        }
        return true;
    }

    private boolean validateNumericRangesInternal() {
        boolean allValid = true;
        for (NumericRangeField nrf : numericRangeFields) {
            // Stil nicht zurücksetzen
            String text = nrf.field().getText().trim();
            if (text.isEmpty()) {
                if (nrf.required()) {
                    markInvalid(nrf.field());
                    errorMessages.add(nrf.fieldName() + " darf nicht leer sein.");
                    allValid = false;
                }
            } else {
                try {
                    int value = Integer.parseInt(text);
                    if (value < nrf.min() || value > nrf.max()) {
                        markInvalid(nrf.field());
                        errorMessages.add(nrf.fieldName() + " muss zwischen " + nrf.min() + " und " + nrf.max() + " liegen.");
                        allValid = false;
                    }
                } catch (NumberFormatException e) {
                    markInvalid(nrf.field());
                    errorMessages.add(nrf.fieldName() + ": Ungültige Zahleneingabe.");
                    allValid = false;
                }
            }
        }
        return allValid;
    }

    private boolean validateDecimalRangesInternal() {
        boolean allValid = true;
        for (DecimalRangeField drf : decimalRangeFields) {
            String text = drf.field().getText().trim();
            if (text.isEmpty()) {
                if (drf.required()) {
                    markInvalid(drf.field());
                    errorMessages.add(drf.fieldName() + " darf nicht leer sein.");
                    allValid = false;
                }
            } else {
                try {
                    String parsableText = text.replace(',', '.');
                    // Prüfe explizit auf ungültige Endungen oder nur Punkt
                    if (parsableText.equals(".") || parsableText.endsWith(".")) {
                        // Werfe hier die Exception, die parseDouble auch werfen würde
                        throw new NumberFormatException("For input string: \"" + text + "\"");
                    }
                    double value = Double.parseDouble(parsableText); // Parse
                    if (value < drf.min() || value > drf.max()) { // Prüfe Bereich
                        markInvalid(drf.field());
                        errorMessages.add(String.format(Locale.GERMAN, "%s muss zwischen %.1f und %.1f liegen.", drf.fieldName(), drf.min(), drf.max()));
                        allValid = false;
                    }
                } catch (NumberFormatException e) { // Fange nur NumberFormatException
                    markInvalid(drf.field());
                    errorMessages.add(drf.fieldName() + ": Ungültige Dezimalzahl.");
                    allValid = false;
                }
            }
        }
        return allValid;
    }


    /**
     * Validiert ein optionales Textfeld für einen Business-Link (URL) bei der finalen Prüfung.
     * Fügt Fehler zur Liste hinzu und markiert das Feld, wenn ungültig.
     * Entfernt den Fehlerstil, wenn gültig oder nicht relevant.
     *
     * @param linkField Das Textfeld für den Link.
     * @param toggleCheckBox Die CheckBox, die die Eingabe aktiviert.
     * @param baseFieldName Der Name des Feldes für Fehlermeldungen.
     * @return true, wenn der Link gültig ist oder nicht validiert werden muss, sonst false.
     */
    public boolean validateOptionalBusinessLinkInternal(TextField linkField, CheckBox toggleCheckBox, String baseFieldName) {
        // --- IMMER zuerst Stil entfernen ---
        removeErrorStyle(linkField); // Stellt sicher, dass alte Fehler entfernt werden

        boolean isValid = true;

        // Nur prüfen, wenn Checkbox aktiv und Feld sichtbar
        if (toggleCheckBox.isSelected() && linkField.isVisible()) {
            String link = linkField.getText().trim();

            // Nur prüfen, wenn das Feld nicht leer ist
            if (!link.isEmpty()) {
                // Minimalprüfung: Muss Protokoll haben oder zumindest Domain-Struktur
                // Diese Regex ist relativ tolerant
                if (!link.matches("^(https?://)?([\\w\\-]+\\.)+[a-z]{2,}([/\\w .\\-?=&%]*)*/?$")) {
                    // Wenn die Regex NICHT passt -> ungültig
                    markInvalid(linkField);
                    errorMessages.add(baseFieldName + ": Ungültiges URL-Format (z.B. 'https://domain.com').");
                    isValid = false;
                    // Kein try-catch mehr nötig, da wir keine URL parsen, nur Regex nutzen
                }
                // Wenn die Regex passt, bleibt isValid = true und kein Fehler wird gesetzt/markiert.
            }
            // Wenn das Feld leer ist, ist es gültig (da optional), isValid bleibt true.
        }
        // Wenn Checkbox nicht aktiv oder Feld nicht sichtbar, ist es gültig, isValid bleibt true.

        return isValid; // Gibt das Ergebnis der Prüfung zurück
    }

    // --- Styling und UI Hilfsmethoden ---

    private void markInvalid(Control control) {
        if (control != null) {
            control.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            if (!errorControls.contains(control)) {
                errorControls.add(control);
            }
        }
    }

    public void removeErrorStyle(Control control) {
        if (control != null) {
            control.setStyle(null);
            control.setTooltip(null); // Tooltip auch hier entfernen
        }
    }

    // Diese Methode wird von den Listenern aufgerufen, wenn sich Text/Auswahl ändert
    private void removeErrorStyleOnChange(Control control) {
        if (control != null && control.getStyle() != null && control.getStyle().contains("-fx-border-color: red;")) {
            // Stil nur entfernen, wenn Feld geändert wird, Tooltip bleibt ggf. bis zur nächsten Validierung
            control.setStyle(null);
            // Tooltip hier *nicht* unbedingt entfernen, damit die Meldung sichtbar bleibt, bis der Wert gültig ist
            // control.setTooltip(null);
        }
    }

    private void setTooltip(Control control, String message) {
        if (control == null) return;
        if (message == null || message.isBlank()) {
            control.setTooltip(null);
        } else {
            Tooltip tooltip = control.getTooltip();
            if (tooltip == null) {
                tooltip = new Tooltip(message); // Text direkt im Konstruktor setzen
                control.setTooltip(tooltip);
            } else {
                tooltip.setText(message); // Bestehenden Tooltip aktualisieren
            }
        }
    }

    private void showErrorSummaryDialog() {
        if (errorMessages.isEmpty()) return;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validierungsfehler");
        alert.setHeaderText("Bitte korrigieren Sie die folgenden Eingaben:");
        String content = String.join("\n", errorMessages);
        int maxLength = 800;
        if (content.length() > maxLength) {
            content = content.substring(0, maxLength) + "\n...";
        }
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private String getFieldName(Control control, String defaultName) {
        // Versuche PromptText zuerst, dann Text des Labels (falls es ein Labeled ist)
        if (control instanceof TextInputControl tic && tic.getPromptText() != null && !tic.getPromptText().isEmpty()) return tic.getPromptText();
        if (control instanceof ComboBoxBase<?> cbb && cbb.getPromptText() != null && !cbb.getPromptText().isEmpty()) return cbb.getPromptText();
        if (control instanceof Labeled labeled && labeled.getText() != null && !labeled.getText().isEmpty()) return labeled.getText();
        return defaultName;
    }

    private void scrollToNode(Node node, ScrollPane scrollPane) {
        if (scrollPane == null || node == null) return;
        Node content = scrollPane.getContent();
        if (content != null) {
            // Diese Methode zum Scrollen ist oft genauer:
            Platform.runLater(()-> { // Stelle sicher, dass es auf dem FX Thread läuft
                double contentHeight = content.getBoundsInLocal().getHeight();
                double nodeMinY = node.getBoundsInParent().getMinY();
                double nodeMaxY = node.getBoundsInParent().getMaxY();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double vValue = scrollPane.getVvalue();
                double y = nodeMinY; // Scrolle zur Oberkante

                // Berechne VValue für die Oberkante
                double vValueMin = Math.max(0, (y / (contentHeight - viewportHeight)));
                // Berechne VValue, damit auch Unterkante sichtbar ist (falls möglich)
                double vValueMax = Math.max(0, ((y + node.getBoundsInLocal().getHeight() - viewportHeight) / (contentHeight - viewportHeight)));


                // Prüfe, ob der Node bereits (teilweise) sichtbar ist
                double currentVPMinY = (contentHeight - viewportHeight) * vValue;
                double currentVPMaxY = currentVPMinY + viewportHeight;


                if (nodeMinY < currentVPMinY) { // Node ist oberhalb des Viewports
                    scrollPane.setVvalue(vValueMin);
                } else if (nodeMaxY > currentVPMaxY) { // Node ist unterhalb des Viewports
                    scrollPane.setVvalue(vValueMax);
                }
                // Wenn schon sichtbar, nicht scrollen oder nur leicht zentrieren (optional)

                node.requestFocus(); // Fokus setzen
            });
        }
    }

}