package com.krouna.empfehlungsapp_javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class DashboardController {

    @FXML
    private void handleNewRecommendation() {
        showMessage("Hier kannst du eine neue Empfehlung einreichen!");
    }

    @FXML
    private void handleViewRecommendations() {
        showMessage("Hier kannst du deine Empfehlungen sehen!");
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
