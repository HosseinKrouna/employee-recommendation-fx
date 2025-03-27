package com.krouna.empfehlungsapp_javafx.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;



public class HRLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final BackendService backendService = new BackendService();

    @FXML
    private void handleLogin(ActionEvent event) {
        if (!validateInput()) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        backendService.login(username, password).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode json = mapper.readTree(response.body());

                        String role = json.get("role").asText();
                        if (!"HR".equalsIgnoreCase(role)) {
                            DialogUtil.showError("Fehler", "Kein Zugriff. Nur HR erlaubt!");
                            return;
                        }

                        UserSession.getInstance().setUserId(json.get("id").asLong());
                        UserSession.getInstance().setUsername(json.get("username").asText());

                        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/hr-dashboard-view.fxml");
                    } catch (IOException e) {
                        DialogUtil.showError("Fehler", "Fehler bei der Verarbeitung der Antwort.");
                    }
                } else {
                    DialogUtil.showError("Fehler", "Ungültige Anmeldedaten!");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler bei der Anfrage!"));
            return null;
        });
    }

    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
            DialogUtil.showError("Fehler", "Bitte Benutzernamen und Passwort eingeben!");
            return false;
        }
        return true;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

}
