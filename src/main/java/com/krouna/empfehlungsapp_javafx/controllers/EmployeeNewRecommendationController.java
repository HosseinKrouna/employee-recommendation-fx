package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.*;
import com.krouna.empfehlungsapp_javafx.util.FieldValidators;
import com.krouna.empfehlungsapp_javafx.util.DateValidators;
import com.krouna.empfehlungsapp_javafx.util.SkillFieldManager;
import com.krouna.empfehlungsapp_javafx.util.FormBuilder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EmployeeNewRecommendationController {

    // Basic candidate information fields
    @FXML private TextField candidateFirstnameField;
    @FXML private TextField candidateLastnameField;
    @FXML private TextField emailField;
    @FXML private Label emailFeedbackLabel;
    @FXML private TextField phoneField;
    @FXML private TextArea knownFromField;
    @FXML private DatePicker contactDatePicker;
    @FXML private DatePicker convincedCandidateDatePicker;
    @FXML private ScrollPane scrollPane;

    // Employment status fields
    @FXML private ComboBox<String> employmentStatusCombo;
    @FXML private VBox currentPositionBox;
    @FXML private TextField currentPositionField;
    @FXML private TextField currentCareerLevelField;
    @FXML private VBox lastPositionBox;
    @FXML private TextField lastPositionField;
    @FXML private TextField lastCareerLevelField;

    // Information checkboxes
    @FXML private CheckBox informedPositionCheck;
    @FXML private CheckBox informedTasksCheck;
    @FXML private CheckBox informedRequirementsCheck;
    @FXML private CheckBox informedClientsProjectsCheck;
    @FXML private CheckBox informedBenefitsCheck;
    @FXML private CheckBox informedTrainingCheck;
    @FXML private CheckBox informedCoachCheck;
    @FXML private CheckBox informedRolesCheck;

    // Career fields
    @FXML private TextField experienceYearsField;
    @FXML private ComboBox<String> positionField;
    @FXML private DatePicker noticePeriodDatePicker;
    @FXML private DatePicker startDatePicker;
    @FXML private TextField salaryExpectationField;
    @FXML private TextField workHoursField;
    @FXML private TextField travelWillingnessField;

    // CV fields
    @FXML private ComboBox<String> cvChoiceCombo;
    @FXML private Button uploadCvButton;
    @FXML private Label cvByEmailLabel;
    @FXML private CheckBox cvLinkToggle;
    @FXML private TextField documentCvField;
    @FXML private TextArea personalityTypArea;

    // Skill checkboxes and percentage fields (organized by type)
    // Backend
    @FXML private CheckBox javaCheckBox;
    @FXML private TextField javaPercentField;
    @FXML private CheckBox springCheckBox;
    @FXML private TextField springPercentField;
    @FXML private CheckBox backendOtherCheckBox;
    @FXML private TextField backendOtherPercentField;
    @FXML private TextField backendOtherNameField;

    // Frontend
    @FXML private CheckBox angularCheckBox;
    @FXML private TextField angularPercentField;
    @FXML private CheckBox reactCheckBox;
    @FXML private TextField reactPercentField;
    @FXML private CheckBox vueCheckBox;
    @FXML private TextField vuePercentField;
    @FXML private CheckBox frontendOtherCheckBox;
    @FXML private TextField frontendOtherPercentField;
    @FXML private TextField frontendOtherNameField;

    // Database
    @FXML private CheckBox sqlCheckBox;
    @FXML private TextField sqlPercentField;
    @FXML private CheckBox mongoCheckBox;
    @FXML private TextField mongoPercentField;
    @FXML private CheckBox databaseOtherCheckBox;
    @FXML private TextField databaseOtherPercentField;
    @FXML private TextField databaseOtherNameField;

    // Build
    @FXML private CheckBox mavenCheckBox;
    @FXML private TextField mavenPercentField;
    @FXML private CheckBox gradleCheckBox;
    @FXML private TextField gradlePercentField;
    @FXML private CheckBox buildOtherCheckBox;
    @FXML private TextField buildOtherPercentField;
    @FXML private TextField buildOtherNameField;

    // CI/CD
    @FXML private CheckBox jenkinsCheckBox;
    @FXML private TextField jenkinsPercentField;
    @FXML private CheckBox azureCheckBox;
    @FXML private TextField azurePercentField;
    @FXML private CheckBox bambooCheckBox;
    @FXML private TextField bambooPercentField;
    @FXML private CheckBox cicdOtherCheckBox;
    @FXML private TextField cicdOtherPercentField;
    @FXML private TextField cicdOtherNameField;

    // Additional fields
    @FXML private VBox customSkillsContainer;
    @FXML private Button addSkillButton;
    @FXML private TextArea hobbiesField;
    @FXML private TextArea projectExperienceField;
    @FXML private TextArea miscellaneousField;
    @FXML private Button submitButton;

    private final BackendService backendService = new BackendService();
    private String uploadedCvFilename;

    // Service classes
    private SkillFieldManager skillFieldManager;
    private DateValidators dateValidators;
    private FieldValidators fieldValidators;
    private FormBuilder formBuilder;

    @FXML
    private void initialize() {
        // Initialize service classes
        skillFieldManager = new SkillFieldManager();
        dateValidators = new DateValidators();
        fieldValidators = new FieldValidators();
        formBuilder = new FormBuilder(customSkillsContainer, scrollPane);

        initializeValidators();
        initializeSkillFields();
        initializeUIControls();
        initializeDatePickers();
        initializeEmailValidator();

        FocusTraversHelper.cancelFocusTravers(scrollPane.getContent());
    }

    private void initializeValidators() {
        // Setup validators for numeric fields
        fieldValidators.setupDecimalField(experienceYearsField, 0.0, 99.9, "Berufserfahrung in Jahren");
        fieldValidators.setupNumericField(salaryExpectationField, 1, 500000, "Gehalt");
        fieldValidators.setupNumericField(travelWillingnessField, 0, 100, "Reisebereitschaft");
    }

    private void initializeEmailValidator() {
        fieldValidators.setupEmailField(emailField, emailFeedbackLabel);
    }

    private void initializeSkillFields() {
        // Create mapping of skill fields
        Map<CheckBox, TextField[]> skillFields = createSkillFieldsMap();

        // Initialize all skill fields
        skillFields.forEach((checkbox, fields) -> {
            skillFieldManager.setupSkillCheckbox(checkbox, fields[0], fields[1]);
            if (fields[0] != null) {
                fieldValidators.setupNumericField(fields[0], 0, 100, "Kenntnisgrad (%)");
            }
        });
    }

    private Map<CheckBox, TextField[]> createSkillFieldsMap() {
        Map<CheckBox, TextField[]> skillFields = new HashMap<>();

        // Backend skills
        skillFields.put(javaCheckBox, new TextField[]{javaPercentField, null});
        skillFields.put(springCheckBox, new TextField[]{springPercentField, null});
        skillFields.put(backendOtherCheckBox, new TextField[]{backendOtherPercentField, backendOtherNameField});

        // Frontend skills
        skillFields.put(angularCheckBox, new TextField[]{angularPercentField, null});
        skillFields.put(reactCheckBox, new TextField[]{reactPercentField, null});
        skillFields.put(vueCheckBox, new TextField[]{vuePercentField, null});
        skillFields.put(frontendOtherCheckBox, new TextField[]{frontendOtherPercentField, frontendOtherNameField});

        // Database skills
        skillFields.put(sqlCheckBox, new TextField[]{sqlPercentField, null});
        skillFields.put(mongoCheckBox, new TextField[]{mongoPercentField, null});
        skillFields.put(databaseOtherCheckBox, new TextField[]{databaseOtherPercentField, databaseOtherNameField});

        // Build skills
        skillFields.put(mavenCheckBox, new TextField[]{mavenPercentField, null});
        skillFields.put(gradleCheckBox, new TextField[]{gradlePercentField, null});
        skillFields.put(buildOtherCheckBox, new TextField[]{buildOtherPercentField, buildOtherNameField});

        // CI/CD skills
        skillFields.put(jenkinsCheckBox, new TextField[]{jenkinsPercentField, null});
        skillFields.put(azureCheckBox, new TextField[]{azurePercentField, null});
        skillFields.put(bambooCheckBox, new TextField[]{bambooPercentField, null});
        skillFields.put(cicdOtherCheckBox, new TextField[]{cicdOtherPercentField, cicdOtherNameField});

        return skillFields;
    }

    private void initializeUIControls() {
        // Setup position field
        positionField.getItems().addAll(
                "Junior Developer", "Mid-Level Developer", "Senior Developer", "Team Lead", "Architekt"
        );

        // Setup employment status combo box
        employmentStatusCombo.setOnAction(e -> {
            String status = employmentStatusCombo.getValue();
            boolean isEmployed = "In Anstellung".equals(status);
            UIUtils.setVisibilityAndManaged(currentPositionBox, isEmployed);
            UIUtils.setVisibilityAndManaged(lastPositionBox, !isEmployed);
        });

        // Setup CV choice combo box
        cvChoiceCombo.setOnAction(e -> {
            String selected = cvChoiceCombo.getValue();
            uploadCvButton.setVisible("CV hochladen".equals(selected));
            cvByEmailLabel.setVisible("CV per E-Mail".equals(selected));
        });

        // Setup CV link toggle
        cvLinkToggle.setOnAction(e -> documentCvField.setVisible(cvLinkToggle.isSelected()));
    }

    private void initializeDatePickers() {
        // Setup date validation
        dateValidators.setupDateValidation(contactDatePicker, "Erstkontakt-Datum", true);
        dateValidators.setupDateValidation(convincedCandidateDatePicker, "Überzeugt-Datum", true);
        dateValidators.setupDateValidation(noticePeriodDatePicker, "Kündigungsfrist", false);
        dateValidators.setupDateValidation(startDatePicker, "Startdatum", false);

        // Setup date dependencies
        dateValidators.setupDateDependency(contactDatePicker, convincedCandidateDatePicker,
                "Kontakt-Datum", "Überzeugt-Datum");
        dateValidators.setupDateDependency(contactDatePicker, startDatePicker,
                "Kontakt-Datum", "Startdatum");
        dateValidators.setupStartAndNoticeDependency(startDatePicker, noticePeriodDatePicker);
    }

    @FXML
    private void handleAddCustomSkill(ActionEvent event) {
        formBuilder.addCustomSkill();
    }

    @FXML
    private void handleBrowseCV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CV auswählen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            MultipartUtils.uploadFile(selectedFile, savedFilename -> Platform.runLater(() -> {
                documentCvField.setText(savedFilename);
                uploadedCvFilename = savedFilename;
            }));
        }
    }

    @FXML
    private void handleSaveRecommendation(ActionEvent event) {
        if (!isInputValid()) return;

        RecommendationRequestDTO dto = createRecommendationDTO();

        backendService.submitRecommendation(dto)
                .thenAccept(response -> {
                    if (response.statusCode() == 201 || response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            DialogUtil.showInfo("Erfolg", "Empfehlung erfolgreich gespeichert!");
                            SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml");
                        });
                    } else {
                        Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler beim Speichern!"));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler bei der Anfrage!"));
                    return null;
                });
    }

    private boolean isInputValid() {
        if (candidateFirstnameField.getText().trim().isEmpty() ||
                candidateLastnameField.getText().trim().isEmpty() ||
                positionField.getValue() == null ||
                positionField.getValue().trim().isEmpty()) {
            DialogUtil.showError("Validierungsfehler", "Bitte alle Pflichtfelder ausfüllen.");
            return false;
        }
        return true;
    }

    private RecommendationRequestDTO createRecommendationDTO() {
        RecommendationRequestDTO dto = new RecommendationRequestDTO();

        // Basic information
        dto.setUserId(UserSession.getInstance().getUserId());
        dto.setCandidateFirstname(candidateFirstnameField.getText().trim());
        dto.setCandidateLastname(candidateLastnameField.getText().trim());
        dto.setEmail(emailField.getText().trim());
        dto.setPhone(phoneField.getText().trim());
        dto.setKnownFrom(knownFromField.getText().trim());
        dto.setContactDate(contactDatePicker.getValue());
        dto.setConvincedCandidateDate(convincedCandidateDatePicker.getValue());

        // Employment status
        dto.setEmploymentStatus(employmentStatusCombo.getValue());
        dto.setCurrentPosition(currentPositionField.getText().trim());
        dto.setCurrentCareerLevel(currentCareerLevelField.getText().trim());
        dto.setLastPosition(lastPositionField.getText().trim());
        dto.setLastCareerLevel(lastCareerLevelField.getText().trim());

        // Information checkboxes
        dto.setInformedPosition(informedPositionCheck.isSelected());
        dto.setInformedTasks(informedTasksCheck.isSelected());
        dto.setInformedRequirements(informedRequirementsCheck.isSelected());
        dto.setInformedClientsProjects(informedClientsProjectsCheck.isSelected());
        dto.setInformedBenefits(informedBenefitsCheck.isSelected());
        dto.setInformedTraining(informedTrainingCheck.isSelected());
        dto.setInformedCoach(informedCoachCheck.isSelected());
        dto.setInformedRoles(informedRolesCheck.isSelected());

        // Career details
        String experienceText = experienceYearsField.getText().trim().replace(",", ".");
        dto.setExperienceYears(experienceText.isEmpty() ? null : Double.parseDouble(experienceText));
        dto.setPosition(positionField.getValue());
        dto.setNoticePeriod(noticePeriodDatePicker.getValue());
        dto.setStartDate(startDatePicker.getValue());

        String salaryText = salaryExpectationField.getText().trim();
        dto.setSalaryExpectation(salaryText.isEmpty() ? null : Integer.parseInt(salaryText));
        dto.setWorkHours(workHoursField.getText().trim());

        String travelText = travelWillingnessField.getText().trim();
        dto.setTravelWillingness(travelText.isEmpty() ? null : Integer.parseInt(travelText));

        // CV and additional information
        dto.setCvChoice(cvChoiceCombo.getValue());
        dto.setDocumentCvPath(uploadedCvFilename);
        dto.setCvLink(documentCvField.getText().trim());
        dto.setPersonalityType(personalityTypArea.getText().trim());
        dto.setHobbies(hobbiesField.getText().trim());
        dto.setProjectExperience(projectExperienceField.getText().trim());
        dto.setMiscellaneous(miscellaneousField.getText().trim());

        return dto;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml", 0.8);
    }
}