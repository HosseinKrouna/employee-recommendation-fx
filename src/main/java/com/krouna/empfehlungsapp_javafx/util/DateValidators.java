package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

public class DateValidators {

    /**
     * Richtet Validierungslistener für einen einzelnen DatePicker ein.
     * Prüft, ob das Datum in der Zukunft liegt (optional).
     *
     * @param datePicker Der zu validierende DatePicker.
     * @param fieldName Der Name des Feldes für Fehlermeldungen.
     * @param preventFutureDates true, wenn zukünftige Daten verhindert werden sollen.
     */
    public void setupFutureDateValidation(DatePicker datePicker, String fieldName, boolean preventFutureDates) {
        // Listener hinzufügen, der bei jeder Datumsänderung validiert
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateSingleDate(datePicker, fieldName, preventFutureDates);
        });
        // Optional: Initiale Validierung beim Einrichten?
        // validateSingleDate(datePicker, fieldName, preventFutureDates);
    }

    /**
     * Validiert ein einzelnes Datum gegen die Zukunft.
     * Zeigt einen Fehler und setzt das Datum zurück, wenn es ungültig ist.
     */
    private void validateSingleDate(DatePicker datePicker, String fieldName, boolean preventFutureDates) {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            // Leeres Datum ist gültig (Pflichtfeldprüfung erfolgt ggf. woanders)
            return;
        }

        if (preventFutureDates && selectedDate.isAfter(LocalDate.now())) {
            // Platform.runLater, falls dies aus einem Listener aufgerufen wird,
            // der den Wert ändert, während er noch verarbeitet wird.
            Platform.runLater(() -> {
                DialogUtil.showError("Ungültiges Datum",
                        fieldName + " darf nicht in der Zukunft liegen.");
                datePicker.setValue(null); // Setze ungültiges Datum zurück
                datePicker.getEditor().clear(); // Lösche auch den Text im Editor
            });
        }
        // Optional: Hier könnte man auch prüfen, ob allowPastDates greift,
        // aber das hast du in deiner ursprünglichen Methode nicht aktiv genutzt.
    }

    /**
     * Richtet eine Abhängigkeitsprüfung zwischen zwei DatePickern ein (Start vs. Ende).
     * Stellt sicher, dass das Enddatum nicht vor dem Startdatum liegt.
     * Validiert BEIDE Felder, wenn sich eines ändert.
     *
     * @param startDatePicker Der DatePicker für das Startdatum.
     * @param endDatePicker Der DatePicker für das Enddatum.
     * @param startLabel Label für das Startdatum (für Fehlermeldungen).
     * @param endLabel Label für das Enddatum (für Fehlermeldungen).
     */
    public void setupDateDependency(DatePicker startDatePicker, DatePicker endDatePicker,
                                    String startLabel, String endLabel) {

        // Listener für das Startdatum: Wenn es sich ändert, validiere die Abhängigkeit
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateOrder(startDatePicker, endDatePicker, startLabel, endLabel);
        });

        // Listener für das Enddatum: Wenn es sich ändert, validiere die Abhängigkeit
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateOrder(startDatePicker, endDatePicker, startLabel, endLabel);
        });

        // Optional: Initiale Validierung, falls beide Felder schon Werte haben?
        // validateDateOrder(startDatePicker, endDatePicker, startLabel, endLabel);
    }

    /**
     * Prüft, ob das Enddatum vor dem Startdatum liegt.
     * Zeigt einen Fehler und setzt das *geänderte* ungültige Datum zurück.
     */
    private void validateDateOrder(DatePicker startDatePicker, DatePicker endDatePicker,
                                   String startLabel, String endLabel) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Nur prüfen, wenn beide Daten gesetzt sind
        if (startDate == null || endDate == null) {
            return;
        }

        // Prüfe, ob das Enddatum VOR dem Startdatum liegt
        if (endDate.isBefore(startDate)) {
            // Finde heraus, welches Feld zuletzt geändert wurde, um die Fehlermeldung
            // und das Zurücksetzen darauf zu beziehen (heuristisch, nicht 100% sicher)
            // Einfacher Ansatz: Zeige generischen Fehler, setze das Enddatum zurück.
            Platform.runLater(() -> {
                DialogUtil.showError("Ungültige Datumsreihenfolge",
                        endLabel + " ('" + endDate + "') kann nicht vor " + startLabel + " ('" + startDate + "') liegen.");
                // Setze das Enddatum zurück, da es die Abhängigkeit verletzt
                endDatePicker.setValue(null);
                endDatePicker.getEditor().clear();
            });
        }
        // Die umgekehrte Prüfung (Start nach Ende) wird durch den anderen Listener abgedeckt.
    }
}