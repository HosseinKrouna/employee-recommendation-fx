package com.krouna.empfehlungsapp_javafx.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class EmployeeLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final BackendService backendService = new BackendService();

    /**
     * Wird aufgerufen, wenn der Benutzer auf "Anmelden" klickt.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        backendService.login(username, password).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode json = mapper.readTree(response.body());

                    long userId = json.get("id").asLong();            // 👈 ID aus Response
                    String returnedUsername = json.get("username").asText();  // optional: falls Server den echten Namen zurückschickt

                    UserSession.getInstance().setUserId(userId);      // ✅ jetzt gesetzt!
                    UserSession.getInstance().setUsername(returnedUsername);

                    Platform.runLater(() -> switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml"));

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> errorLabel.setText("Fehler beim Verarbeiten der Antwort!"));
                }
            } else {
                Platform.runLater(() -> errorLabel.setText("Ungültige Anmeldedaten!"));
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            Platform.runLater(() -> errorLabel.setText("Fehler bei der Anfrage!"));
            return null;
        });

        System.out.println("Mitarbeiter-Login versucht für: " + username);
    }


    /**
     * Wird aufgerufen, wenn der Benutzer den Registrieren-Button klickt.
     * Wechselt zur Registrierungsansicht.
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-register-view.fxml");
    }

    /**
     * Wird aufgerufen, wenn der Benutzer auf "Zurück" klickt.
     * Wechselt zurück zur Rollenauswahl.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

    /**
     * Hilfsmethode zum Wechseln der Szene.
     *
     * @param event    Das auslösende ActionEvent.
     * @param fxmlPath Der Pfad zur FXML-Datei, zu der gewechselt werden soll.
     */
    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Aktuelle Größe des Fensters holen
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            // Neue Scene mit aktueller Größe erstellen
            Scene newScene = new Scene(root, currentWidth, currentHeight);
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
