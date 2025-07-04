package com.krouna.empfehlungsapp_javafx.ui.cells;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class StatusComboBoxTableCell extends TableCell<RecommendationDTO, String> {

    private ComboBox<String> comboBox;

    private static final List<String> STATUS_OPTIONS = List.of("Eingereicht", "Im Prozess", "Abgesagt", "Eingestellt", "Zurückgezogen");
    private final BiConsumer<RecommendationDTO, String> updateAction;
    private static final String FINAL_STATUS_ZURUECKGEZOGEN = "Zurückgezogen";


    public StatusComboBoxTableCell(BiConsumer<RecommendationDTO, String> updateAction) {
        this.updateAction = updateAction;

    }


    @Override
    public void startEdit() {

        if (FINAL_STATUS_ZURUECKGEZOGEN.equals(getItem()) ||
                isEmpty() ||
                !isEditable() ||
                !getTableView().isEditable() ||
                !getTableColumn().isEditable()) {

            System.out.println("Bearbeiten verhindert für Item: " + getItem() + " (isEditable: " + isEditable() + ")");
            return;
        }


        super.startEdit();

        if (comboBox == null) {
            createComboBox();
        }

        comboBox.getSelectionModel().select(getItem());
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(comboBox);
        Platform.runLater(() -> comboBox.requestFocus());
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getItem());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);


        Tooltip currentTooltip = getTooltip();

        if (empty || item == null) {

            setText(null);
            setGraphic(null);
            setCursor(Cursor.DEFAULT);

            if (currentTooltip != null) {
                Tooltip.uninstall(this, currentTooltip);
            }
        } else {

            boolean isEditableStatus = !FINAL_STATUS_ZURUECKGEZOGEN.equals(item);


            boolean isCellConfiguredEditable = isEditable() && getTableView().isEditable() && getTableColumn().isEditable();


            if (isEditing() && isEditableStatus && isCellConfiguredEditable) {

                if (comboBox != null) {
                    comboBox.getSelectionModel().select(item);
                }
                setText(null);
                setGraphic(comboBox);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setCursor(Cursor.DEFAULT);

                if (currentTooltip != null) {
                    Tooltip.uninstall(this, currentTooltip);
                }

            } else {

                setText(item);
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);


                if (isEditableStatus && isCellConfiguredEditable) {
                    setCursor(Cursor.HAND);

                    if (currentTooltip == null) {
                        Tooltip tt = new Tooltip("Status ändern (Doppelklick oder Enter)");

                        Tooltip.install(this, tt);
                    }
                } else {

                    setCursor(Cursor.DEFAULT); // Standard-Cursor

                    if (currentTooltip != null) {
                        Tooltip.uninstall(this, currentTooltip);
                    }
                }
            }
        }
    }


    private void createComboBox() {

        List<String> selectableOptions = STATUS_OPTIONS.stream()
                .filter(option -> !FINAL_STATUS_ZURUECKGEZOGEN.equals(option))
                .collect(Collectors.toList());

        comboBox = new ComboBox<>(FXCollections.observableArrayList(selectableOptions));

        comboBox.setPrefWidth(150);
        comboBox.setMaxWidth(Double.MAX_VALUE);


        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldStatus, newStatus) -> {
            if (isEditing() && newStatus != null && !newStatus.equals(oldStatus)) {

                if (!FINAL_STATUS_ZURUECKGEZOGEN.equals(newStatus)) {
                    RecommendationDTO currentDto = getTableView().getItems().get(getIndex());
                    if (updateAction != null) {

                        updateAction.accept(currentDto, newStatus);
                    } else {

                        System.err.println("UpdateAction ist null in StatusComboBoxTableCell!");
                        cancelEdit();
                    }
                } else {

                    System.out.println("Versuch, 'Zurückgezogen' auszuwählen, ignoriert.");

                }
            }
        });



        comboBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {

                String selectedValue = comboBox.getValue();
                if (selectedValue != null && !FINAL_STATUS_ZURUECKGEZOGEN.equals(selectedValue)) {
                    commitEdit(selectedValue);
                } else {

                    cancelEdit();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                event.consume();
            }
        });

        comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && isEditing()) {
                String selectedValue = comboBox.getValue();
                if (selectedValue != null && !FINAL_STATUS_ZURUECKGEZOGEN.equals(selectedValue)) {
                    commitEdit(selectedValue);
                } else {
                    cancelEdit();
                }
            }
        });

    }


    @Override
    public void commitEdit(String newValue) {

        if (FINAL_STATUS_ZURUECKGEZOGEN.equals(newValue)) {
            System.out.println("Commit für Status 'Zurückgezogen' verhindert.");
            cancelEdit();
            return;
        }

        if (!isEditing()) {
            return;
        }
        super.commitEdit(newValue);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}