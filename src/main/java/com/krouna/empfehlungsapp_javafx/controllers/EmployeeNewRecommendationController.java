package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.Consumer;

public class EmployeeNewRecommendationController {

    @FXML
    private TextField candidateFirstnameField;
    @FXML
    private TextField candidateLastnameField;
//    @FXML
//    private TextField positionField;
//    @FXML
//    private TextField documentCvField;

    @FXML
    private ScrollPane scrollPane; // ScrollPane im FXML

    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField knownFromField;
    @FXML private DatePicker contactDatePicker;
    @FXML private TextField conversationPeriodField;

    @FXML private ComboBox<String> employmentStatusCombo;
    @FXML private VBox currentPositionBox;
    @FXML private TextField currentPositionField;
    @FXML private TextField currentCareerLevelField;
    @FXML private VBox lastPositionBox;
    @FXML private TextField lastPositionField;
    @FXML private TextField lastCareerLevelField;

    // Checkboxes für "Kontakt ist informiert zu"
    @FXML private CheckBox informedPositionCheck;
    @FXML private CheckBox informedTasksCheck;
    @FXML private CheckBox informedRequirementsCheck;
    @FXML private CheckBox informedClientsProjectsCheck;
    @FXML private CheckBox informedBenefitsCheck;
    @FXML private CheckBox informedTrainingCheck;
    @FXML private CheckBox informedCoachCheck;
    @FXML private CheckBox informedRolesCheck;

    // Berufserfahrung, Rolle, Verfügbarkeit
    @FXML private TextField experienceYearsField;
    @FXML private ComboBox<String> positionField;
    @FXML private DatePicker noticePeriodDatePicker;
    @FXML private DatePicker startDatePicker;
    @FXML private TextField salaryExpectationField;
    @FXML private TextField workHoursField;
    @FXML private TextField travelWillingnessField;

    // CV-Wahl
    @FXML private ComboBox<String> cvChoiceCombo;
    @FXML private Button uploadCvButton;
    @FXML private Label cvByEmailLabel;
    @FXML private CheckBox cvLinkToggle;
    @FXML private TextField documentCvField;

    // Persönlichkeitstyp
    @FXML private TextArea personalityTypArea;

    // --- Backend
    @FXML private CheckBox javaCheckBox;
    @FXML private TextField javaPercentField;
    @FXML private CheckBox springCheckBox;
    @FXML private TextField springPercentField;
    @FXML private CheckBox backendOtherCheckBox;
    @FXML private TextField backendOtherPercentField;
    @FXML private TextField backendOtherNameField;

    // --- Frontend
    @FXML private CheckBox angularCheckBox;
    @FXML private TextField angularPercentField;
    @FXML private CheckBox reactCheckBox;
    @FXML private TextField reactPercentField;
    @FXML private CheckBox vueCheckBox;
    @FXML private TextField vuePercentField;
    @FXML private CheckBox frontendOtherCheckBox;
    @FXML private TextField frontendOtherPercentField;
    @FXML private TextField frontendOtherNameField;

    // --- Datenbanken
    @FXML private CheckBox sqlCheckBox;
    @FXML private TextField sqlPercentField;
    @FXML private CheckBox mongoCheckBox;
    @FXML private TextField mongoPercentField;
    @FXML private CheckBox databaseOtherCheckBox;
    @FXML private TextField databaseOtherPercentField;
    @FXML private TextField databaseOtherNameField;

    // --- Buildsysteme
    @FXML private CheckBox mavenCheckBox;
    @FXML private TextField mavenPercentField;
    @FXML private CheckBox gradleCheckBox;
    @FXML private TextField gradlePercentField;
    @FXML private CheckBox buildOtherCheckBox;
    @FXML private TextField buildOtherPercentField;
    @FXML private TextField buildOtherNameField;

    // --- CI/CD Tools
    @FXML private CheckBox jenkinsCheckBox;
    @FXML private TextField jenkinsPercentField;
    @FXML private CheckBox azureCheckBox;
    @FXML private TextField azurePercentField;
    @FXML private CheckBox bambooCheckBox;
    @FXML private TextField bambooPercentField;
    @FXML private CheckBox cicdOtherCheckBox;
    @FXML private TextField cicdOtherPercentField;
    @FXML private TextField cicdOtherNameField;


