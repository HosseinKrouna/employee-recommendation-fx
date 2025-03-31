package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Utility class for dynamically building form elements
 */
public class FormBuilder {

    private final VBox container;
    private final ScrollPane scrollPane;
    private final FieldValidators fieldValidators;

    /**
     * Constructor that takes references to the UI components where dynamic form elements will be added
     *
     * @param container The container to add form elements to
     * @param scrollPane The scroll pane containing the form
     */
    public FormBuilder(VBox container, ScrollPane scrollPane) {
        this.container = container;
        this.scrollPane = scrollPane;
        this.fieldValidators = new FieldValidators();
    }

    /**
     * Adds a custom skill row to the skills container
     */
    public void addCustomSkill() {
        TextField technologyField = new TextField();
        technologyField.setPromptText("Technologie");

        TextField skillField = new TextField();
        skillField.setPromptText("weiterer Skill");

        TextField percentageField = new TextField();
        percentageField.setPromptText("Kenntnisgrad (%)");
        fieldValidators.setupNumericField(percentageField, 0, 100, "Kenntnisgrad");

        Button deleteButton = new Button("Löschen");

        HBox skillEntry = new HBox(10);
        skillEntry.getChildren().addAll(technologyField, skillField, percentageField, deleteButton);

        deleteButton.setOnAction(e -> {
            scrollPane.setDisable(true);
            container.getChildren().remove(skillEntry);
            Platform.runLater(() -> {
                scrollPane.setDisable(false);
                scrollPane.setVvalue(1);
            });
        });

        container.getChildren().add(skillEntry);
    }
}