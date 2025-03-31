package com.krouna.empfehlungsapp_javafx.util;

import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

/**
 * Utility class for validating date fields and dependencies between dates
 */
public class DateValidators {

    /**
     * Sets up date validation for a DatePicker
     *
     * @param picker DatePicker to validate
     * @param labelText Label for error messages
     * @param disallowFutureDates Whether to disallow future dates
     */
    public void setupDateValidation(DatePicker picker, String labelText, boolean disallowFutureDates) {
        picker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate == null) return;

            LocalDate today = LocalDate.now();
            boolean isInvalid = disallowFutureDates && newDate.isAfter(today);

            if (isInvalid) {
                DialogUtil.showError("Ungültiges Datum", labelText + " darf nicht in der Zukunft liegen.");
                picker.setStyle("-fx-border-color: red;");
            } else {
                picker.setStyle(null);
            }
        });
    }

    /**
     * Sets up a dependency relationship between two date fields
     *
     * @param earlier DatePicker that should contain an earlier date
     * @param later DatePicker that should contain a later date
     * @param earlierLabel Label for the earlier date
     * @param laterLabel Label for the later date
     */
    public void setupDateDependency(DatePicker earlier, DatePicker later, String earlierLabel, String laterLabel) {
        ChangeListener<LocalDate> listener = (obs, oldDate, newDate) -> {
            LocalDate d1 = earlier.getValue();
            LocalDate d2 = later.getValue();

            if (d1 != null && d2 != null && d2.isBefore(d1)) {
                DialogUtil.showError("Ungültige Kombination", laterLabel + " darf nicht vor dem " + earlierLabel + " liegen.");
                later.setStyle("-fx-border-color: red;");
            } else {
                later.setStyle(null);
            }
        };

        earlier.valueProperty().addListener(listener);
        later.valueProperty().addListener(listener);
    }

    /**
     * Sets up a dependency between start date and notice period date
     *
     * @param startPicker DatePicker for start date
     * @param noticePicker DatePicker for notice period date
     */
    public void setupStartAndNoticeDependency(DatePicker startPicker, DatePicker noticePicker) {
        ChangeListener<LocalDate> listener = (obs, oldVal, newVal) -> {
            var notice = noticePicker.getValue();
            var start = startPicker.getValue();

            if (notice != null && start != null && start.isBefore(notice)) {
                DialogUtil.showError("Ungültige Eingabe", "Startdatum darf nicht vor Ende der Kündigungsfrist liegen.");
                startPicker.setStyle("-fx-border-color: red;");
            } else {
                startPicker.setStyle(null);
            }
        };

        noticePicker.valueProperty().addListener(listener);
        startPicker.valueProperty().addListener(listener);
    }
}