    // Weitere individuelle Skills
    @FXML private VBox customSkillsContainer;
    @FXML private Button addSkillButton;

    // Textfelder für freie Eingaben
    @FXML private TextArea hobbiesField;
    @FXML private TextArea projectExperienceField;
    @FXML private TextArea miscellaneousField;

    // Submit-Button
    @FXML private Button submitButton;


    private String uploadedCvFilename;

    private final BackendService backendService = new BackendService();

    @FXML
    private void initialize() {

        FocusTraversHelper.cancelFocusTravers(scrollPane.getContent());

        // Hier kannst du sicherstellen, dass die FXML-Elemente initialisiert sind
        if (scrollPane == null) {
            System.out.println("ScrollPane ist null!");
        } else {
            System.out.println("ScrollPane wurde erfolgreich geladen.");
        }

        System.out.println("cicdOtherCheckBox = " + cicdOtherCheckBox);
        System.out.println("backendOtherCheckBox = " + backendOtherCheckBox);
        System.out.println("buildOtherCheckBox = " + buildOtherCheckBox);

        System.out.println("Initialize wurde aufgerufen!");

        // 1. Positionen für ComboBox "preferredRoleComboBox" setzen
        positionField.getItems().addAll(
                "Junior Developer", "Mid-Level Developer", "Senior Developer", "Team Lead", "Architekt"
        );

        // 2. Beschäftigungsstatus-Logik (zeigt passende Eingabefelder)
        employmentStatusCombo.setOnAction(e -> {
            String status = employmentStatusCombo.getValue();
            if ("In Anstellung".equals(status)) {
                currentPositionBox.setVisible(true);
                currentPositionBox.setManaged(true);
                lastPositionBox.setVisible(false);
                lastPositionBox.setManaged(false);
            } else if ("Arbeitsuchend".equals(status)) {
                lastPositionBox.setVisible(true);
                lastPositionBox.setManaged(true);
                currentPositionBox.setVisible(false);
                currentPositionBox.setManaged(false);
            }
        });

        // 3. Checkbox-Logik für Skill-Prozentfelder
        setupSkillCheckbox(javaCheckBox, javaPercentField, null);
        setupSkillCheckbox(springCheckBox, springPercentField, null);
        setupSkillCheckbox(backendOtherCheckBox, backendOtherPercentField, backendOtherNameField);

        setupSkillCheckbox(angularCheckBox, angularPercentField, null);
        setupSkillCheckbox(reactCheckBox, reactPercentField, null);
        setupSkillCheckbox(vueCheckBox, vuePercentField, null);
        setupSkillCheckbox(frontendOtherCheckBox, frontendOtherPercentField, frontendOtherNameField);

        setupSkillCheckbox(sqlCheckBox, sqlPercentField, null);
        setupSkillCheckbox(mongoCheckBox, mongoPercentField, null);
        setupSkillCheckbox(databaseOtherCheckBox, databaseOtherPercentField, databaseOtherNameField);

        setupSkillCheckbox(mavenCheckBox, mavenPercentField, null);
        setupSkillCheckbox(gradleCheckBox, gradlePercentField, null);
        setupSkillCheckbox(buildOtherCheckBox, buildOtherPercentField, buildOtherNameField);

        setupSkillCheckbox(jenkinsCheckBox, jenkinsPercentField, null);
        setupSkillCheckbox(azureCheckBox, azurePercentField, null);
        setupSkillCheckbox(bambooCheckBox, bambooPercentField, null);
        setupSkillCheckbox(cicdOtherCheckBox, cicdOtherPercentField, cicdOtherNameField);


        // 4. CV-Wahl-Logik
        cvChoiceCombo.setOnAction(e -> {
            String selected = cvChoiceCombo.getValue();
            uploadCvButton.setVisible("CV hochladen".equals(selected));
            cvByEmailLabel.setVisible("CV per E-Mail".equals(selected));
        });

        // 5. Optionaler Link sichtbar machen
        cvLinkToggle.setOnAction(e ->
                documentCvField.setVisible(cvLinkToggle.isSelected())
        );

        if (scrollPane == null) {
            System.out.println("ScrollPane ist null! Prüfe FXML-Zuweisung.");
        } else {
            System.out.println("ScrollPane wurde erfolgreich geladen.");
        }
    }


