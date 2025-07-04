package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javafx.stage.Modality;

public class DialogUtil {


    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }


    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }


    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }



    public static boolean showConfirmation(String title, String message) {

        if (!Platform.isFxApplicationThread()) {
            System.err.println("WARNUNG: showConfirmation sollte vom JavaFX Application Thread aufgerufen werden.");

            return false;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        configureAlert(alert, title, message);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }



    private static void showAlert(Alert.AlertType type, String title, String message) {

        if (Platform.isFxApplicationThread()) {

            createAndShowAlert(type, title, message);
        } else {

            Platform.runLater(() -> createAndShowAlert(type, title, message));
        }
    }


    private static void createAndShowAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        configureAlert(alert, title, message);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }


    private static void configureAlert(Alert alert, String title, String message) {
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.initModality(Modality.APPLICATION_MODAL);

    }
}