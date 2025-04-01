package com.krouna.empfehlungsapp_javafx.services;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
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

    public void downloadPdf(RecommendationDTO recommendation) {
        try {
            // Empfehlung in JSON konvertieren (z.B. mit Jackson)
            String jsonPayload = convertRecommendationToJson(recommendation);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/recommendations/pdf"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .thenAccept(response -> Platform.runLater(() -> handlePdfResponse(response,
                            "Empfehlung_" + recommendation.getCandidateFirstname() + "_" + recommendation.getCandidateLastname() + ".pdf")))
                    .exceptionally(e -> {
                        Platform.runLater(() -> DialogUtil.showError("Fehler", "Verbindungsfehler beim Herunterladen des PDFs."));
                        return null;
                    });
        } catch (Exception e) {
            Platform.runLater(() -> DialogUtil.showError("Fehler", "Fehler bei der Verarbeitung der Empfehlung."));
        }
    }

    private void handlePdfResponse(HttpResponse<byte[]> response, String defaultFilename) {
        if (response.statusCode() == 200) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("PDF speichern unter");
            fileChooser.setInitialFileName(defaultFilename);
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

    // Beispielhafte Methode zur Konvertierung in JSON (mit Jackson)
    private String convertRecommendationToJson(RecommendationDTO recommendation) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(recommendation);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public void downloadGeneratedFile(String filename) {
        if (filename == null || filename.isBlank()) {
            DialogUtil.showError("Fehler", "Kein Dateiname angegeben.");
            return;
        }

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        // Neuer Endpoint für generierte PDFs:
        String downloadUrl = "http://localhost:8080/api/files/download-generated/" + encodedFilename;

        client.sendAsync(
                        HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                ).thenAccept(response -> Platform.runLater(() -> handleResponse(response, filename)))
                .exceptionally(e -> {
                    Platform.runLater(() -> DialogUtil.showError("Fehler", "Verbindungsfehler beim Herunterladen."));
                    return null;
                });
    }


}