    @FXML
    private void handleAddCustomSkill(ActionEvent event) {
        System.out.println("➕ Skill hinzufügen gedrückt!");

        TextField newSkillTechnologyField = new TextField();
        newSkillTechnologyField.setPromptText("Technologie");

        TextField newSkillField = new TextField();
        newSkillField.setPromptText("weiterer Skill");

        TextField percentageField = new TextField();
        percentageField.setPromptText("Kenntnisgrad (%)");

        HBox skillEntry = new HBox(10);
        skillEntry.getChildren().addAll(newSkillTechnologyField, newSkillField, percentageField);

        Button deleteButton = new Button("Löschen");
        deleteButton.setOnAction(e -> {
            // Temporär ScrollPane deaktivieren
            scrollPane.setDisable(true);

            customSkillsContainer.getChildren().remove(skillEntry);

            Platform.runLater(() -> {
                scrollPane.setDisable(false);
                // Wiederherstellen der Scroll-Position auf zuletzt bekannten Bereich
                scrollPane.setVvalue(1); // oder gezielt ein Wert wie vorher
            });
        });


        skillEntry.getChildren().add(deleteButton);
        customSkillsContainer.getChildren().add(skillEntry);
    }










    private void setupSkillCheckbox(CheckBox checkBox, TextField nameField, TextField percentField) {
        if (percentField != null) {
            percentField.setVisible(false);
        }
        if (nameField != null) {
            nameField.setVisible(false);
        }

        checkBox.setOnAction(e -> {
            boolean selected = checkBox.isSelected();
            if (percentField != null) {
                percentField.setVisible(selected);
                System.out.println("percentField sichtbar: " + selected);
            }
            if (nameField != null) {
                nameField.setVisible(selected);
                System.out.println("nameField sichtbar: " + selected);
            }
        });



    }





    @FXML
    private void handleBrowseCV(ActionEvent event) {
        browseAndUploadFile("CV auswählen", documentCvField, filename -> uploadedCvFilename = filename);
    }

    private void browseAndUploadFile(String dialogTitle, TextField targetField, Consumer<String> onUploadComplete) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(dialogTitle);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            MultipartUtils.uploadFile(selectedFile, savedFilename -> Platform.runLater(() -> {
                targetField.setText(savedFilename);
                onUploadComplete.accept(savedFilename);
            }));
        }
    }

    @FXML
    private void handleSaveRecommendation(ActionEvent event) {
        if (!isInputValid()) return;

        RecommendationRequestDTO dto = createRecommendationDTO();

        backendService.submitRecommendation(dto).thenAccept(response -> {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                Platform.runLater(() -> {
                    DialogUtil.showInfo("Erfolg", "Empfehlung erfolgreich gespeichert!");
                    SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml");
                });
            } else {
                Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler beim Speichern!"));
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler bei der Anfrage!"));
            return null;
        });
    }

    private boolean isInputValid() {
        if (candidateFirstnameField.getText().trim().isEmpty() ||
                candidateLastnameField.getText().trim().isEmpty() ||
                positionField.getValue() == null || positionField.getValue().trim().isEmpty()) {
            DialogUtil.showError("Validierungsfehler", "Bitte alle Pflichtfelder ausfüllen.");
            return false;
        }

        if (uploadedCvFilename == null) {
            DialogUtil.showError("Validierungsfehler", "Bitte das CV-Dokument hochladen.");
            return false;
        }

        return true;
    }

    private RecommendationRequestDTO createRecommendationDTO() {
        RecommendationRequestDTO dto = new RecommendationRequestDTO();
        dto.setUserId(UserSession.getInstance().getUserId());
        dto.setCandidateFirstname(candidateFirstnameField.getText().trim());
        dto.setCandidateLastname(candidateLastnameField.getText().trim());
        dto.setPosition(positionField.getValue().trim());
        dto.setDocumentCvPath(uploadedCvFilename);
        return dto;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml");
    }

}

