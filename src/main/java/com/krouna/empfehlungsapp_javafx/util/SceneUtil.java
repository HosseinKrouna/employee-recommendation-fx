package com.krouna.empfehlungsapp_javafx.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneUtil {

    public static void switchScene(ActionEvent event, String fxmlPath) {
        switchScene(event, fxmlPath, 0); // 0 = keine Größenanpassung
    }

    public static void switchScene(ActionEvent event, String fxmlPath, double sizeFactor) {
        try {
            Parent root = FXMLLoader.load(SceneUtil.class.getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(scene);
            stage.sizeToScene();

            // Automatisch zentrieren
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double centeredX = (screenBounds.getWidth() - stage.getWidth()) / 2;
            double centeredY = (screenBounds.getHeight() - stage.getHeight()) / 2;
            stage.setX(centeredX);
            stage.setY(centeredY);

            stage.show();

            // Nur anpassen, wenn Faktor > 0
            if (sizeFactor > 0) {
//                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setWidth(screenBounds.getWidth() * sizeFactor);
                stage.setHeight(screenBounds.getHeight() * sizeFactor);
                stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
                stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
            } else {
                stage.sizeToScene(); // Standardgröße aus FXML
            }

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
