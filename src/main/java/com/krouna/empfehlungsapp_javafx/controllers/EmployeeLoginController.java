package com.krouna.empfehlungsapp_javafx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class EmployeeLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    /**
     * Wird aufgerufen, wenn der Benutzer auf "Anmelden" klickt.
     * Hier kannst du später die Anmeldeprüfung (z.B. über einen REST-API-Aufruf)
     * implementieren.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Erstelle JSON-Body für den Request
        String jsonBody = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

        // HttpClient erstellen
        HttpClient client = HttpClient.newHttpClient();

        // HttpRequest an das Spring Boot Backend (z. B. http://localhost:8080/api/users/login)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        try {
            // Sende den Request synchron und empfange die Antwort
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Bei erfolgreicher Authentifizierung: Wechsel zum HR-Dashboard
                switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml");
            } else {
                // Falls die Authentifizierung fehlschlägt, Fehlermeldung anzeigen
                errorLabel.setText("Ungültige Anmeldedaten!");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            errorLabel.setText("Fehler bei der Anfrage!");
        }

        System.out.println("Mitarbeiter-Login versucht für: " + username);

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
