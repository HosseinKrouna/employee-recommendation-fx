package com.krouna.empfehlungsapp_javafx.services;

import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import javafx.application.Platform;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileDownloadService {

    private final HttpClient client = HttpClient.newHttpClient();

    public void downloadFile(String filename) {
        if (filename == null || filename.isBlank()) {
            DialogUtil.showError("Fehler", "Kein Dateiname angegeben.");
            return;
        }

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        String downloadUrl = "http://localhost:8080/api/files/download/" + encodedFilename;

        client.sendAsync(
                        HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                ).thenAccept(response -> Platform.runLater(() -> handleResponse(response, filename)))
                .exceptionally(e -> {
                    Platform.runLater(() -> DialogUtil.showError("Fehler", "Verbindungsfehler beim Herunterladen."));
                    return null;
                });
    }

    private void handleResponse(HttpResponse<byte[]> response, String filename) {
        if (response.statusCode() == 200) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Speichern unter");
            fileChooser.setInitialFileName(filename);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF-Datei", "*.pdf")
            );

            File targetFile = fileChooser.showSaveDialog(null);
            if (targetFile != null) {
                saveToFile(targetFile, response.body());
            }
        } else {
            DialogUtil.showError("Fehler", "Download fehlgeschlagen (Status: " + response.statusCode() + ")");
        }
    }

    private void saveToFile(File targetFile, byte[] data) {
        try {
            Files.write(targetFile.toPath(), data);
            DialogUtil.showInfo("Erfolg", "Datei erfolgreich gespeichert.");
        } catch (IOException e) {
            DialogUtil.showError("Fehler", "Fehler beim Speichern der Datei.");
        }
    }
}