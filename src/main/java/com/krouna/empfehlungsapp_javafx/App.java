package com.krouna.empfehlungsapp_javafx;

import com.krouna.empfehlungsapp_javafx.util.BackendProcessManager; // Importiere die neue Klasse
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;       // Importiere dein DialogUtil (falls vorhanden)
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent; // Parent importieren
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects; // Für requireNonNull

public class App extends Application {

    private BackendProcessManager backendManager;
    private final boolean autoStartBackend = true;


    @Override
    public void init() throws Exception {
        super.init();

        if (autoStartBackend) {
            System.out.println("[JavaFX App] Initialisierung: Versuche Backend zu starten...");

            String jarPath = "C:/Users/User/Documents/DO-IT/Empfehlungsapp_Backend/target/empfehlungsapp-0.0.1-SNAPSHOT.jar";


            String healthCheckUrl = "http://localhost:8080/actuator/health";


            int maxWaitSeconds = 60;


            boolean redirectOutput = true;


            backendManager = new BackendProcessManager(jarPath, healthCheckUrl, maxWaitSeconds, redirectOutput);


            boolean backendStarted = backendManager.startBackend();

            if (!backendStarted) {

                System.err.println("[JavaFX App] FATAL: Backend konnte nicht gestartet werden!");


                Platform.runLater(() -> {

                    DialogUtil.showError("Backend Startfehler",
                            "Das Spring Boot Backend konnte nicht gestartet werden.\n" +
                                    "Mögliche Ursachen:\n" +
                                    "- Java ist nicht im Systempfad.\n" +
                                    "- Der Pfad zur JAR-Datei ist falsch:\n  " + jarPath + "\n" +
                                    "- Port 8080 ist bereits belegt.\n" +
                                    "- Fehler beim Start des Backends (siehe Konsole).\n\n" +
                                    "Die Anwendung wird beendet.");
                    Platform.exit();
                });


                throw new IOException("Automatischer Start des Backends fehlgeschlagen. Siehe Logs.");
            } else {
                System.out.println("[JavaFX App] Backend erfolgreich gestartet und bereit.");
            }

        } else {
            System.out.println("[JavaFX App] Automatischer Backend-Start ist deaktiviert.");
        }
    }


    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("[JavaFX App] Start Methode wird aufgerufen.");

        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml")));

        Scene scene = new Scene(root);
        stage.setTitle("EmpfehlungsApp");
        stage.setScene(scene);
        stage.show();
        System.out.println("[JavaFX App] Stage angezeigt.");
    }


    @Override
    public void stop() throws Exception {
        System.out.println("[JavaFX App] Stop Methode wird aufgerufen...");
        if (backendManager != null) {
            System.out.println("[JavaFX App] Stoppe Backend-Prozess...");
            backendManager.stopBackend();
        } else {
            System.out.println("[JavaFX App] Kein Backend-Manager zum Stoppen vorhanden.");
        }
        super.stop();
        System.out.println("[JavaFX App] Anwendung gestoppt.");
    }



    public static void main(String[] args) {
        launch(args);
    }
}