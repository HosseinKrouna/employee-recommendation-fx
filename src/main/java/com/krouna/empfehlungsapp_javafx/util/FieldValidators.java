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
}