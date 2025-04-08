package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class FormValidator {
    private final List<TextInputControl> requiredTextFields = new ArrayList<>();
    private final List<ComboBox<?>> requiredComboBoxes = new ArrayList<>();

    public void addRequiredTextField(TextInputControl field) {
        requiredTextFields.add(field);
    }

    public void addRequiredComboBox(ComboBox<?> comboBox) {
        requiredComboBoxes.add(comboBox);
    }

    public boolean validateForm(ScrollPane scrollPane) {
        List<Control> missing = validateRequiredFields();

        if (!missing.isEmpty()) {
            Control firstInvalid = missing.get(0);
            Platform.runLater(() -> {
                firstInvalid.requestFocus();
                scrollToNode(firstInvalid, scrollPane);
            });
            return false;
        }

        return true;
    }

    private List<Control> validateRequiredFields() {
        List<Control> missing = new ArrayList<>();

        for (TextInputControl field : requiredTextFields) {
            if (field.getText().isBlank()) {
                markInvalid(field);
                missing.add(field);
            } else {
                field.setStyle(""); // Reset
            }
        }

        for (ComboBox<?> comboBox : requiredComboBoxes) {
            if (comboBox.getValue() == null) {
                markInvalid(comboBox);
                missing.add(comboBox);
            } else {
                comboBox.setStyle(""); // Reset
            }
        }

        return missing;
    }

    private void markInvalid(Control field) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
    }

    private void scrollToNode(Node node, ScrollPane scrollPane) {
        if (scrollPane == null) return;

        Node content = scrollPane.getContent();
        if (content != null) {
            Bounds contentBounds = content.localToScene(content.getBoundsInLocal());
            Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());

            double y = nodeBounds.getMinY() - contentBounds.getMinY();
            double vvalue = y / (contentBounds.getHeight() - scrollPane.getViewportBounds().getHeight());
            scrollPane.setVvalue(Math.min(Math.max(vvalue, 0), 1)); // Clamp between 0 and 1
        }
    }

    public void setupNumericField(TextField field, int min, int max, String fieldName) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }

            if (Pattern.matches("\\d*", newText)) {
                try {
                    int value = Integer.parseInt(newText);
                    if (value >= min && value <= max) {
                        return change;
                    }
                } catch (NumberFormatException e) {
                    // Skip validation for parsing error
                }
            }
            return null;
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }

    public void setupDecimalField(TextField field, double min, double max, String fieldName) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || newText.equals(",") || newText.equals(".")) {
                return change;
            }

            // Replace comma with dot for decimal parsing
            newText = newText.replace(',', '.');

            if (Pattern.matches("\\d*\\.?\\d*", newText)) {
                try {
                    double value = Double.parseDouble(newText);
                    if (value >= min && value <= max) {
                        return change;
                    }
                } catch (NumberFormatException e) {
                    // Skip validation for parsing error
                }
            }
            return null;
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }

    public void setupEmailField(TextField emailField, Label feedbackLabel) {
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

        emailField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            String email = emailField.getText().trim();

            if (email.isEmpty()) {
                feedbackLabel.setText("");
                return;
            }

            boolean isValid = emailPattern.matcher(email).matches();

            if (!isValid) {
                feedbackLabel.setText("Bitte gib eine gültige E-Mail-Adresse ein.");
                feedbackLabel.setStyle("-fx-text-fill: red;");
            } else {
                feedbackLabel.setText("E-Mail-Format ist gültig.");
                feedbackLabel.setStyle("-fx-text-fill: green;");
            }
        });
    }
}