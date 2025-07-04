package com.krouna.empfehlungsapp_javafx.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.dto.LoginResponseDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class EmployeeLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final BackendService backendService = new BackendService();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Einfache Eingabeprüfung
        if (username.isEmpty() || password.isEmpty()) {
            setError("Bitte Benutzername und Passwort eingeben!");
            return;
        }

        setError(null);


        backendService.login(username, password).thenAccept(response -> {

            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    try {

                        LoginResponseDTO loginData = objectMapper.readValue(response.body(), LoginResponseDTO.class);


                        if (!"MITARBEITER".equalsIgnoreCase(loginData.getRole())) {
                            setError("Zugriff nur für Mitarbeiter!");
                        }


                        UserSession session = UserSession.getInstance();
                        session.setUserId(loginData.getId());
                        session.setUsername(loginData.getUsername());
                        session.setToken(loginData.getToken());


                        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml", 0.8);

                    } catch (IOException e) {

                        setError("Fehler beim Verarbeiten der Server-Antwort.");
                        e.printStackTrace();
                    }
                } else {

                    setError("Ungültige Anmeldedaten!");
                }
            });
        }).exceptionally(e -> {

            Platform.runLater(() -> setError("Fehler bei der Verbindung zum Server: " + e.getMessage()));
            e.printStackTrace();
            return null;
        });


    }


    private void setError(String message) {
        if (message == null || message.isEmpty()) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        } else {
            errorLabel.setText("⚠ " + message);
            errorLabel.setVisible(true);

        }
    }


    @FXML
    private void handleRegister(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-register-view.fxml");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }
}