package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;

import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.scene.layout.Region;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class FormValidator {

    private final List<TextInputControl> requiredTextFields = new ArrayList<>();
    private final List<ComboBox<?>> requiredComboBoxes = new ArrayList<>();


    private final List<String> errorMessages = new ArrayList<>();
    private final List<Control> errorControls = new ArrayList<>();



    private record NumericRangeField(TextField field, int min, int max, String fieldName, boolean required) {}
    private final List<NumericRangeField> numericRangeFields = new ArrayList<>();

    private record DecimalRangeField(TextField field, double min, double max, String fieldName, boolean required) {}
    private final List<DecimalRangeField> decimalRangeFields = new ArrayList<>();


    private static final NumberFormat DECIMAL_FORMAT = DecimalFormat.getNumberInstance(Locale.US);



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




    public void addDecimalRangeValidation(TextField field, double min, double max, String fieldName, boolean isRequired) {
        decimalRangeFields.add(new DecimalRangeField(field, min, max, fieldName, isRequired));
        setupDecimalFieldWithLiveRangeCheck(field, min, max, fieldName, isRequired);
    }

    public void setupNumericFieldWithLiveRangeCheck(TextField field, int min, int max, String fieldName, boolean isRequired) {

        UnaryOperator<TextFormatter.Change> charFilter = change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty() || Pattern.matches("\\d*", newText)) {
                return change;
            }
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(charFilter));


        field.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNumericFieldLive(field, min, max, fieldName, isRequired);
        });


        field.focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                validateNumericFieldLive(field, min, max, fieldName, isRequired);
            }
        });
    }

    public void setupDecimalFieldWithLiveRangeCheck(TextField field, double min, double max, String fieldName, boolean isRequired) {

        UnaryOperator<TextFormatter.Change> charFilter = change -> {
            String currentText = change.getControlText();
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;

            if (!Pattern.matches("^[\\d.,]*$", newText)) return null;

            String checkText = newText.replace(',', '.');

            if (checkText.indexOf('.') != checkText.lastIndexOf('.')) return null;

            return change;
        };
        field.setTextFormatter(new TextFormatter<>(charFilter));


        field.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDecimalFieldLive(field, min, max, fieldName, isRequired);
        });

        field.focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (!newVal) {
                validateDecimalFieldLive(field, min, max, fieldName, isRequired);
            }
        });
    }

    public void setupEmailField(TextField emailField, Label feedbackLabel) {
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,7}$");
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            String email = newValue.trim();

            if (email.isEmpty()) {
                feedbackLabel.setText("");
                removeErrorStyle(emailField);
            } else if (!emailPattern.matcher(email).matches()) {
                feedbackLabel.setText("Ungültiges E-Mail-Format.");
                feedbackLabel.setStyle("-fx-text-fill: red;");

            } else {
                feedbackLabel.setText("Format gültig.");
                feedbackLabel.setStyle("-fx-text-fill: green;");
                removeErrorStyle(emailField);
            }
        });
    }


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

                if (parsableText.equals(".")) {

                    isValid = false;
                    validationMessage = fieldName + ": Unvollständige Zahl.";
                } else {
                    // Versuche zu parsen
                    double value = Double.parseDouble(parsableText);

                    if (value < min || value > max) {
                        isValid = false;
                        validationMessage = String.format(Locale.GERMAN, "%s muss zwischen %.1f und %.1f liegen.", fieldName, min, max);
                    }
                }
            } catch (NumberFormatException e) {
                isValid = false;
                validationMessage = fieldName + ": Ungültige Dezimalzahl.";
            }
        }

        if (!isValid) {
            markInvalid(field);
            setTooltip(field, validationMessage);
        } else {
            removeErrorStyle(field);
        }
    }





    private void validateNumericFieldLive(TextField field, int min, int max, String fieldName, boolean isRequired) {
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
                int value = Integer.parseInt(text);
                if (value < min || value > max) {
                    isValid = false;
                    validationMessage = fieldName + " muss zwischen " + min + " und " + max + " liegen.";
                }
            } catch (NumberFormatException e) {

                isValid = false;
                validationMessage = fieldName + ": Ungültige Zahl.";
            }
        }

        if (!isValid) {
            markInvalid(field);
            setTooltip(field, validationMessage);
        } else {
            removeErrorStyle(field);
        }
    }




    public boolean validateForm(ScrollPane scrollPane, TextField businessLinkField, CheckBox businessLinkToggle, TextField emailField) {
        errorMessages.clear();
        errorControls.clear();
        boolean isFormValid = true;


        isFormValid &= validateRequiredFieldsInternal();
        isFormValid &= validateEmailFormatInternal(emailField, "E-Mail");
        isFormValid &= validateOptionalBusinessLinkInternal(businessLinkField, businessLinkToggle, "Business-Profil-Link");
        isFormValid &= validateNumericRangesInternal();
        isFormValid &= validateDecimalRangesInternal();


        if (!isFormValid) {
            showErrorSummaryDialog();
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



    private boolean validateRequiredFieldsInternal() {
        boolean allValid = true;
        for (TextInputControl field : requiredTextFields) {

            if (field.getText().isBlank()) {
                markInvalid(field);
                errorMessages.add(getFieldName(field, "Eingabefeld") + " darf nicht leer sein.");
                allValid = false;
            }
        }
        for (ComboBox<?> comboBox : requiredComboBoxes) {

            if (comboBox.getValue() == null) {
                markInvalid(comboBox);
                errorMessages.add(getFieldName(comboBox, "Auswahl") + " muss ausgewählt werden.");
                allValid = false;
            }
        }
        return allValid;
    }

    private boolean validateEmailFormatInternal(TextField emailField, String baseFieldName) {

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

                    if (parsableText.equals(".") || parsableText.endsWith(".")) {

                        throw new NumberFormatException("For input string: \"" + text + "\"");
                    }
                    double value = Double.parseDouble(parsableText);
                    if (value < drf.min() || value > drf.max()) {
                        markInvalid(drf.field());
                        errorMessages.add(String.format(Locale.GERMAN, "%s muss zwischen %.1f und %.1f liegen.", drf.fieldName(), drf.min(), drf.max()));
                        allValid = false;
                    }
                } catch (NumberFormatException e) {
                    markInvalid(drf.field());
                    errorMessages.add(drf.fieldName() + ": Ungültige Dezimalzahl.");
                    allValid = false;
                }
            }
        }
        return allValid;
    }



    public boolean validateOptionalBusinessLinkInternal(TextField linkField, CheckBox toggleCheckBox, String baseFieldName) {

        removeErrorStyle(linkField);

        boolean isValid = true;

        if (toggleCheckBox.isSelected() && linkField.isVisible()) {
            String link = linkField.getText().trim();


            if (!link.isEmpty()) {

                if (!link.matches("^(https?://)?([\\w\\-]+\\.)+[a-z]{2,}([/\\w .\\-?=&%]*)*/?$")) {

                    markInvalid(linkField);
                    errorMessages.add(baseFieldName + ": Ungültiges URL-Format (z.B. 'https://domain.com').");
                    isValid = false;

                }

            }

        }


        return isValid;
    }



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
            control.setTooltip(null);
        }
    }


    private void removeErrorStyleOnChange(Control control) {
        if (control != null && control.getStyle() != null && control.getStyle().contains("-fx-border-color: red;")) {

            control.setStyle(null);

        }
    }

    private void setTooltip(Control control, String message) {
        if (control == null) return;
        if (message == null || message.isBlank()) {
            control.setTooltip(null);
        } else {
            Tooltip tooltip = control.getTooltip();
            if (tooltip == null) {
                tooltip = new Tooltip(message);
                control.setTooltip(tooltip);
            } else {
                tooltip.setText(message);
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

        if (control instanceof TextInputControl tic && tic.getPromptText() != null && !tic.getPromptText().isEmpty()) return tic.getPromptText();
        if (control instanceof ComboBoxBase<?> cbb && cbb.getPromptText() != null && !cbb.getPromptText().isEmpty()) return cbb.getPromptText();
        if (control instanceof Labeled labeled && labeled.getText() != null && !labeled.getText().isEmpty()) return labeled.getText();
        return defaultName;
    }

    private void scrollToNode(Node node, ScrollPane scrollPane) {
        if (scrollPane == null || node == null) return;
        Node content = scrollPane.getContent();
        if (content != null) {

            Platform.runLater(()-> {
                double contentHeight = content.getBoundsInLocal().getHeight();
                double nodeMinY = node.getBoundsInParent().getMinY();
                double nodeMaxY = node.getBoundsInParent().getMaxY();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();
                double vValue = scrollPane.getVvalue();
                double y = nodeMinY;


                double vValueMin = Math.max(0, (y / (contentHeight - viewportHeight)));

                double vValueMax = Math.max(0, ((y + node.getBoundsInLocal().getHeight() - viewportHeight) / (contentHeight - viewportHeight)));



                double currentVPMinY = (contentHeight - viewportHeight) * vValue;
                double currentVPMaxY = currentVPMinY + viewportHeight;


                if (nodeMinY < currentVPMinY) {
                    scrollPane.setVvalue(vValueMin);
                } else if (nodeMaxY > currentVPMaxY) {
                    scrollPane.setVvalue(vValueMax);
                }


                node.requestFocus();
            });
        }
    }

}