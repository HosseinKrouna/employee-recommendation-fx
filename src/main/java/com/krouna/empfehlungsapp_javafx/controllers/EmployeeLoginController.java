package com.krouna.empfehlungsapp_javafx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;

public class EmployeeLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /**
     * Wird aufgerufen, wenn der Benutzer auf "Anmelden" klickt.
     * Hier kannst du später die Anmeldeprüfung (z.B. über einen REST-API-Aufruf)
     * implementieren.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Beispielhafte Validierung – hier später die echte Authentifizierung einfügen
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Bitte Benutzername und Passwort eingeben!");
            return;
        }

        System.out.println("Mitarbeiter-Login versucht für: " + username);

        // Bei erfolgreichem Login zum Dashboard wechseln (Pfad anpassen)
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard.fxml");
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
