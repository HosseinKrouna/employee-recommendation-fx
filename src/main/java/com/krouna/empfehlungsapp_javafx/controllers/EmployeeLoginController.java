package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;



public class EmployeeLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final BackendService backendService = new BackendService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        backendService.authenticateUser(username, password).thenAccept(userDataOpt ->
                Platform.runLater(() -> userDataOpt.ifPresentOrElse(userData -> {
                    UserSession.getInstance().setUserId(userData.getId());
                    UserSession.getInstance().setUsername(userData.getUsername());
                    SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml", 0.8);
                }, () -> {
                    errorLabel.setText("Ungültige Anmeldedaten!");
                }))
        ).exceptionally(e -> {
            e.printStackTrace();
            Platform.runLater(() -> errorLabel.setText("Fehler bei der Anfrage!"));
            return null;
        });

        System.out.println("Mitarbeiter-Login versucht für: " + username);
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
