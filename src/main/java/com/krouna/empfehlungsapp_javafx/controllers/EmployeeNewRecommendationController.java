package com.krouna.empfehlungsapp_javafx.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

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


    // Methode, die aufgerufen wird, wenn der "Durchsuchen"-Button für den CV geklickt wird
    @FXML
    private void handleBrowseCV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CV auswählen (NUR PDF!)");
        // Optional: Setze Dateitypfilter
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF-Dateien", "*.pdf")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            documentCvField.setText(selectedFile.getAbsolutePath());
        }
    }


//     Methode, die aufgerufen wird, wenn der "Durchsuchen"-Button für das Anschreiben geklickt wird
    @FXML
    private void handleBrowseCoverLetter(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Anschreiben auswählen (NUR PDF!)");
        // Optional: Setze Dateitypfilter
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF-Dateien", "*.pdf")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            documentCoverLetterField.setText(selectedFile.getAbsolutePath());
        }
    }

    /**
     * Wird aufgerufen, wenn der Mitarbeiter auf "Empfehlung speichern" klickt.
     * Hier kannst du die Eingaben validieren und per REST-Call an dein Backend senden.
     */
    @FXML
    private void handleSaveRecommendation(ActionEvent event) {
        String firstName = candidateFirstnameField.getText().trim();
        String lastname = candidateLastnameField.getText().trim();
        String position = positionField.getText().trim();
        String cvPath = documentCvField.getText().trim();
        String coverLetterPath = documentCoverLetterField.getText().trim();
        long userId = UserSession.getInstance().getUserId();

        if (firstName.isEmpty() || lastname.isEmpty() || position.isEmpty()) {
            errorLabel.setText("Bitte alle Pflichtfelder ausfüllen.");
            return;
        }

        RecommendationRequestDTO dto = new RecommendationRequestDTO ();
        dto.setUserId(UserSession.getInstance().getUserId());
        dto.setCandidateFirstname(firstName);
        dto.setCandidateLastname(lastname);
        dto.setPosition(position);
        dto.setDocumentCvPath(cvPath);
        dto.setDocumentCoverLetterPath(coverLetterPath);

        String username = UserSession.getInstance().getUsername();

        BackendService backendService = new BackendService();

        backendService.submitRecommendation(dto).thenAccept(response -> {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                System.out.println("Empfehlung erfolgreich gespeichert.");
                javafx.application.Platform.runLater(() -> switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml"));
            } else {
                javafx.application.Platform.runLater(() -> errorLabel.setText("Fehler beim Speichern!"));
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> errorLabel.setText("Fehler bei der Anfrage!"));
            return null;
        });
    }


    /**
     * Wechselt zurück zur Mitarbeiter-Dashboard-Ansicht.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml");
    }

    /**
     * Hilfsmethode zum Szenenwechsel.
     */
    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            // Behalte die aktuelle Fenstergröße
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Fehler beim Szenenwechsel!");
        }
    }
}
