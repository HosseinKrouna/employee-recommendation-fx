package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleSelection; // Auswahl für "Mitarbeiter" oder "HR"

    @FXML
    private void initialize() {
        roleSelection.getItems().addAll("Mitarbeiter", "HR");
    }

    @FXML
    private void onLoginClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleSelection.getValue();

        if (role == null) {
            showAlert("Fehler", "Bitte eine Rolle auswählen!");
            return;
        }
//
//        // Simpler Test-Login (später mit Datenbank verbinden)
//        if (isValidUser(username, password, role)) {
//            // Zuerst den Benutzernamen in die Session setzen
//            UserSession.getInstance().setUsername(username);
//            // Dann ausgeben
//            System.out.println("Benutzername in der Session: " + UserSession.getInstance().getUsername());
//            switchToDashboard(role);
//        } else {
//            showAlert("Fehler", "Ungültige Anmeldeinformationen!");
//        }
    }

    private void switchToDashboard(String role) {
        try {
            FXMLLoader loader;
            if (role.equals("Mitarbeiter")) {
                loader = new FXMLLoader(getClass().getResource("/com/krouna/empfehlungsapp_javafx/employee-dashboard-view.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/com/krouna/empfehlungsapp_javafx/hr-dashboard-view.fxml"));
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private boolean isValidUser(String username, String password, String role) {
//        // Hier später Datenbank-Check mit Spring Boot einbauen
//        return username.equals("test") && password.equals("1234");
//    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onRegisterClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/krouna/empfehlungsapp_javafx/register-view.fxml"));
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(fxmlLoader.load()));
    }
}
