package com.krouna.empfehlungsapp_javafx.controllers;

// Imports hinzufügen oder anpassen
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.dto.LoginResponseDTO; // Importiere das DTO
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil; // DialogUtil für Fehler verwenden
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException; // Für IOException

public class EmployeeLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel; // Kann bleiben oder durch DialogUtil ersetzt werden

    private final BackendService backendService = new BackendService();
    // ObjectMapper für das Parsen der JSON-Antwort
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim(); // trim() hinzufügen
        String password = passwordField.getText().trim(); // trim() hinzufügen

        // Einfache Eingabeprüfung
        if (username.isEmpty() || password.isEmpty()) {
            setError("Bitte Benutzername und Passwort eingeben!");
            return;
        }
        // Fehlermeldung zurücksetzen vor neuem Versuch
        setError(null); // Oder errorLabel.setText("")

        // Verwende die BackendService.login Methode
        backendService.login(username, password).thenAccept(response -> {
            // Verarbeitung der Antwort auf dem JavaFX Thread
            Platform.runLater(() -> {
                if (response.statusCode() == 200) { // Erfolg
                    try {
                        // Parse die JSON-Antwort in das LoginResponseDTO
                        LoginResponseDTO loginData = objectMapper.readValue(response.body(), LoginResponseDTO.class);

                        // Rollenprüfung: Stelle sicher, dass es ein Mitarbeiter ist
                        if (!"MITARBEITER".equalsIgnoreCase(loginData.getRole())) {
                            setError("Zugriff nur für Mitarbeiter!");
                            // Optional: Session leeren, falls vorher jemand angemeldet war
                            // UserSession.getInstance().clear();
                            return;
                        }

                        // Speichere die Benutzerdaten und das TOKEN in der Session
                        UserSession session = UserSession.getInstance();
                        session.setUserId(loginData.getId());
                        session.setUsername(loginData.getUsername());
                        session.setToken(loginData.getToken()); // <-- Token speichern

                        // Erfolgreich eingeloggt, wechsle zum Dashboard
                        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml", 0.8);

                    } catch (IOException e) {
                        // Fehler beim Parsen der JSON-Antwort
                        setError("Fehler beim Verarbeiten der Server-Antwort.");
                        e.printStackTrace(); // Fehler loggen
                    }
                } else {
                    // Login fehlgeschlagen (z.B. 401 Unauthorized)
                    setError("Ungültige Anmeldedaten!");
                }
            });
        }).exceptionally(e -> {
            // Fehler bei der Netzwerkkommunikation
            Platform.runLater(() -> setError("Fehler bei der Verbindung zum Server: " + e.getMessage()));
            e.printStackTrace(); // Fehler loggen
            return null;
        });

        // Das System.out kann bleiben oder entfernt werden
        // System.out.println("Mitarbeiter-Login versucht für: " + username);
    }

    // Hilfsmethode zum Setzen/Löschen der Fehlermeldung
    private void setError(String message) {
        if (message == null || message.isEmpty()) {
            errorLabel.setText("");
            errorLabel.setVisible(false); // Optional: Label ausblenden
        } else {
            errorLabel.setText("⚠ " + message);
            errorLabel.setVisible(true);  // Optional: Label einblenden
            // Alternative: DialogUtil verwenden
            // DialogUtil.showError("Login Fehler", message);
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