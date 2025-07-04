package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import static java.net.http.HttpResponse.BodyHandlers;


public class MultipartUtils {


    private static final String UPLOAD_URL = "http://localhost:8080/api/files/upload";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();


    private static HttpRequest.BodyPublisher ofMimeMultipartData(String fieldName, Path path, String boundary) throws IOException {
        byte[] fileBytes = Files.readAllBytes(path);
        String filename = path.getFileName().toString();
        String contentType = Files.probeContentType(path);
        if (contentType == null) contentType = "application/octet-stream";

        byte[] separator = ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] header = ("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n" +
                "Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] trailer = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);


        return HttpRequest.BodyPublishers.ofByteArrays(List.of(
                separator,
                header,
                fileBytes,
                trailer
        ));
    }



    public static void uploadFile(File file,
                                  Consumer<String> onSuccess,
                                  Consumer<String> onError) {

        if (file == null || !file.exists() || !file.isFile()) {

            Platform.runLater(() -> onError.accept("Ungültige Datei ausgewählt oder Datei nicht gefunden."));
            return;
        }

        String boundary = "Boundary-" + UUID.randomUUID().toString();
        Path filePath = file.toPath();

        try {

            HttpRequest.BodyPublisher bodyPublisher = ofMimeMultipartData("file", filePath, boundary); // Annahme: Feldname ist "file"


            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UPLOAD_URL))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)

                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .POST(bodyPublisher)
                    .build();


            CLIENT.sendAsync(request, BodyHandlers.ofString())
                    .thenAccept(response -> {

                        Platform.runLater(() -> {
                            if (response.statusCode() >= 200 && response.statusCode() < 300) {

                                String savedFilename = response.body();

                                if (savedFilename != null && savedFilename.contains("✅ Hochgeladen unter: ")) {
                                    savedFilename = savedFilename.replace("✅ Hochgeladen unter: ", "").trim();
                                }

                                if (savedFilename != null && !savedFilename.isBlank()) {
                                    onSuccess.accept(savedFilename);
                                } else {

                                    onError.accept("Upload erfolgreich, aber Server gab keinen Dateinamen zurück (Body: " + response.body() + ")");
                                }
                            } else {

                                onError.accept("Upload fehlgeschlagen: Status " + response.statusCode() + ", Antwort: " + response.body());
                            }
                        });
                    })
                    .exceptionally(ex -> {

                        Platform.runLater(() -> {
                            onError.accept("Verbindungsfehler beim Hochladen: " + ex.getMessage());
                            ex.printStackTrace();
                        });
                        return null;
                    });

        } catch (IOException e) {

            Platform.runLater(() -> {
                onError.accept("Interner Fehler beim Vorbereiten des Uploads: " + e.getMessage());
                e.printStackTrace();
            });
        } catch (Exception e) {

            Platform.runLater(() -> {
                onError.accept("Unerwarteter interner Fehler: " + e.getMessage());
                e.printStackTrace();
            });
        }
    }
}