package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.MultipartUtils;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class EmployeeNewRecommendationController {

    @FXML
    private TextField candidateFirstnameField;
    @FXML
    private TextField candidateLastnameField;
    @FXML
    private TextField positionField;
    @FXML
    private TextField documentCvField;
    @FXML
    private TextField documentCoverLetterField;
    @FXML
    private Label errorLabel;
//    @FXML
//    private TextField successLabel;

    private String uploadedCvFilename;
    private String uploadedCoverLetterFilename;

    @FXML
    private void handleBrowseCV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CV auswählen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            MultipartUtils.uploadFile(selectedFile, savedFilename -> {
                uploadedCvFilename = savedFilename;
                documentCvField.setText(savedFilename); // Zeigt den Dateinamen an
            });
        }
    }

    @FXML
    private void handleBrowseCoverLetter(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Anschreiben auswählen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            MultipartUtils.uploadFile(selectedFile, savedFilename -> {
                uploadedCoverLetterFilename = savedFilename;
                documentCoverLetterField.setText(savedFilename);
            });
        }
    }

    @FXML
    private void handleSaveRecommendation(ActionEvent event) {
        String firstName = candidateFirstnameField.getText().trim();
        String lastname = candidateLastnameField.getText().trim();
        String position = positionField.getText().trim();

        if (firstName.isEmpty() || lastname.isEmpty() || position.isEmpty()) {
            errorLabel.setText("Bitte alle Pflichtfelder ausfüllen.");
            return;
        }

        if (uploadedCvFilename == null || uploadedCoverLetterFilename == null) {
            errorLabel.setText("Bitte beide Dokumente hochladen.");
            return;
        }

        RecommendationRequestDTO dto = new RecommendationRequestDTO();
        dto.setUserId(UserSession.getInstance().getUserId());
        dto.setCandidateFirstname(firstName);
        dto.setCandidateLastname(lastname);
        dto.setPosition(position);
        dto.setDocumentCvPath(uploadedCvFilename);
        dto.setDocumentCoverLetterPath(uploadedCoverLetterFilename);

        new BackendService().submitRecommendation(dto).thenAccept(response -> {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                System.out.println("Empfehlung erfolgreich gespeichert.");
                Platform.runLater(() -> switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml"));
//                successLabel.setText("✅ Empfehlung erfolgreich gespeichert!");
            } else {
                Platform.runLater(() -> errorLabel.setText("Fehler beim Speichern!"));
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            Platform.runLater(() -> errorLabel.setText("Fehler bei der Anfrage!"));
            return null;
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Fehler beim Szenenwechsel!");
        }
    }
}
