package com.krouna.empfehlungsapp_javafx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class SceneUtil {

    public static void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
        } catch (IOException e) {
            DialogUtil.showError("Fehler", "Fehler beim Laden der Ansicht.");
        }
    }
}
