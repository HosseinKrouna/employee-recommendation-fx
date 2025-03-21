package com.krouna.empfehlungsapp_javafx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HRDashboardController {

    // Diese Methode wird nach dem Laden der FXML automatisch aufgerufen.
    @FXML
    public void initialize() {
        // Hier könntest du z.B. Daten aus einer REST-API laden und in der UI darstellen.
        System.out.println("HR Dashboard Controller initialisiert.");
    }

    // Logout-Button: Wechselt zurück zur Rollenauswahl oder zum Login.
    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

    /**
     * Hilfsmethode zum Wechseln der Scene.
     *
     * @param event    Das auslösende ActionEvent.
     * @param fxmlPath Der Pfad zur FXML-Datei, zu der gewechselt werden soll.
     */
    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Übernehme die aktuelle Größe des Fensters:
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
