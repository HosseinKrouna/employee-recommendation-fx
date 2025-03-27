package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.MultipartUtils;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.function.Consumer;

public class EmployeeNewRecommendationController {

//    @FXML private TextField candidateFirstnameField;
//    @FXML private TextField candidateLastnameField;
//    @FXML private TextField positionField;
//    @FXML private TextField documentCvField;

    @FXML private TextField candidateFirstnameField;
    @FXML private TextField candidateLastnameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField knownFromField;
    @FXML private ComboBox<String> employmentStatusCombo;
    @FXML private ComboBox<String> cvChoiceCombo;
    @FXML private TextField cvLinkField;
    @FXML private Button uploadCvButton;
    @FXML private DatePicker contactDatePicker;
    @FXML private TextField conversationPeriodField;
    @FXML private ComboBox<String> personalityTypeCombo;
    @FXML private TextArea hobbiesField;
    @FXML private TextArea projectExperienceField;
    @FXML private Button submitButton;



    private String uploadedCvFilename;

    private final BackendService backendService = new BackendService();

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
                positionField.getText().trim().isEmpty()) {
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
        dto.setPosition(positionField.getText().trim());
        dto.setDocumentCvPath(uploadedCvFilename);
        return dto;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml");
    }

}

