package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.*; // Importiere alle Utils
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button; // Explizite Imports sind ok, * nicht zwingend
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop; // Desktop für Datei-Öffnen
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeNewRecommendationController {




    @FXML private TextField candidateFirstnameField;
    @FXML private TextField candidateLastnameField;
    @FXML private TextField emailField;
    @FXML private Label emailFeedbackLabel;
    @FXML private TextField phoneField;
    @FXML private TextArea knownFromField;
    @FXML private DatePicker contactDatePicker;
    @FXML private DatePicker convincedCandidateDatePicker;
    @FXML private ScrollPane scrollPane;


    @FXML private ComboBox<String> employmentStatusCombo;
    @FXML private VBox currentPositionBox;
    @FXML private TextField currentPositionField;
    @FXML private TextField currentCareerLevelField;
    @FXML private VBox lastPositionBox;
    @FXML private TextField lastPositionField;
    @FXML private TextField lastCareerLevelField;


    @FXML private CheckBox informedPositionCheck;
    @FXML private CheckBox informedTasksCheck;
    @FXML private CheckBox informedRequirementsCheck;
    @FXML private CheckBox informedClientsProjectsCheck;
    @FXML private CheckBox informedBenefitsCheck;
    @FXML private CheckBox informedTrainingCheck;
    @FXML private CheckBox informedCoachCheck;
    @FXML private CheckBox informedRolesCheck;


    @FXML private TextField experienceYearsField;
    @FXML private ComboBox<String> positionField;
    @FXML private DatePicker noticePeriodDatePicker;
    @FXML private DatePicker startDatePicker;
    @FXML private TextField salaryExpectationField;
    @FXML private TextField workHoursField;
    @FXML private TextField travelWillingnessField;


    @FXML private ComboBox<String> cvChoiceCombo;
    @FXML private Button uploadCvButton;
    @FXML private HBox cvPreviewBox;
    @FXML private ImageView cvIcon;
    @FXML private Hyperlink cvLink;
    @FXML private Label cvByEmailLabel;
    @FXML private Label cvByBusinessLink;
    @FXML private CheckBox businessLinkToggle;
    @FXML private TextField businessLinkField;


    // Personality and other fields
    @FXML private TextArea personalityTypArea;
    @FXML private TextArea hobbiesField;
    @FXML private TextArea projectExperienceField;
    @FXML private TextArea miscellaneousField;
    @FXML private Button submitButton;

    // Skill fields
    @FXML private VBox customSkillsContainer;
    @FXML private Button addSkillButton;
    @FXML private CheckBox javaCheckBox;
    @FXML private TextField javaPercentField;
    @FXML private CheckBox springCheckBox;
    @FXML private TextField springPercentField;
    @FXML private CheckBox backendOtherCheckBox;
    @FXML private TextField backendOtherPercentField;
    @FXML private TextField backendOtherNameField;

    @FXML private CheckBox angularCheckBox;
    @FXML private TextField angularPercentField;
    @FXML private CheckBox reactCheckBox;
    @FXML private TextField reactPercentField;
    @FXML private CheckBox vueCheckBox;
    @FXML private TextField vuePercentField;
    @FXML private CheckBox frontendOtherCheckBox;
    @FXML private TextField frontendOtherPercentField;
    @FXML private TextField frontendOtherNameField;
    @FXML private CheckBox sqlCheckBox;
    @FXML private TextField sqlPercentField;
    @FXML private CheckBox mongoCheckBox;
    @FXML private TextField mongoPercentField;
    @FXML private CheckBox databaseOtherCheckBox;
    @FXML private TextField databaseOtherPercentField;
    @FXML private TextField databaseOtherNameField;
    @FXML private CheckBox mavenCheckBox;
    @FXML private TextField mavenPercentField;
    @FXML private CheckBox gradleCheckBox;
    @FXML private TextField gradlePercentField;
    @FXML private CheckBox buildOtherCheckBox;
    @FXML private TextField buildOtherPercentField;
    @FXML private TextField buildOtherNameField;
    @FXML private CheckBox jenkinsCheckBox;
    @FXML private TextField jenkinsPercentField;
    @FXML private CheckBox azureCheckBox;
    @FXML private TextField azurePercentField;
    @FXML private CheckBox bambooCheckBox;
    @FXML private TextField bambooPercentField;
    @FXML private CheckBox cicdOtherCheckBox;
    @FXML private TextField cicdOtherPercentField;
    @FXML private TextField cicdOtherNameField;



    private final BackendService backendService = new BackendService();
    private final FormValidator formValidator = new FormValidator();
    private String uploadedCvFilename;


    private SkillFieldManager skillFieldManager;
    private DateValidators dateValidator;
    private FormBuilder formBuilder;



    @FXML
    private void initialize() {
        initializeRequiredFields();
        initializeServices();
        initializeUIComponents();
        FocusTraversHelper.cancelFocusTravers(scrollPane.getContent());

        addBusinessLinkListener();
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

        formValidator.addDecimalRangeValidation(experienceYearsField, 0.0, 99.9, "Berufserfahrung",false);
        formValidator.setupNumericFieldWithLiveRangeCheck(salaryExpectationField, 1, 500000, "Gehalt", false);
        formValidator.setupNumericFieldWithLiveRangeCheck(travelWillingnessField, 0, 100, "Reisebereitschaft", false);
        formValidator.setupEmailField(emailField, emailFeedbackLabel);

    }

    private void initializeSkillFields() {

        Map<CheckBox, TextField[]> skillFields = createSkillFieldsMap();
        skillFields.forEach((checkbox, fields) -> {
            skillFieldManager.setupSkillCheckbox(checkbox, fields[0], fields[1]);
            if (fields[0] != null) {

                formValidator.setupNumericFieldWithLiveRangeCheck(fields[0], 0, 100, "Kenntnisgrad (%) für " + checkbox.getText(), false);
            }
        });
    }

    private Map<CheckBox, TextField[]> createSkillFieldsMap() {

        Map<CheckBox, TextField[]> skillFields = new HashMap<>();
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

        positionField.getItems().addAll(
                "Junior Developer", "Mid-Level Developer", "Senior Developer", "Team Lead", "Architekt"
        );


        employmentStatusCombo.setOnAction(e -> {
            String status = employmentStatusCombo.getValue();
            boolean isEmployed = "In Anstellung".equals(status);
            UIUtils.setVisibilityAndManaged(currentPositionBox, isEmployed);
            UIUtils.setVisibilityAndManaged(lastPositionBox, !isEmployed);
        });


        cvChoiceCombo.setOnAction(e -> {
            String selected = cvChoiceCombo.getValue();
            boolean isUpload = "CV hochladen".equals(selected);
            boolean isEmail = "CV per E-Mail".equals(selected);
            boolean isBusinessLinkCv = "CV im Business-Profil-Link enthalten".equals(selected);

            uploadCvButton.setVisible(isUpload);
            cvByEmailLabel.setVisible(isEmail);
            cvByBusinessLink.setVisible(isBusinessLinkCv);


            businessLinkToggle.setVisible(!isBusinessLinkCv);

            businessLinkField.setVisible(isBusinessLinkCv || businessLinkToggle.isSelected());

            if (!isUpload) {
                clearCvUpload();
            }
        });


        businessLinkToggle.setOnAction(e -> businessLinkField.setVisible(businessLinkToggle.isSelected()));

        businessLinkField.setVisible(businessLinkToggle.isSelected());
    }

    private void initializeDatePickers() {

        dateValidator.setupFutureDateValidation(contactDatePicker, "Erstkontakt-Datum", true);

        dateValidator.setupFutureDateValidation(convincedCandidateDatePicker, "Überzeugt-Datum", true);

        dateValidator.setupFutureDateValidation(noticePeriodDatePicker, "Kündigungsfrist", false);

        dateValidator.setupFutureDateValidation(startDatePicker, "Startdatum", false);


        dateValidator.setupDateDependency(contactDatePicker, convincedCandidateDatePicker, "Kontakt-Datum", "Überzeugt-Datum");

        dateValidator.setupDateDependency(contactDatePicker, startDatePicker, "Kontakt-Datum", "Startdatum");

        dateValidator.setupDateDependency(noticePeriodDatePicker, startDatePicker, "Kündigungsfrist", "Startdatum");
    }


    // --- Event Handlers ---

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
        fileChooser.setTitle("Lebenslauf (CV) auswählen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Dokumente", "*.pdf"));

        return fileChooser.showOpenDialog(null);
    }

    private void uploadCV(File file) {

        MultipartUtils.uploadFile(file, savedFilename -> {
            if (savedFilename != null && !savedFilename.isBlank()) {
                Platform.runLater(() -> {
                    uploadedCvFilename = savedFilename;
                    showCvPreview(file.getName());
                });
            } else {

                Platform.runLater(() -> DialogUtil.showError("Upload Fehler", "Datei konnte nicht hochgeladen werden."));
                clearCvUpload();
            }
        }, error -> {

            Platform.runLater(() -> DialogUtil.showError("Upload Fehler", "Fehler: " + error));
            clearCvUpload();
        });
    }

    private void showCvPreview(String originalFileName) {

        cvPreviewBox.setVisible(true);

        try {
            cvIcon.setImage(new Image(getClass().getResourceAsStream("/images/pdf-icon.png"), 18, 18, true, true));
        } catch (Exception e) {
            System.err.println("PDF Icon nicht gefunden!");

        }
        cvLink.setText(originalFileName);
    }

    @FXML
    private void handleOpenUploadedCV(ActionEvent event) {

        if (uploadedCvFilename != null && !uploadedCvFilename.isBlank()) {
            // TODO: Implementiere Vorschau über FileDownloadService.previewFile(uploadedCvFilename)
            DialogUtil.showInfo("Info", "Vorschau-Funktion noch nicht implementiert.\nGespeicherter Pfad: " + uploadedCvFilename);

        } else {
            DialogUtil.showInfo("Keine Datei", "Es wurde noch kein Lebenslauf hochgeladen.");
        }
    }


    @FXML
    private void handleRemoveCVPreview(ActionEvent event) {

        clearCvUpload();
    }

    private void clearCvUpload() {

        uploadedCvFilename = null;
        cvPreviewBox.setVisible(false);
        cvLink.setText("");

    }


    private void addBusinessLinkListener() {

        businessLinkToggle.selectedProperty().addListener((obs, ov, nv) -> handleBusinessLinkToggleChange());

        businessLinkField.textProperty().addListener((obs, ov, nv) -> handleBusinessLinkToggleChange());
    }


    private void handleBusinessLinkToggleChange() {
        boolean toggleSelected = businessLinkToggle.isSelected();
        businessLinkField.setVisible(toggleSelected);


        if (toggleSelected) {

            formValidator.validateOptionalBusinessLinkInternal(businessLinkField, businessLinkToggle, "Business-Profil-Link");
        } else {

            formValidator.removeErrorStyle(businessLinkField);
        }
    }


    @FXML
    private void handleSaveRecommendation(ActionEvent event) {

        boolean isFormValid = formValidator.validateForm(
                scrollPane,
                businessLinkField,
                businessLinkToggle,
                emailField

        );

        if (!isFormValid) {

            System.out.println("Validierung fehlgeschlagen.");
            return; // Abbrechen
        }


        System.out.println("Validierung erfolgreich.");
        RecommendationRequestDTO dto = createRecommendationDTO();
        submitRecommendation(event, dto);
    }

    private void submitRecommendation(ActionEvent event, RecommendationRequestDTO dto) {

        backendService.submitRecommendation(dto)
                .thenAccept(response -> handleSubmissionResponse(event, response))
                .exceptionally(e -> handleSubmissionError(e));
    }

    private void handleSubmissionResponse(ActionEvent event, HttpResponse response) {

        if (response.isSuccess()) {
            Platform.runLater(() -> {
                DialogUtil.showInfo("Erfolg", "Empfehlung erfolgreich gespeichert!");
                SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml", 0.8);
            });
        } else {
            Platform.runLater(() -> {
                String errorDetails = "Fehler beim Speichern!";
                if (response.getBody() != null && !response.getBody().isBlank()){
                    errorDetails += "\nServerantwort: " + response.getBody();
                } else {
                    errorDetails += "\nStatuscode: " + response.getStatusCode();
                }
                DialogUtil.showError("Fehler", errorDetails);
            });
        }
    }

    private Void handleSubmissionError(Throwable e) {

        e.printStackTrace();
        Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler bei der Anfrage: " + e.getMessage()));
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
        setSkillDetails(dto);
        return dto;
    }


    private void setBasicInfo(RecommendationRequestDTO dto) {

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
        if (currentPositionBox.isVisible()) {
            dto.setCurrentPosition(currentPositionField.getText().trim());
            dto.setCurrentCareerLevel(currentCareerLevelField.getText().trim());
        }
        if (lastPositionBox.isVisible()) {
            dto.setLastPosition(lastPositionField.getText().trim());
            dto.setLastCareerLevel(lastCareerLevelField.getText().trim());
        }
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
        try {
            dto.setExperienceYears(experienceText.isEmpty() ? null : Double.parseDouble(experienceText));
        } catch (NumberFormatException e) { dto.setExperienceYears(null);  }
        dto.setPosition(positionField.getValue());
        dto.setNoticePeriod(noticePeriodDatePicker.getValue());
        dto.setStartDate(startDatePicker.getValue());
        try {
            String salaryText = salaryExpectationField.getText().trim().replaceAll("[^\\d]", ""); // Nur Ziffern behalten
            dto.setSalaryExpectation(salaryText.isEmpty() ? null : Integer.parseInt(salaryText));
        } catch (NumberFormatException e) { dto.setSalaryExpectation(null); }
        dto.setWorkHours(workHoursField.getText().trim());
        try {
            String travelText = travelWillingnessField.getText().trim().replaceAll("[^\\d]", "");
            dto.setTravelWillingness(travelText.isEmpty() ? null : Integer.parseInt(travelText));
        } catch (NumberFormatException e) { dto.setTravelWillingness(null); }
    }

    private void setCVDetails(RecommendationRequestDTO dto) {

        String choice = cvChoiceCombo.getValue();
        dto.setCvChoice(choice);


        if ("CV hochladen".equals(choice) && uploadedCvFilename != null && !uploadedCvFilename.isBlank()) {
            dto.setDocumentCvPath(uploadedCvFilename);
        } else {
            dto.setDocumentCvPath(null);
        }


        if (businessLinkField.isVisible() && !businessLinkField.getText().trim().isEmpty()) {
            dto.setBusinessLink(businessLinkField.getText().trim());
        } else {
            dto.setBusinessLink(null);
        }
    }

    private void setAdditionalInfo(RecommendationRequestDTO dto) {
        dto.setPersonalityType(personalityTypArea.getText().trim());
        dto.setHobbies(hobbiesField.getText().trim());
        dto.setProjectExperience(projectExperienceField.getText().trim());
        dto.setMiscellaneous(miscellaneousField.getText().trim());
    }

    private void setSkillDetails(RecommendationRequestDTO dto) {

        dto.setBackendSkills(extractSkillsFromFields(
                new SkillInput(javaCheckBox, javaPercentField),
                new SkillInput(springCheckBox, springPercentField),
                new SkillInput(backendOtherCheckBox, backendOtherPercentField, backendOtherNameField)
        ));
        dto.setFrontendSkills(extractSkillsFromFields(
                new SkillInput(angularCheckBox, angularPercentField),
                new SkillInput(reactCheckBox, reactPercentField),
                new SkillInput(vueCheckBox, vuePercentField),
                new SkillInput(frontendOtherCheckBox, frontendOtherPercentField, frontendOtherNameField)
        ));
        dto.setDatabaseSkills(extractSkillsFromFields(
                new SkillInput(sqlCheckBox, sqlPercentField),
                new SkillInput(mongoCheckBox, mongoPercentField),
                new SkillInput(databaseOtherCheckBox, databaseOtherPercentField, databaseOtherNameField)
        ));
        dto.setBuildSkills(extractSkillsFromFields(
                new SkillInput(mavenCheckBox, mavenPercentField),
                new SkillInput(gradleCheckBox, gradlePercentField),
                new SkillInput(buildOtherCheckBox, buildOtherPercentField, buildOtherNameField)
        ));
        dto.setCicdSkills(extractSkillsFromFields(
                new SkillInput(jenkinsCheckBox, jenkinsPercentField),
                new SkillInput(azureCheckBox, azurePercentField),
                new SkillInput(bambooCheckBox, bambooPercentField),
                new SkillInput(cicdOtherCheckBox, cicdOtherPercentField, cicdOtherNameField)
        ));
        dto.setCustomSkills(extractCustomSkills());
    }

    private List<RecommendationRequestDTO.SkillEntry> extractSkillsFromFields(SkillInput... skillInputs) {

        List<RecommendationRequestDTO.SkillEntry> skills = new ArrayList<>();
        for (SkillInput input : skillInputs) {
            CheckBox checkBox = input.getCheckBox();
            TextField percentField = input.getPercentField();
            TextField nameField = input.getNameField();
            if (!checkBox.isSelected()) continue;

            String name = (nameField != null && nameField.isVisible() && !nameField.getText().isEmpty())
                    ? nameField.getText().trim()
                    : checkBox.getText();

            int percentage = 0;

            if (percentField != null && percentField.isVisible() && !percentField.getText().isEmpty()) {
                try {
                    percentage = Integer.parseInt(percentField.getText().trim());

                    percentage = Math.max(0, Math.min(100, percentage));
                } catch (NumberFormatException e) {
                    System.out.println("Ungültiger Prozentwert für Skill: " + name + ", setze auf 0.");
                }
            } else if (nameField != null && nameField.isVisible()){

                percentage = 0;
            }

            skills.add(new RecommendationRequestDTO.SkillEntry(name, percentage));
        }
        return skills;
    }

    private List<RecommendationRequestDTO.SkillEntry> extractCustomSkills() {

        List<RecommendationRequestDTO.SkillEntry> skills = new ArrayList<>();
        for (Node node : customSkillsContainer.getChildren()) {
            if (node instanceof HBox hbox && hbox.getChildren().size() >= 3) {

                TextField techField = (TextField) hbox.getChildren().get(0);
                TextField nameField = (TextField) hbox.getChildren().get(1);
                TextField percentField = (TextField) hbox.getChildren().get(2);

                String tech = techField.getText().trim();
                String name = nameField.getText().trim();
                String percentText = percentField.getText().trim();

                if (!name.isEmpty()) {
                    int percentage = 0;
                    if (!percentText.isEmpty()) {
                        try {
                            percentage = Integer.parseInt(percentText);
                            percentage = Math.max(0, Math.min(100, percentage));
                        } catch (NumberFormatException e) {
                            System.out.println("Ungültiger Prozentwert bei Custom-Skill: " + name + ", setze auf 0.");
                        }
                    }
                    skills.add(new RecommendationRequestDTO.SkillEntry(name, percentage, tech));
                }
            }
        }
        return skills;
    }



    @FXML
    private void handleBack(ActionEvent event) {

        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml", 0.8);
    }
}