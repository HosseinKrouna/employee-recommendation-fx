package com.krouna.empfehlungsapp_javafx.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HRLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    /**
     * Wird aufgerufen, wenn der HR-Benutzer auf "Anmelden" klickt.
     * Hier senden wir einen HTTP-POST-Request an das Backend, um die Login-Daten zu prüfen.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        String jsonBody = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());

                long userId = json.get("id").asLong();
                String returnedUsername = json.get("username").asText();
                String role = json.get("role").asText();

                // 🔒 Nur HR darf ins HR-Dashboard
                if (!"HR".equalsIgnoreCase(role)) {
                    errorLabel.setText("Kein Zugriff. Nur HR erlaubt!");
                    return;
                }

                // ✅ Daten in UserSession speichern
                UserSession.getInstance().setUserId(userId);
                UserSession.getInstance().setUsername(returnedUsername);

                switchScene(event, "/com/krouna/empfehlungsapp_javafx/hr-dashboard-view.fxml");
            } else {
                errorLabel.setText("Ungültige Anmeldedaten!");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            errorLabel.setText("Fehler bei der Anfrage!");
        }
    }


    /**
     * Wechselt die Scene und übernimmt dabei die aktuelle Fenstergröße.
     */
    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wechselt zurück zur Rollenauswahl.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }
}
