package com.krouna.empfehlungsapp_javafx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

public class EmployeeRegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    // Optional: Ein Label, um Fehlermeldungen anzuzeigen
    @FXML
    private Label errorLabel;

    /**
     * Wird aufgerufen, wenn der "Registrieren"-Button geklickt wird.
     * Hier wird ein HTTP-POST-Request an dein Backend gesendet, um den neuen Mitarbeiter zu registrieren.
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Erstelle den JSON-Body für den Request
        String jsonBody = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        // Erstelle einen HttpClient und den Request (Backend-Endpoint muss übereinstimmen)
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users/register-employee"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Registrierung war erfolgreich – wechsle zur Login-Ansicht
                switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-login-view.fxml");
            } else {
                // Fehlermeldung anzeigen
                if(errorLabel != null) {
                    errorLabel.setText("Registrierung fehlgeschlagen: " + response.body());
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            if(errorLabel != null) {
                errorLabel.setText("Fehler beim Senden der Anfrage!");
            }
        }
    }

    /**
     * Wird aufgerufen, wenn der "Zurück"-Button geklickt wird.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

    /**
     * Hilfsmethode, um die Scene zu wechseln und die aktuelle Fenstergröße beizubehalten.
     */
    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
