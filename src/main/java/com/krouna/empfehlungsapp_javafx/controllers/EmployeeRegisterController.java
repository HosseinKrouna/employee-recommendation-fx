package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;



public class EmployeeRegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final BackendService backendService = new BackendService();

    @FXML
    private void handleRegister(ActionEvent event) {
        if (!validateInput()) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        backendService.registerEmployee(username, password).thenAccept(response -> {
            if (response.statusCode() == 200) {
                Platform.runLater(() -> {
                    DialogUtil.showInfo("Erfolg", "Registrierung erfolgreich!");
                    SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-login-view.fxml");
                });
            } else {
                Platform.runLater(() -> DialogUtil.showError("Fehler", "Registrierung fehlgeschlagen: " + response.body()));
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            Platform.runLater(() -> DialogUtil.showError("Fehler", "Verbindungsfehler beim Registrieren!"));
            return null;
        });
    }

    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
            DialogUtil.showError("Fehler", "Bitte alle Felder ausfüllen!");
            return false;
        }
        return true;
    }


    @FXML
    private void handleBack(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

}