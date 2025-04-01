package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    // Skill checkboxes and percentage fields
    @FXML private VBox customSkillsContainer;
    @FXML private Button addSkillButton;

    // Backend skills
    @FXML private CheckBox javaCheckBox;
    @FXML private TextField javaPercentField;
    @FXML private CheckBox springCheckBox;
    @FXML private TextField springPercentField;
    @FXML private CheckBox backendOtherCheckBox;
    @FXML private TextField backendOtherPercentField;
    @FXML private TextField backendOtherNameField;

    // Frontend skills
    @FXML private CheckBox angularCheckBox;
    @FXML private TextField angularPercentField;
    @FXML private CheckBox reactCheckBox;
    @FXML private TextField reactPercentField;
    @FXML private CheckBox vueCheckBox;
    @FXML private TextField vuePercentField;
    @FXML private CheckBox frontendOtherCheckBox;
    @FXML private TextField frontendOtherPercentField;
    @FXML private TextField frontendOtherNameField;

    // Database skills
    @FXML private CheckBox sqlCheckBox;
    @FXML private TextField sqlPercentField;
    @FXML private CheckBox mongoCheckBox;
    @FXML private TextField mongoPercentField;
    @FXML private CheckBox databaseOtherCheckBox;
    @FXML private TextField databaseOtherPercentField;
    @FXML private TextField databaseOtherNameField;

    // Build skills
    @FXML private CheckBox mavenCheckBox;
    @FXML private TextField mavenPercentField;
    @FXML private CheckBox gradleCheckBox;
    @FXML private TextField gradlePercentField;
    @FXML private CheckBox buildOtherCheckBox;
    @FXML private TextField buildOtherPercentField;
    @FXML private TextField buildOtherNameField;

    // CI/CD skills
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
    @FXML private TextArea hobbiesField;
    @FXML private TextArea projectExperienceField;
    @FXML private TextArea miscellaneousField;
    @FXML private Button submitButton;

    private final BackendService backendService = new BackendService();
    private final FormValidator formValidator = new FormValidator();
    private String uploadedCvFilename;

    // Service classes
    private SkillFieldManager skillFieldManager;
    private DateValidators dateValidator;
    private FormBuilder formBuilder;

    @FXML
    private void initialize() {
        initializeRequiredFields();
        initializeServices();
        initializeUIComponents();
        FocusTraversHelper.cancelFocusTravers(scrollPane.getContent());
    }

    private void initializeRequiredFields() {
        formValidator.addRequiredTextField(candidateFirstnameField);
        formValidator.addRequiredTextField(candidateLastnameField);
        formValidator.addRequiredComboBox(positionField);
    }

    private void initializeServices() {
        skillFieldManager = new SkillFieldManager();
        dateValidator = new DateValidators();
        formBuilder = new FormBuilder(customSkillsContainer, scrollPane);
    }

    private void initializeUIComponents() {
        initializeValidators();
        initializeSkillFields();
        initializeComboBoxes();
        initializeDatePickers();
    }

    private void initializeValidators() {
        // Setup validators for numeric fields
        formValidator.setupDecimalField(experienceYearsField, 0.0, 99.9, "Berufserfahrung in Jahren");
        formValidator.setupNumericField(salaryExpectationField, 1, 500000, "Gehalt");
        formValidator.setupNumericField(travelWillingnessField, 0, 100, "Reisebereitschaft");
        formValidator.setupEmailField(emailField, emailFeedbackLabel);
    }

    private void initializeSkillFields() {
        Map<CheckBox, TextField[]> skillFields = createSkillFieldsMap();

        skillFields.forEach((checkbox, fields) -> {
            skillFieldManager.setupSkillCheckbox(checkbox, fields[0], fields[1]);
            if (fields[0] != null) {
                formValidator.setupNumericField(fields[0], 0, 100, "Kenntnisgrad (%)");
            }
        });
    }

    private Map<CheckBox, TextField[]> createSkillFieldsMap() {
        Map<CheckBox, TextField[]> skillFields = new HashMap<>();

        // Map all skill checkboxes to their corresponding percentage and name fields
        addSkillMapping(skillFields, javaCheckBox, javaPercentField, null);
        addSkillMapping(skillFields, springCheckBox, springPercentField, null);
        addSkillMapping(skillFields, backendOtherCheckBox, backendOtherPercentField, backendOtherNameField);

        addSkillMapping(skillFields, angularCheckBox, angularPercentField, null);
        addSkillMapping(skillFields, reactCheckBox, reactPercentField, null);
        addSkillMapping(skillFields, vueCheckBox, vuePercentField, null);
        addSkillMapping(skillFields, frontendOtherCheckBox, frontendOtherPercentField, frontendOtherNameField);

        addSkillMapping(skillFields, sqlCheckBox, sqlPercentField, null);
        addSkillMapping(skillFields, mongoCheckBox, mongoPercentField, null);
        addSkillMapping(skillFields, databaseOtherCheckBox, databaseOtherPercentField, databaseOtherNameField);

        addSkillMapping(skillFields, mavenCheckBox, mavenPercentField, null);
        addSkillMapping(skillFields, gradleCheckBox, gradlePercentField, null);
        addSkillMapping(skillFields, buildOtherCheckBox, buildOtherPercentField, buildOtherNameField);

        addSkillMapping(skillFields, jenkinsCheckBox, jenkinsPercentField, null);
        addSkillMapping(skillFields, azureCheckBox, azurePercentField, null);
        addSkillMapping(skillFields, bambooCheckBox, bambooPercentField, null);
        addSkillMapping(skillFields, cicdOtherCheckBox, cicdOtherPercentField, cicdOtherNameField);

        return skillFields;
    }

    private void addSkillMapping(Map<CheckBox, TextField[]> map, CheckBox checkbox, TextField percentField, TextField nameField) {
        map.put(checkbox, new TextField[]{percentField, nameField});
    }

    private void initializeComboBoxes() {
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
        dateValidator.setupDateValidation(contactDatePicker, "Erstkontakt-Datum", true);
        dateValidator.setupDateValidation(convincedCandidateDatePicker, "Überzeugt-Datum", true);
        dateValidator.setupDateValidation(noticePeriodDatePicker, "Kündigungsfrist", false);
        dateValidator.setupDateValidation(startDatePicker, "Startdatum", false);

        // Setup date dependencies
        dateValidator.setupDateDependency(contactDatePicker, convincedCandidateDatePicker,
                "Kontakt-Datum", "Überzeugt-Datum");
        dateValidator.setupDateDependency(contactDatePicker, startDatePicker,
                "Kontakt-Datum", "Startdatum");
        dateValidator.setupDateDependency(noticePeriodDatePicker, startDatePicker,
                "Kündigungsfrist", "Startdatum");
    }

    @FXML
    private void handleAddCustomSkill(ActionEvent event) {
        formBuilder.addCustomSkill();
    }

    @FXML
    private void handleBrowseCV(ActionEvent event) {
        File selectedFile = selectCVFile();
        if (selectedFile != null) {
            uploadCV(selectedFile);
        }
    }

    private File selectCVFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CV auswählen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return fileChooser.showOpenDialog(null);
    }

    private void uploadCV(File file) {
        MultipartUtils.uploadFile(file, savedFilename -> Platform.runLater(() -> {
            documentCvField.setText(savedFilename);
            uploadedCvFilename = savedFilename;
        }));
    }

    @FXML
    private void handleSaveRecommendation(ActionEvent event) {
        if (!formValidator.validateForm(scrollPane)) {
            return;
        }

        RecommendationRequestDTO dto = createRecommendationDTO();
        submitRecommendation(event, dto);
    }

    private void submitRecommendation(ActionEvent event, RecommendationRequestDTO dto) {
        backendService.submitRecommendation(dto)
                .thenApply(response -> new HttpResponse(response.statusCode(), response.body()))
                .thenAccept(response -> handleSubmissionResponse(event, response))
                .exceptionally(e -> handleSubmissionError(e));
    }

    private void handleSubmissionResponse(ActionEvent event, HttpResponse response) {
        if (response.isSuccess()) {
            Platform.runLater(() -> {
                DialogUtil.showConfirmation("Erfolg", "Empfehlung erfolgreich gespeichert!");
                SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml", 0.8);
            });
        } else {
            Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler beim Speichern!"));
        }
    }

    private Void handleSubmissionError(Throwable e) {
        e.printStackTrace();
        Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler bei der Anfrage!"));
        return null;
    }

    private RecommendationRequestDTO createRecommendationDTO() {
        RecommendationRequestDTO dto = new RecommendationRequestDTO();

        setBasicInfo(dto);
        setEmploymentInfo(dto);
        setInformationStatus(dto);
        setCareerDetails(dto);
        setCVDetails(dto);
        setAdditionalInfo(dto);

        return dto;
    }

    private void setBasicInfo(RecommendationRequestDTO dto) {
        dto.setUserId(UserSession.getInstance().getUserId());
        dto.setCandidateFirstname(candidateFirstnameField.getText().trim());
        dto.setCandidateLastname(candidateLastnameField.getText().trim());
        dto.setEmail(emailField.getText().trim());
        dto.setPhone(phoneField.getText().trim());
        dto.setKnownFrom(knownFromField.getText().trim());
        dto.setContactDate(contactDatePicker.getValue());
        dto.setConvincedCandidateDate(convincedCandidateDatePicker.getValue());
    }

    private void setEmploymentInfo(RecommendationRequestDTO dto) {
        dto.setEmploymentStatus(employmentStatusCombo.getValue());
        dto.setCurrentPosition(currentPositionField.getText().trim());
        dto.setCurrentCareerLevel(currentCareerLevelField.getText().trim());
        dto.setLastPosition(lastPositionField.getText().trim());
        dto.setLastCareerLevel(lastCareerLevelField.getText().trim());
    }

    private void setInformationStatus(RecommendationRequestDTO dto) {
        dto.setInformedPosition(informedPositionCheck.isSelected());
        dto.setInformedTasks(informedTasksCheck.isSelected());
        dto.setInformedRequirements(informedRequirementsCheck.isSelected());
        dto.setInformedClientsProjects(informedClientsProjectsCheck.isSelected());
        dto.setInformedBenefits(informedBenefitsCheck.isSelected());
        dto.setInformedTraining(informedTrainingCheck.isSelected());
        dto.setInformedCoach(informedCoachCheck.isSelected());
        dto.setInformedRoles(informedRolesCheck.isSelected());
    }

    private void setCareerDetails(RecommendationRequestDTO dto) {
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
    }

    private void setCVDetails(RecommendationRequestDTO dto) {
        dto.setCvChoice(cvChoiceCombo.getValue());
        dto.setDocumentCvPath(uploadedCvFilename);
        dto.setCvLink(documentCvField.getText().trim());
    }

    private void setAdditionalInfo(RecommendationRequestDTO dto) {
        dto.setPersonalityType(personalityTypArea.getText().trim());
        dto.setHobbies(hobbiesField.getText().trim());
        dto.setProjectExperience(projectExperienceField.getText().trim());
        dto.setMiscellaneous(miscellaneousField.getText().trim());
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml", 0.8);
    }
}