package com.krouna.empfehlungsapp_javafx.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;


public class FieldValidators {


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



    public void setupEmailField(TextField field, javafx.scene.control.Label feedbackLabel) {

        feedbackLabel.setText("");


        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                feedbackLabel.setText("");
                feedbackLabel.setStyle("-fx-text-fill: black;");
                field.setStyle(null);
                return;
            }


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


        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            return change;
        });

        field.setTextFormatter(formatter);
    }


}