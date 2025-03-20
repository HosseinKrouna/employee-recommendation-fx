package com.krouna.empfehlungsapp_javafx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class HRLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    public void handleHRLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Simulierter Login (später mit API-Call ersetzen)
        if ("hr".equals(username) && "admin".equals(password)) {
            System.out.println("HR erfolgreich eingeloggt!");
        } else {
            System.out.println("Falscher Benutzername oder Passwort!");
        }
    }

    public void handleBack(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            // Hole die aktuelle Stage und setze die neue Scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

