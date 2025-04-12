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

    // --- FXML Felder ---

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

    // CV and Business Link fields
    @FXML private ComboBox<String> cvChoiceCombo;
    @FXML private Button uploadCvButton;
    @FXML private HBox cvPreviewBox;        // Zeigt Icon, Link, Löschen-Button für hochgeladene CV
    @FXML private ImageView cvIcon;
    @FXML private Hyperlink cvLink;
    @FXML private Label cvByEmailLabel;     // Info-Label für "CV per E-Mail"
    @FXML private Label cvByBusinessLink;   // Info-Label für "CV im Business-Profil"
    @FXML private CheckBox businessLinkToggle; // Checkbox, um Business-Link-Feld anzuzeigen/verbergen
    @FXML private TextField businessLinkField;  // Eingabefeld für Xing/LinkedIn etc.
    // Die Labels cvLinkWarningLabel, cvLinkValidationLabel waren spezifisch für business-link Validierung, füge sie hinzu falls benötigt
    // @FXML private Label cvLinkWarningLabel;
    // @FXML private Label cvLinkValidationLabel;

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
    // --- (Restliche Skill-Felder bleiben unverändert) ---
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


    // --- Service-Instanzen und Variablen ---
    private final BackendService backendService = new BackendService();
    private final FormValidator formValidator = new FormValidator(); // Stelle sicher, dass diese Klasse aktuell ist
    private String uploadedCvFilename; // Speichert den Namen der hochgeladenen Datei vom Backend

    // Service classes (angenommen, diese existieren und sind korrekt)
    private SkillFieldManager skillFieldManager;
    private DateValidators dateValidator;
    private FormBuilder formBuilder;


    // --- Initialisierung ---
    @FXML
    private void initialize() {
        initializeRequiredFields();
        initializeServices();
        initializeUIComponents(); // Beinhaltet jetzt auch ComboBox/DatePicker Initialisierung
        FocusTraversHelper.cancelFocusTravers(scrollPane.getContent());
        // updateCvPreviewIfExists(); // Diese Methode war fehlerhaft, erstmal auskommentieren oder korrigieren
        addBusinessLinkListener(); // Listener für Business Link hinzufügen
    }

    // --- Initialisierungs-Hilfsmethoden ---

    private void initializeRequiredFields() {
        // Pflichtfelder für den Validator registrieren
        formValidator.addRequiredTextField(candidateFirstnameField);
        formValidator.addRequiredTextField(candidateLastnameField);
        formValidator.addRequiredComboBox(positionField);
        // Füge hier ggf. weitere Pflichtfelder hinzu
    }

    private void initializeServices() {
        // Instanziiere Hilfsklassen
        skillFieldManager = new SkillFieldManager();
        dateValidator = new DateValidators();
        formBuilder = new FormBuilder(customSkillsContainer, scrollPane);
    }

    private void initializeUIComponents() {
        // Ruft die Unter-Initialisierungen auf
        initializeValidators();
        initializeSkillFields();
        initializeComboBoxes();
        initializeDatePickers();
    }

    private void initializeValidators() {
        // Richte Validatoren für numerische Felder und E-Mail ein
        formValidator.setupDecimalField(experienceYearsField, 0.0, 99.9, "Berufserfahrung");
        formValidator.setupNumericField(salaryExpectationField, 1, 500000, "Gehalt");
        formValidator.setupNumericField(travelWillingnessField, 0, 100, "Reisebereitschaft");
        formValidator.setupEmailField(emailField, emailFeedbackLabel); // Verwendet das Feedback-Label
        // Füge hier ggf. Validatoren für andere Felder hinzu (z.B. Telefonnummer)
    }

    private void initializeSkillFields() {
        // Richte die Logik für Skill-Checkboxen und Prozentfelder ein
        Map<CheckBox, TextField[]> skillFields = createSkillFieldsMap();
        skillFields.forEach((checkbox, fields) -> {
            skillFieldManager.setupSkillCheckbox(checkbox, fields[0], fields[1]);
            if (fields[0] != null) {
                // Stelle sicher, dass der Validator auch für diese Felder aufgerufen wird
                formValidator.setupNumericField(fields[0], 0, 100, "Kenntnisgrad (%) für " + checkbox.getText());
            }
        });
    }

    private Map<CheckBox, TextField[]> createSkillFieldsMap() {
        // Erstellt die Map für die Skill-Felder (unverändert)
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
        // Befülle die Positions-ComboBox
        positionField.getItems().addAll(
                "Junior Developer", "Mid-Level Developer", "Senior Developer", "Team Lead", "Architekt"
        );

        // Logik für die Anstellungsstatus-ComboBox
        employmentStatusCombo.setOnAction(e -> {
            String status = employmentStatusCombo.getValue();
            boolean isEmployed = "In Anstellung".equals(status);
            UIUtils.setVisibilityAndManaged(currentPositionBox, isEmployed);
            UIUtils.setVisibilityAndManaged(lastPositionBox, !isEmployed);
        });

        // Logik für die CV-Auswahl-ComboBox
        cvChoiceCombo.setOnAction(e -> {
            String selected = cvChoiceCombo.getValue();
            boolean isUpload = "CV hochladen".equals(selected);
            boolean isEmail = "CV per E-Mail".equals(selected);
            boolean isBusinessLinkCv = "CV im Business-Profil-Link enthalten".equals(selected);

            uploadCvButton.setVisible(isUpload);
            cvByEmailLabel.setVisible(isEmail);
            cvByBusinessLink.setVisible(isBusinessLinkCv);

            // Business-Link Felder immer anzeigen/verbergen basierend auf der Checkbox,
            // ABER die Checkbox selbst nur anzeigen, wenn NICHT "CV im Business-Profil" gewählt ist.
            businessLinkToggle.setVisible(!isBusinessLinkCv);
            // Das Textfeld für den Link ist nur sichtbar, wenn die Checkbox UND der Toggle aktiv sind,
            // ODER wenn "CV im Business-Profil" gewählt ist.
            businessLinkField.setVisible(isBusinessLinkCv || businessLinkToggle.isSelected());

            // Wenn eine andere Option als Upload gewählt wird, den Upload-Status zurücksetzen
            if (!isUpload) {
                clearCvUpload(); // Eigene Methode zum Aufräumen
            }
        });

        // Initialisiere den Business-Link Toggle Listener
        businessLinkToggle.setOnAction(e -> businessLinkField.setVisible(businessLinkToggle.isSelected()));
        // Setze initialen Zustand des Business Link Feldes basierend auf Toggle (falls schon gecheckt)
        businessLinkField.setVisible(businessLinkToggle.isSelected());
    }

    private void initializeDatePickers() {
        // Richte Datumsvalidierungen und Abhängigkeiten ein
        dateValidator.setupDateValidation(contactDatePicker, "Erstkontakt-Datum", true);
        dateValidator.setupDateValidation(convincedCandidateDatePicker, "Überzeugt-Datum", true);
        dateValidator.setupDateValidation(noticePeriodDatePicker, "Kündigungsfrist", false);
        dateValidator.setupDateValidation(startDatePicker, "Startdatum", false);
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
        // Öffnet den FileChooser und startet den Upload
        File selectedFile = selectCVFile();
        if (selectedFile != null) {
            uploadCV(selectedFile);
        }
    }

    private File selectCVFile() {
        // Konfiguriert und zeigt den FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lebenslauf (CV) auswählen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Dokumente", "*.pdf"));
        // Optional: Startverzeichnis setzen
        // fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        return fileChooser.showOpenDialog(null); // null => Standardfenster
    }

    private void uploadCV(File file) {
        // Ruft den MultipartUpload auf und aktualisiert die UI bei Erfolg
        // Annahme: MultipartUtils.uploadFile gibt den *vom Server gespeicherten Pfad/Namen* zurück
        MultipartUtils.uploadFile(file, savedFilename -> {
            if (savedFilename != null && !savedFilename.isBlank()) {
                Platform.runLater(() -> {
                    uploadedCvFilename = savedFilename; // Speichere den Server-Pfad/Namen
                    showCvPreview(file.getName()); // Zeige Vorschau mit Originalnamen
                });
            } else {
                // Fehler beim Upload anzeigen
                Platform.runLater(() -> DialogUtil.showError("Upload Fehler", "Datei konnte nicht hochgeladen werden."));
                clearCvUpload();
            }
        }, error -> {
            // Detaillierterer Fehler beim Upload
            Platform.runLater(() -> DialogUtil.showError("Upload Fehler", "Fehler: " + error));
            clearCvUpload();
        });
    }

    private void showCvPreview(String originalFileName) {
        // Aktualisiert die UI, um die hochgeladene Datei anzuzeigen
        cvPreviewBox.setVisible(true);
        // Setze Icon (Pfad zu deinem Icon anpassen!)
        try {
            cvIcon.setImage(new Image(getClass().getResourceAsStream("/images/pdf-icon.png"), 18, 18, true, true));
        } catch (Exception e) {
            System.err.println("PDF Icon nicht gefunden!");
            // Optional: Fallback-Icon oder Text
        }
        cvLink.setText(originalFileName); // Zeige den Originalnamen im Link
    }

    @FXML
    private void handleOpenUploadedCV(ActionEvent event) {
        // Versucht, die hochgeladene Datei zu öffnen (Vorschau)
        // Diese Methode macht jetzt weniger Sinn, wenn wir den Server-Pfad speichern.
        // Besser wäre, den FileDownloadService zu nutzen, um die Datei anzuzeigen.
        // Aktuell versucht es, eine lokale Datei zu öffnen, was nur direkt nach dem Upload klappt.
        if (uploadedCvFilename != null && !uploadedCvFilename.isBlank()) {
            // TODO: Implementiere Vorschau über FileDownloadService.previewFile(uploadedCvFilename)
            DialogUtil.showInfo("Info", "Vorschau-Funktion noch nicht implementiert.\nGespeicherter Pfad: " + uploadedCvFilename);
            /*
            try {
                File pdf = new File(uploadedCvFilename); // Funktioniert nur, wenn es der *lokale* Pfad ist!
                if (pdf.exists()) {
                    Desktop.getDesktop().open(pdf);
                } else {
                    DialogUtil.showError("Datei nicht gefunden", "Die hochgeladene Datei konnte lokal nicht gefunden werden.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                DialogUtil.showError("Fehler beim Öffnen", "Die Datei konnte nicht geöffnet werden.");
            }
            */
        } else {
            DialogUtil.showInfo("Keine Datei", "Es wurde noch kein Lebenslauf hochgeladen.");
        }
    }


    @FXML
    private void handleRemoveCVPreview(ActionEvent event) {
        // Entfernt die Vorschau und den gespeicherten Dateinamen
        clearCvUpload();
    }

    private void clearCvUpload() {
        // Setzt den CV-Upload-Status zurück
        uploadedCvFilename = null;
        cvPreviewBox.setVisible(false);
        cvLink.setText(""); // Link-Text leeren
        // Optional: Wenn cvChoiceCombo auf "CV hochladen" steht, zurücksetzen?
        // if ("CV hochladen".equals(cvChoiceCombo.getValue())) {
        //    cvChoiceCombo.getSelectionModel().clearSelection();
        //}
    }

    // Methode für Echtzeit-Feedback (optional, aber empfohlen)
    private void addBusinessLinkListener() {
        // 1. Listener für die Checkbox (wenn sie an-/ausgewählt wird)
        businessLinkToggle.selectedProperty().addListener((obs, ov, nv) -> handleBusinessLinkToggleChange());

        // 2. Listener für das Textfeld (wenn sich der Text darin ändert)
        businessLinkField.textProperty().addListener((obs, ov, nv) -> handleBusinessLinkToggleChange());
    }

    // Methode für die Listener der Business-Link-Felder
    private void handleBusinessLinkToggleChange() {
        boolean toggleSelected = businessLinkToggle.isSelected();
        businessLinkField.setVisible(toggleSelected); // Zeige Feld nur wenn Toggle aktiv

        // Optional: Rufe Live-Validierung auf, wenn das Feld sichtbar wird oder sich ändert
        if (toggleSelected) {
            // Führe Validierung durch (nur Stil ändern, keine Popup-Meldung)
            formValidator.validateOptionalBusinessLinkInternal(businessLinkField, businessLinkToggle, "Business-Profil-Link");
        } else {
            // Entferne Fehlerstil, wenn Toggle deaktiviert wird
            formValidator.removeErrorStyle(businessLinkField);
        }
    }


    @FXML
    private void handleSaveRecommendation(ActionEvent event) {
        // Führe die Validierung durch (stelle sicher, dass validateForm aktuell ist)
        boolean isFormValid = formValidator.validateForm(
                scrollPane,
                businessLinkField, // Übergib die Referenzen
                businessLinkToggle,
                emailField
                // Füge hier weitere Felder für spezielle Prüfungen hinzu (z.B. Prozentfelder)
        );

        if (!isFormValid) {
            // Fehler wurden bereits durch den Validator angezeigt (Dialog + Scrollen)
            System.out.println("Validierung fehlgeschlagen.");
            return; // Abbrechen
        }

        // Validierung erfolgreich, erstelle DTO und sende
        System.out.println("Validierung erfolgreich.");
        RecommendationRequestDTO dto = createRecommendationDTO();
        submitRecommendation(event, dto);
    }

    private void submitRecommendation(ActionEvent event, RecommendationRequestDTO dto) {
        // Ruft den BackendService auf
        backendService.submitRecommendation(dto)
                .thenAccept(response -> handleSubmissionResponse(event, response))
                .exceptionally(e -> handleSubmissionError(e));
    }

    private void handleSubmissionResponse(ActionEvent event, HttpResponse response) {
        // Verarbeitet die Antwort vom Backend (unverändert gut)
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
        // Behandelt Netzwerk-/Verbindungsfehler (unverändert gut)
        e.printStackTrace();
        Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler bei der Anfrage: " + e.getMessage()));
        return null;
    }


    // --- DTO Erstellung ---
    private RecommendationRequestDTO createRecommendationDTO() {
        // Sammelt Daten aus den UI-Feldern und erstellt das DTO
        RecommendationRequestDTO dto = new RecommendationRequestDTO();
        setBasicInfo(dto);
        setEmploymentInfo(dto);
        setInformationStatus(dto);
        setCareerDetails(dto);
        setCVDetails(dto); // Stellt sicher, dass der korrekte CV-Pfad/Link gesetzt wird
        setAdditionalInfo(dto);
        setSkillDetails(dto);
        return dto;
    }

    // --- Hilfsmethoden zur DTO-Befüllung (unverändert gut) ---
    private void setBasicInfo(RecommendationRequestDTO dto) {
        // dto.setUserId(...); // Wird jetzt serverseitig gesetzt, hier nicht nötig
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
        if (currentPositionBox.isVisible()) { // Nur setzen, wenn sichtbar
            dto.setCurrentPosition(currentPositionField.getText().trim());
            dto.setCurrentCareerLevel(currentCareerLevelField.getText().trim());
        }
        if (lastPositionBox.isVisible()) { // Nur setzen, wenn sichtbar
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
        } catch (NumberFormatException e) { dto.setExperienceYears(null); /* Fehler oder Standardwert */ }
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
        // Setzt die Auswahl und den Pfad/Link basierend auf der UI
        String choice = cvChoiceCombo.getValue();
        dto.setCvChoice(choice);

        // Setze den Pfad nur, wenn "CV hochladen" gewählt wurde UND ein Upload stattgefunden hat
        if ("CV hochladen".equals(choice) && uploadedCvFilename != null && !uploadedCvFilename.isBlank()) {
            dto.setDocumentCvPath(uploadedCvFilename);
        } else {
            dto.setDocumentCvPath(null); // Ansonsten keinen Pfad senden
        }

        // Setze den Business-Link nur, wenn das Feld sichtbar ist und Text enthält
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
        // Extrahiert die Skills (unverändert)
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
        // Extrahiert Standard-Skills (unverändert)
        List<RecommendationRequestDTO.SkillEntry> skills = new ArrayList<>();
        for (SkillInput input : skillInputs) {
            CheckBox checkBox = input.getCheckBox();
            TextField percentField = input.getPercentField();
            TextField nameField = input.getNameField();
            if (!checkBox.isSelected()) continue; // Nur ausgewählte Skills berücksichtigen

            String name = (nameField != null && nameField.isVisible() && !nameField.getText().isEmpty())
                    ? nameField.getText().trim()
                    : checkBox.getText();

            int percentage = 0;
            // Prozentwert nur holen, wenn Feld sichtbar und nicht leer
            if (percentField != null && percentField.isVisible() && !percentField.getText().isEmpty()) {
                try {
                    percentage = Integer.parseInt(percentField.getText().trim());
                    // Optional: Bereichsprüfung 0-100
                    percentage = Math.max(0, Math.min(100, percentage));
                } catch (NumberFormatException e) {
                    System.out.println("Ungültiger Prozentwert für Skill: " + name + ", setze auf 0.");
                }
            } else if (nameField != null && nameField.isVisible()){
                // Falls nur "Anderer Skill" ohne Prozentfeld, setze 0 oder einen Standardwert
                percentage = 0; // Oder z.B. 1, wenn Anwesenheit > 0 bedeuten soll
            }

            skills.add(new RecommendationRequestDTO.SkillEntry(name, percentage));
        }
        return skills;
    }

    private List<RecommendationRequestDTO.SkillEntry> extractCustomSkills() {
        // Extrahiert benutzerdefinierte Skills (unverändert)
        List<RecommendationRequestDTO.SkillEntry> skills = new ArrayList<>();
        for (Node node : customSkillsContainer.getChildren()) {
            if (node instanceof HBox hbox && hbox.getChildren().size() >= 3) {
                // Annahme: Reihenfolge ist Technologie, Name, Prozent
                TextField techField = (TextField) hbox.getChildren().get(0);
                TextField nameField = (TextField) hbox.getChildren().get(1);
                TextField percentField = (TextField) hbox.getChildren().get(2);

                String tech = techField.getText().trim();
                String name = nameField.getText().trim();
                String percentText = percentField.getText().trim();

                if (!name.isEmpty()) { // Name muss vorhanden sein
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


    // --- Navigation ---
    @FXML
    private void handleBack(ActionEvent event) {
        // Navigiert zurück zum Dashboard
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml", 0.8);
    }
}