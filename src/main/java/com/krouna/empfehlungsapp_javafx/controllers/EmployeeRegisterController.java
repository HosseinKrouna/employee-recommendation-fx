package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class EmployeeRegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final BackendService backendService = new BackendService();

    @FXML
    private void handleRegister(ActionEvent event) {
        if (!validateInput()) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        backendService.registerEmployee(username, password).thenAccept(response -> {
            if (response.statusCode() == 200) {
                Platform.runLater(() -> {
                    DialogUtil.showInfo("Erfolg", "Registrierung erfolgreich!");
                    switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-login-view.fxml");
                });
            } else {
                Platform.runLater(() -> DialogUtil.showError("Fehler", "Registrierung fehlgeschlagen: " + response.body()));
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            Platform.runLater(() -> DialogUtil.showError("Fehler", "Verbindungsfehler beim Registrieren!"));
            return null;
        });
    }

    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
            DialogUtil.showError("Fehler", "Bitte alle Felder ausfüllen!");
            return false;
        }
        return true;
    }


    @FXML
    private void handleBack(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showError("Fehler", "Fehler beim Laden der Ansicht.");
        }
    }
}




//package com.krouna.empfehlungsapp_javafx.controllers;
//
//import javafx.application.Platform;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.control.*;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.Node;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.charset.StandardCharsets;
//
//public class EmployeeRegisterController {
//
//    @FXML
//    private TextField usernameField;
//
//    @FXML
//    private PasswordField passwordField;
//
//    @FXML
//    private Label errorLabel;
//
//    private final HttpClient client = HttpClient.newHttpClient();
//
//    @FXML
//    private void handleRegister(ActionEvent event) {
//        String username = usernameField.getText();
//        String password = passwordField.getText();
//
//        if (username.isEmpty() || password.isEmpty()) {
//            showAlert("Fehler", "Bitte alle Felder ausfüllen!");
//            return;
//        }
//
//        String jsonBody = String.format(
//                "{\"username\":\"%s\", \"password\":\"%s\", \"role\":\"MITARBEITER\"}",
//                username, password
//        );
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/api/users/register-employee"))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
//                .build();
//
//        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenAccept(response -> {
//                    System.out.println("REG RESPONSE: " + response.statusCode());
//                    if (response.statusCode() == 200) {
//                        Platform.runLater(() -> {
//                            showInfo("Erfolg", "Registrierung erfolgreich!");
//                            switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-login-view.fxml");
//                        });
//                    } else {
//                        Platform.runLater(() ->
//                                showAlert("Fehler", "Registrierung fehlgeschlagen:\n" + response.body()));
//                    }
//                })
//                .exceptionally(e -> {
//                    e.printStackTrace();
//                    Platform.runLater(() -> showAlert("Fehler", "Verbindungsfehler beim Registrieren!"));
//                    return null;
//                });
//    }
//
//    @FXML
//    private void handleBack(ActionEvent event) {
//        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
//    }
//
//    private void switchScene(ActionEvent event, String fxmlPath) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//            Parent root = loader.load();
//            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void showAlert(String title, String message) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    private void showInfo(String title, String message) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//}
