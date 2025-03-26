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

    public void downloadFile(String filename) {
        if (filename == null || filename.isBlank()) {
            System.out.println("⚠️ Kein Dateiname zum Herunterladen.");
            return;
        }

        try {
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            String downloadUrl = "http://localhost:8080/api/files/download/" + encodedFilename;

            HttpClient.newHttpClient().sendAsync(
                    HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build(),
                    HttpResponse.BodyHandlers.ofByteArray()
            ).thenAccept(response -> {
                if (response.statusCode() == 200) {
                    Platform.runLater(() -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Speichern unter");
                        fileChooser.setInitialFileName(filename);
                        fileChooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter("PDF-Datei", "*.pdf")
                        );

                        File targetFile = fileChooser.showSaveDialog(null);
                        if (targetFile != null) {
                            try {
                                Files.write(targetFile.toPath(), response.body());
                                System.out.println("📥 Datei gespeichert unter: " + targetFile.getAbsolutePath());
                            } catch (IOException e) {
                                e.printStackTrace();
                                DialogUtil.showError("Error", "Fehler beim Speichern der Datei!");
                            }
                        } else {
                            System.out.println("❌ Speichern abgebrochen.");
                        }
                    });
                } else {
                    System.err.println("❌ Download fehlgeschlagen: " + response.statusCode());
                    DialogUtil.showError("Error", "Download fehlgeschlagen (Status: " + response.statusCode() + ")");
                }
            }).exceptionally(e -> {
                e.printStackTrace();
                DialogUtil.showError("Error", "Verbindungsfehler beim Herunterladen!");
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "Interner Fehler beim Herunterladen!");
        }
    }
}
