package com.krouna.empfehlungsapp_javafx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

public class HRLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        // Hier Logik für den HR-Login einfügen
        System.out.println("HR-Login: " + usernameField.getText());
        // Beispiel: Wechsel zum HR-Dashboard
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/hr-dashboard.fxml");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // Wechsel zurück zur Rollenauswahl
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

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
