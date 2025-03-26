package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.MultipartUtils;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class EmployeeNewRecommendationController {

    @FXML private TextField candidateFirstnameField;
    @FXML private TextField candidateLastnameField;
    @FXML private TextField positionField;
    @FXML private TextField documentCvField;

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
                    switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml");
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
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showError("Fehler", "Fehler beim Szenenwechsel!");
        }
    }
}

