package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform; // Importieren (optional, siehe Hinweis unten)
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane; // Für mögliche Style-Anpassungen
import javafx.stage.Modality; // Um sicherzustellen, dass der Dialog im Vordergrund bleibt

/**
 * Hilfsklasse zur Anzeige von Standard-Dialogen in JavaFX.
 * Stellt sicher, dass Dialoge auf dem JavaFX Application Thread angezeigt werden.
 */
public class DialogUtil {

    /**
     * Zeigt einen Fehlerdialog an.
     * Blockiert, bis der Benutzer den Dialog schließt.
     * Sollte vom JavaFX Application Thread aufgerufen werden.
     *
     * @param title   Der Titel des Dialogfensters.
     * @param message Die anzuzeigende Fehlermeldung.
     */
    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Zeigt einen Informationsdialog an.
     * Blockiert, bis der Benutzer den Dialog schließt.
     * Sollte vom JavaFX Application Thread aufgerufen werden.
     *
     * @param title   Der Titel des Dialogfensters.
     * @param message Die anzuzeigende Information.
     */
    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Zeigt einen Warnhinweis an.
     * Blockiert, bis der Benutzer den Dialog schließt.
     * Sollte vom JavaFX Application Thread aufgerufen werden.
     *
     * @param title   Der Titel des Dialogfensters.
     * @param message Der anzuzeigende Warnhinweis.
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }


    /**
     * Zeigt einen Bestätigungsdialog mit OK- und Abbrechen-Buttons an.
     * Blockiert, bis der Benutzer eine Auswahl trifft.
     * Sollte vom JavaFX Application Thread aufgerufen werden.
     *
     * @param title   Der Titel des Dialogfensters.
     * @param message Die Frage, die der Benutzer bestätigen soll.
     * @return true, wenn der Benutzer OK geklickt hat, sonst false.
     *         Gibt null zurück, wenn der Dialog nicht angezeigt werden konnte (z.B. nicht im FX-Thread).
     */
    public static boolean showConfirmation(String title, String message) {
        // Sicherstellen, dass wir auf dem FX-Thread sind, da wir einen Wert zurückgeben
        if (!Platform.isFxApplicationThread()) {
            System.err.println("WARNUNG: showConfirmation sollte vom JavaFX Application Thread aufgerufen werden.");
            // Hier könnte man überlegen, ob man einen Fehler wirft oder synchron wartet,
            // aber das kann zu Deadlocks führen. Besser ist es, den Aufrufer anzupassen.
            return false; // Oder eine Exception werfen? Für Bestätigungen ist false sicherer.
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        configureAlert(alert, title, message);
        // Standardmäßig sind OK und CANCEL bereits Buttons bei CONFIRMATION
        // alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL); // Nicht nötig

        // Zeige den Dialog an und warte auf die Antwort
        return alert.showAndWait() // Gibt Optional<ButtonType> zurück
                .filter(response -> response == ButtonType.OK) // Prüfe, ob OK geklickt wurde
                .isPresent(); // Gibt true zurück, wenn OK geklickt wurde, sonst false
    }


    // --- Private Hilfsmethode zur Konfiguration und Anzeige ---

    /**
     * Private Hilfsmethode zum Erstellen, Konfigurieren und Anzeigen eines Alerts.
     * Diese Methode stellt sicher, dass der Dialog auf dem JavaFX Application Thread angezeigt wird.
     * @param type Der Typ des Alerts (ERROR, INFORMATION, WARNING).
     * @param title Der Titel des Dialogs.
     * @param message Der anzuzeigende Inhaltstext.
     */
    private static void showAlert(Alert.AlertType type, String title, String message) {
        // Prüfen, ob wir bereits auf dem FX-Thread sind
        if (Platform.isFxApplicationThread()) {
            // Direkt anzeigen
            createAndShowAlert(type, title, message);
        } else {
            // Auf den FX-Thread verschieben
            Platform.runLater(() -> createAndShowAlert(type, title, message));
        }
    }

    /**
     * Erstellt, konfiguriert und zeigt den Alert an. Muss vom FX-Thread aufgerufen werden.
     */
    private static void createAndShowAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        configureAlert(alert, title, message);
        alert.getButtonTypes().setAll(ButtonType.OK); // Sicherstellen, dass nur OK da ist
        alert.showAndWait(); // Blockiert den FX-Thread, bis der Dialog geschlossen wird
    }

    /**
     * Zentrale Konfiguration für alle Alert-Dialoge.
     */
    private static void configureAlert(Alert alert, String title, String message) {
        alert.setTitle(title);
        alert.setHeaderText(null); // Kein Header-Text, Titel reicht oft
        alert.setContentText(message);
        // Stellt sicher, dass der Dialog modal ist (blockiert andere Fenster der Anwendung)
        alert.initModality(Modality.APPLICATION_MODAL);

        // Optional: Styling hinzufügen oder minimale/maximale Größe setzen
        // DialogPane dialogPane = alert.getDialogPane();
        // dialogPane.getStylesheets().add(DialogUtil.class.getResource("/styles/dialogs.css").toExternalForm());
        // dialogPane.setMinWidth(300);
    }
}