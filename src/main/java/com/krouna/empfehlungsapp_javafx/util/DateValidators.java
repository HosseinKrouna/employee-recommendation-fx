package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

public class DateValidators {


    public void setupFutureDateValidation(DatePicker datePicker, String fieldName, boolean preventFutureDates) {

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateSingleDate(datePicker, fieldName, preventFutureDates);
        });

    }


    private void validateSingleDate(DatePicker datePicker, String fieldName, boolean preventFutureDates) {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {

            return;
        }

        if (preventFutureDates && selectedDate.isAfter(LocalDate.now())) {

            Platform.runLater(() -> {
                DialogUtil.showError("Ungültiges Datum",
                        fieldName + " darf nicht in der Zukunft liegen.");
                datePicker.setValue(null);
                datePicker.getEditor().clear();
            });
        }

    }


    public void setupDateDependency(DatePicker startDatePicker, DatePicker endDatePicker,
                                    String startLabel, String endLabel) {


        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateOrder(startDatePicker, endDatePicker, startLabel, endLabel);
        });


        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateOrder(startDatePicker, endDatePicker, startLabel, endLabel);
        });


    }


    private void validateDateOrder(DatePicker startDatePicker, DatePicker endDatePicker,
                                   String startLabel, String endLabel) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();


        if (startDate == null || endDate == null) {
            return;
        }


        if (endDate.isBefore(startDate)) {

            Platform.runLater(() -> {
                DialogUtil.showError("Ungültige Datumsreihenfolge",
                        endLabel + " ('" + endDate + "') kann nicht vor " + startLabel + " ('" + startDate + "') liegen.");

                endDatePicker.setValue(null);
                endDatePicker.getEditor().clear();
            });
        }

    }
}