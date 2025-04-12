package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path; // Path verwenden
import java.time.Duration; // Timeout hinzufügen
import java.util.List; // Für ofByteArrays
import java.util.UUID;
import java.util.function.Consumer;
import static java.net.http.HttpResponse.BodyHandlers;
// import java.net.http.HttpResponse; // Wird nicht mehr direkt importiert, da im Lambda verwendet

public class MultipartUtils {

    // --- Konstanten und Konfiguration ---
    private static final String UPLOAD_URL = "http://localhost:8080/api/files/upload"; // Upload-URL zentral
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30)) // Upload-Timeout erhöhen
            .build();

    // --- Hilfsmethode zum Erstellen des Multipart-Bodies (Überarbeitet für Klarheit/Korrektheit) ---
    private static HttpRequest.BodyPublisher ofMimeMultipartData(String fieldName, Path path, String boundary) throws IOException {
        byte[] fileBytes = Files.readAllBytes(path);
        String filename = path.getFileName().toString();
        String contentType = Files.probeContentType(path);
        if (contentType == null) contentType = "application/octet-stream"; // Default Content-Type

        // Erstelle die Teile als Byte-Arrays
        byte[] separator = ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] header = ("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n" +
                "Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] trailer = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        // Kombiniere die Teile
        return HttpRequest.BodyPublishers.ofByteArrays(List.of(
                separator,
                header,
                fileBytes,
                trailer
        ));
    }


    // --- ANGEPASSTE uploadFile Methode ---
    public static void uploadFile(File file, // Die zu uploadende Datei
                                  Consumer<String> onSuccess, // Callback bei Erfolg (bekommt Server-Dateinamen)
                                  Consumer<String> onError) { // Callback bei Fehler (bekommt Fehlermeldung)

        if (file == null || !file.exists() || !file.isFile()) {
            // Rufe den Fehler-Callback direkt auf, wenn die Datei ungültig ist
            Platform.runLater(() -> onError.accept("Ungültige Datei ausgewählt oder Datei nicht gefunden."));
            return;
        }

        String boundary = "Boundary-" + UUID.randomUUID().toString();
        Path filePath = file.toPath();

        try {
            // 1. Erstelle den Request Body
            HttpRequest.BodyPublisher bodyPublisher = ofMimeMultipartData("file", filePath, boundary); // Annahme: Feldname ist "file"

            // 2. Baue den Request MIT AUTHENTIFIZIERUNG
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UPLOAD_URL))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    // ---> WICHTIG: Authorization Header hinzufügen <---
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .POST(bodyPublisher)
                    .build();

            // 3. Sende den Request asynchron
            CLIENT.sendAsync(request, BodyHandlers.ofString())
                    .thenAccept(response -> {
                        // Verarbeitung der Antwort auf dem FX Application Thread
                        Platform.runLater(() -> {
                            if (response.statusCode() >= 200 && response.statusCode() < 300) { // Erfolg (2xx Status)
                                // Annahme: Backend gibt gespeicherten Namen/Pfad im Body zurück
                                String savedFilename = response.body();
                                // Optional: Bereinige Antwort, falls nötig (wie vorher)
                                if (savedFilename != null && savedFilename.contains("✅ Hochgeladen unter: ")) {
                                    savedFilename = savedFilename.replace("✅ Hochgeladen unter: ", "").trim();
                                }

                                if (savedFilename != null && !savedFilename.isBlank()) {
                                    onSuccess.accept(savedFilename); // Rufe Erfolgs-Callback auf
                                } else {
                                    // Server gab Erfolgscode, aber keinen Namen zurück?
                                    onError.accept("Upload erfolgreich, aber Server gab keinen Dateinamen zurück (Body: " + response.body() + ")");
                                }
                            } else {
                                // Fehler vom Server (nicht 2xx) -> Rufe Fehler-Callback auf
                                onError.accept("Upload fehlgeschlagen: Status " + response.statusCode() + ", Antwort: " + response.body());
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        // Netzwerkfehler oder andere Exceptions beim Senden/Empfangen
                        Platform.runLater(() -> {
                            onError.accept("Verbindungsfehler beim Hochladen: " + ex.getMessage());
                            ex.printStackTrace(); // Logge den Fehler
                        });
                        return null;
                    });

        } catch (IOException e) {
            // Fehler beim Lesen der Datei oder Erstellen des Request-Bodys
            Platform.runLater(() -> {
                onError.accept("Interner Fehler beim Vorbereiten des Uploads: " + e.getMessage());
                e.printStackTrace(); // Logge den Fehler
            });
        } catch (Exception e) {
            // Fange andere unerwartete Fehler ab
            Platform.runLater(() -> {
                onError.accept("Unerwarteter interner Fehler: " + e.getMessage());
                e.printStackTrace(); // Logge den Fehler
            });
        }
    }
}