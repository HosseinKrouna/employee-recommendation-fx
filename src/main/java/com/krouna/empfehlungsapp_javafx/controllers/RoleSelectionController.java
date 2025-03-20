package com.krouna.empfehlungsapp_javafx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleSelectionController {

    public void handleEmployeeLogin(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-login-view.fxml");
    }

    public void handleHRLogin(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/hr-login-view.fxml");
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
