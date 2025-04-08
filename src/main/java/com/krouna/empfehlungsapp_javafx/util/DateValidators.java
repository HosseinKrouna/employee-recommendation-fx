package com.krouna.empfehlungsapp_javafx.util;

import javafx.scene.control.DatePicker;

import java.time.LocalDate;

public class DateValidators {

    public void setupDateValidation(DatePicker datePicker, String fieldName, boolean allowPastDates) {
        datePicker.setOnAction(e -> validateDate(datePicker, fieldName, allowPastDates));
    }

    private void validateDate(DatePicker datePicker, String fieldName, boolean allowPastDates) {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            return;
        }

        if (!allowPastDates && selectedDate.isBefore(LocalDate.now())) {
            DialogUtil.showError("Ungültiges Datum",
                    fieldName + " darf nicht in der Vergangenheit liegen.");
            datePicker.setValue(null);
        }
    }

    public void setupDateDependency(DatePicker firstDate, DatePicker secondDate,
                                    String firstLabel, String secondLabel) {
        secondDate.setOnAction(e -> {
            if (firstDate.getValue() == null || secondDate.getValue() == null) {
                return;
            }

            if (secondDate.getValue().isBefore(firstDate.getValue())) {
                DialogUtil.showError("Ungültiges Datum",
                        secondLabel + " kann nicht vor " + firstLabel + " liegen.");
                secondDate.setValue(null);
            }
        });
    }
}