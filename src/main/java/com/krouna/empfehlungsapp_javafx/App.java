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
    // Setze dies auf true, um das Backend automatisch zu starten,
    // oder auf false, um es manuell zu starten (z.B. aus der IDE)
    private final boolean autoStartBackend = true;

    /**
     * Diese Methode wird aufgerufen, BEVOR das erste Fenster (Stage) angezeigt wird.
     * Sie eignet sich gut, um nicht-grafische Initialisierungen durchzuführen,
     * wie das Starten des Backend-Prozesses.
     * Läuft NICHT auf dem JavaFX Application Thread.
     */
    @Override
    public void init() throws Exception {
        super.init(); // WICHTIG: Immer die Super-Methode aufrufen!

        if (autoStartBackend) {
            System.out.println("[JavaFX App] Initialisierung: Versuche Backend zu starten...");

            // --- KONFIGURATION ---
            // !!! WICHTIG: Passe diesen Pfad an !!!
            // Er muss auf die ausführbare JAR-Datei deines Spring Boot Backends zeigen.
            // Verwende den Pfad, den du nach 'mvn package -DskipTests' erhalten hast.
            // Beispiel für deinen Pfad:
            String jarPath = "C:/Users/User/Documents/DO-IT/Empfehlungsapp_Backend/target/empfehlungsapp-0.0.1-SNAPSHOT.jar";

            // URL für den Health-Check des Backends (Spring Boot Actuator)
            String healthCheckUrl = "http://localhost:8080/actuator/health"; // Standard: Port 8080

            // Maximale Wartezeit in Sekunden, bis das Backend als "gestartet" gilt
            int maxWaitSeconds = 60;

            // Sollen die Logs des Backends in dieser Konsole angezeigt werden?
            boolean redirectOutput = true;

            // Erstelle den Manager
            backendManager = new BackendProcessManager(jarPath, healthCheckUrl, maxWaitSeconds, redirectOutput);

            // Starte das Backend und warte, bis es bereit ist (oder ein Fehler auftritt)
            boolean backendStarted = backendManager.startBackend();

            if (!backendStarted) {
                // Backend konnte nicht gestartet werden - Kritischer Fehler
                System.err.println("[JavaFX App] FATAL: Backend konnte nicht gestartet werden!");

                // Zeige einen Fehlerdialog im JavaFX-Thread an und beende die App
                Platform.runLater(() -> {
                    // Ersetze dies ggf. durch dein eigenes DialogUtil
                    DialogUtil.showError("Backend Startfehler",
                            "Das Spring Boot Backend konnte nicht gestartet werden.\n" +
                                    "Mögliche Ursachen:\n" +
                                    "- Java ist nicht im Systempfad.\n" +
                                    "- Der Pfad zur JAR-Datei ist falsch:\n  " + jarPath + "\n" +
                                    "- Port 8080 ist bereits belegt.\n" +
                                    "- Fehler beim Start des Backends (siehe Konsole).\n\n" +
                                    "Die Anwendung wird beendet.");
                    Platform.exit(); // Beendet die JavaFX-Anwendung
                });

                // Wirf eine Exception, um den Start der JavaFX-Anwendung abzubrechen
                throw new IOException("Automatischer Start des Backends fehlgeschlagen. Siehe Logs.");
            } else {
                System.out.println("[JavaFX App] Backend erfolgreich gestartet und bereit.");
            }

        } else {
            System.out.println("[JavaFX App] Automatischer Backend-Start ist deaktiviert.");
        }
    }

    /**
     * Diese Methode wird aufgerufen, nachdem init() erfolgreich war.
     * Sie erstellt und zeigt das Hauptfenster (Stage) an.
     * Läuft auf dem JavaFX Application Thread.
     */
    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("[JavaFX App] Start Methode wird aufgerufen.");
        // Lade die erste FXML-Datei (Role Selection)
        // Objects.requireNonNull stellt sicher, dass die Ressource gefunden wird, sonst gibt es eine klare NullPointerException
        Parent root = FXMLLoader.load(Objects.requireNonNull(App.class.getResource("/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml")));

        Scene scene = new Scene(root); // Größe wird oft in FXML definiert, ansonsten hier setzen
        stage.setTitle("EmpfehlungsApp");
        stage.setScene(scene);
        stage.show();
        System.out.println("[JavaFX App] Stage angezeigt.");
    }

    /**
     * Diese Methode wird aufgerufen, wenn die JavaFX-Anwendung beendet wird
     * (z.B. durch Schließen des Hauptfensters).
     * Hier können Aufräumarbeiten durchgeführt werden.
     * Läuft auf dem JavaFX Application Thread.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("[JavaFX App] Stop Methode wird aufgerufen...");
        if (backendManager != null) {
            System.out.println("[JavaFX App] Stoppe Backend-Prozess...");
            backendManager.stopBackend(); // Sende Signal zum Beenden an den Backend-Prozess
        } else {
            System.out.println("[JavaFX App] Kein Backend-Manager zum Stoppen vorhanden.");
        }
        super.stop(); // WICHTIG: Immer die Super-Methode aufrufen!
        System.out.println("[JavaFX App] Anwendung gestoppt.");
    }


    /**
     * Der Haupteinstiegspunkt der Anwendung.
     * Ruft launch() auf, um den JavaFX-Lebenszyklus zu starten.
     */
    public static void main(String[] args) {
        launch(args);
    }
}