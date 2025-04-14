package com.krouna.empfehlungsapp_javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent; // Parent importieren
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL; // URL importieren

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 352, 377);
        stage.setTitle("EmpfehlungsApp");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}