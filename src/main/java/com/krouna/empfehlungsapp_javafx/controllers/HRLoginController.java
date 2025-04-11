package com.krouna.empfehlungsapp_javafx.controllers;

// Imports prüfen und ggf. anpassen
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.dto.LoginResponseDTO; // Importiere DTO
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;


public class HRLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel; // Kann bleiben oder durch DialogUtil ersetzt werden

    private final BackendService backendService = new BackendService();
    // Zentraler ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    private void handleLogin(ActionEvent event) {
        if (!validateInput()) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Fehlermeldung ggf. zurücksetzen
        setError(null);

        backendService.login(username, password).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    try {
                        // Parse die Antwort in LoginResponseDTO
                        LoginResponseDTO loginData = objectMapper.readValue(response.body(), LoginResponseDTO.class);

                        // Rollenprüfung
                        if (!"HR".equalsIgnoreCase(loginData.getRole())) {
                            // Verwende DialogUtil oder setError
                            DialogUtil.showError("Login Fehler", "Kein Zugriff. Nur HR erlaubt!");
                            // Optional: Session leeren
                            // UserSession.getInstance().clear();
                            return;
                        }

                        // Speichere die Benutzerdaten UND DAS TOKEN in der Session
                        UserSession session = UserSession.getInstance();
                        session.setUserId(loginData.getId());
                        session.setUsername(loginData.getUsername());
                        session.setToken(loginData.getToken()); // <-- Token speichern

                        // Erfolgreich, wechsle zum HR Dashboard
                        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/hr-dashboard-view.fxml");

                    } catch (IOException e) {
                        DialogUtil.showError("Fehler", "Fehler bei der Verarbeitung der Server-Antwort.");
                        e.printStackTrace();
                    }
                } else {
                    // Login fehlgeschlagen
                    DialogUtil.showError("Login Fehler", "Ungültige Anmeldedaten!");
                }
            });
        }).exceptionally(e -> {
            // Netzwerkfehler
            Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler bei der Verbindung zum Server: " + e.getMessage()));
            e.printStackTrace();
            return null;
        });
    }

    // Hilfsmethode zum Setzen/Löschen der Fehlermeldung (optional, wenn DialogUtil bevorzugt wird)
    private void setError(String message) {
        if (message == null || message.isEmpty()) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        } else {
            errorLabel.setText("⚠ " + message);
            errorLabel.setVisible(true);
        }
    }

    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
            DialogUtil.showError("Eingabefehler", "Bitte Benutzernamen und Passwort eingeben!");
            return false;
        }
        return true;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }
}