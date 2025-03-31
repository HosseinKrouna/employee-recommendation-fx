package com.krouna.empfehlungsapp_javafx.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * Utility class for validating text fields with numeric constraints
 */
public class FieldValidators {

    /**
     * Configures a text field to only accept valid integer input
     *
     * @param field The field to configure
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @param label Label for error messages
     */
    public void setupNumericField(TextField field, int min, int max, String label) {
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;

            try {
                int value = Integer.parseInt(newText);
                boolean isValid = value >= min && value <= max;
                field.setStyle(isValid ? null : "-fx-border-color: red;");
                return isValid ? change : null;
            } catch (NumberFormatException e) {
                field.setStyle("-fx-border-color: red;");
                return null;
            }
        });

        field.setTextFormatter(formatter);
    }

    /**
     * Configures a text field to only accept valid decimal input
     *
     * @param field The field to configure
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @param label Label for error messages
     */
    public void setupDecimalField(TextField field, double min, double max, String label) {
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;

            try {
                double value = Double.parseDouble(newText.replace(",", "."));
                boolean isValid = value >= min && value <= max;
                field.setStyle(isValid ? null : "-fx-border-color: red;");
                return isValid ? change : null;
            } catch (NumberFormatException e) {
                field.setStyle("-fx-border-color: red;");
                return null;
            }
        });

        field.setTextFormatter(formatter);
    }


    /**
     * Konfiguriert ein Textfeld zur Validierung von E-Mail-Adressen mit Echtzeit-Feedback
     *
     * @param field Das zu konfigurierende Textfeld
     * @param feedbackLabel Ein Label, das den Validierungsstatus anzeigt
     */
    public void setupEmailField(TextField field, javafx.scene.control.Label feedbackLabel) {
        // Initial verstecken oder neutral setzen
        feedbackLabel.setText("");

        // TextProperty Listener für Echtzeit-Feedback
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                feedbackLabel.setText("");
                feedbackLabel.setStyle("-fx-text-fill: black;");
                field.setStyle(null);
                return;
            }

            // E-Mail-Validierung mit regulärem Ausdruck
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            boolean isValid = newValue.matches(emailRegex);

            if (isValid) {
                feedbackLabel.setText("E-Mail-Format ist gültig");
                feedbackLabel.setStyle("-fx-text-fill: green;");
                field.setStyle(null);
            } else {
                feedbackLabel.setText("Ungültiges E-Mail-Format");
                feedbackLabel.setStyle("-fx-text-fill: red;");
                field.setStyle("-fx-border-color: red;");
            }
        });

        // Zusätzlich kann noch der TextFormatter verwendet werden
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            return change; // Erlaubt alle Eingaben, Feedback erfolgt über den Listener
        });

        field.setTextFormatter(formatter);
    }


}