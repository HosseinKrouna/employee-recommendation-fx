package com.krouna.empfehlungsapp_javafx.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Wichtig für LocalDate etc.
import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession; // Annahme: Für Token
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage; // Stage für FileChooser benötigt

import java.awt.Desktop; // Für das Öffnen nach dem Speichern
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class FileDownloadService {


    private static final String BASE_URL = "http://localhost:8080";
    private static final String API_FILES_DOWNLOAD = "/api/files/download/";
    private static final String API_FILES_DOWNLOAD_GENERATED = "/api/files/download-generated/";
    private static final String API_RECOMMENDATIONS_PDF = "/api/recommendations/pdf";


    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public FileDownloadService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();

        this.objectMapper.registerModule(new JavaTimeModule());
    }



    public void downloadCvFile(String filename) {
        if (!validateFilename(filename)) return;
        try {
            String encodedFilename = encodeUrlParameter(filename);
            HttpRequest request = buildGetRequest(BASE_URL + API_FILES_DOWNLOAD + encodedFilename);
            String suggestedFileName = extractFilename(filename);
            performDownloadAndSave(request, suggestedFileName, "Datei", "*.*");
        } catch (IllegalArgumentException e) {
            showErrorDialog("Interner Fehler", "Ungültige URL erstellt: " + e.getMessage());
        }
    }


    public void downloadGeneratedFile(String filename) {
        if (!validateFilename(filename)) return;
        try {
            String encodedFilename = encodeUrlParameter(filename);
            HttpRequest request = buildGetRequest(BASE_URL + API_FILES_DOWNLOAD_GENERATED + encodedFilename);
            String suggestedFileName = extractFilename(filename);
            performDownloadAndSave(request, suggestedFileName, "PDF-Datei", "*.pdf");
        } catch (IllegalArgumentException e) {
            showErrorDialog("Interner Fehler", "Ungültige URL erstellt: " + e.getMessage());
        }
    }


    public void downloadRecommendationPdf(RecommendationDTO recommendation) {
        if (recommendation == null) {
            showErrorDialog("Fehler", "Keine Empfehlungsdaten für PDF-Generierung vorhanden.");
            return;
        }

        try {
            String jsonPayload = convertToJson(recommendation);
            HttpRequest request = buildPostRequest(BASE_URL + API_RECOMMENDATIONS_PDF, jsonPayload);
            String suggestedFileName = createPdfFilename(recommendation);
            performDownloadAndSave(request, suggestedFileName, "PDF-Datei", "*.pdf");
        } catch (JsonProcessingException e) {
            showErrorDialog("Fehler", "Konnte Empfehlungsdaten nicht für den Server vorbereiten: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showErrorDialog("Interner Fehler", "Ungültige URL erstellt: " + e.getMessage());
        }
    }




    private void performDownloadAndSave(HttpRequest request, String defaultFilename, String filterDescription, String... filterExtensions) {
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAcceptAsync(response -> {
                    if (response.statusCode() == 200) {

                        Platform.runLater(() -> showSaveDialogAndSave(response.body(), defaultFilename, filterDescription, filterExtensions));
                    } else {

                        Platform.runLater(() -> showErrorDialog("Download fehlgeschlagen",
                                "Server antwortete mit Status: " + response.statusCode()));
                    }
                }, Platform::runLater)
                .exceptionally(e -> {

                    Platform.runLater(() -> showErrorDialog("Download Fehler",
                            "Verbindungsproblem oder unerwarteter Fehler: " + e.getMessage()));
                    e.printStackTrace();
                    return null;
                });
    }


    private void showSaveDialogAndSave(byte[] data, String defaultFilename, String filterDescription, String... filterExtensions) {
        FileChooser fileChooser = configureFileChooser(defaultFilename, filterDescription, filterExtensions);
        File targetFile = fileChooser.showSaveDialog(new Stage());

        if (targetFile != null) {
            saveToFileAndOpen(targetFile.toPath(), data);
        }

    }


    private void saveToFileAndOpen(Path targetPath, byte[] data) {
        try {
            Files.write(targetPath, data);
            showInfoDialog("Erfolg", "Datei erfolgreich gespeichert:\n" + targetPath.toString());
            openFileAfterSave(targetPath.toFile());
        } catch (IOException e) {
            showErrorDialog("Speicherfehler", "Fehler beim Schreiben der Datei: " + e.getMessage());
            e.printStackTrace();
        } catch (UncheckedIOException e) {
            showErrorDialog("Speicherfehler", "Fehler beim Schreiben der Datei (Unerwartet): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openFileAfterSave(File file) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                System.err.println("Desktop 'open' action not supported. File saved but not opened.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();

            showWarningDialog("Öffnen fehlgeschlagen", "Datei wurde heruntergeladen, konnte aber nicht automatisch geöffnet werden.\nFehler: " + ex.getMessage());
        }
    }


    private FileChooser configureFileChooser(String defaultFilename, String filterDescription, String... filterExtensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Datei speichern unter...");
        fileChooser.setInitialFileName(defaultFilename);
        if (filterExtensions != null && filterExtensions.length > 0) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterDescription, filterExtensions));
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Alle Dateien", "*.*"));
        return fileChooser;
    }


    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .GET()
                .build();
    }


    private HttpRequest buildPostRequest(String url, String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
    }


    private String convertToJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }


    private boolean validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            showErrorDialog("Fehler", "Kein Dateiname angegeben.");
            return false;
        }
        return true;
    }


    private String encodeUrlParameter(String parameter) {
        return URLEncoder.encode(parameter, StandardCharsets.UTF_8);
    }


    private String createPdfFilename(RecommendationDTO dto) {
        // Bereinige Namen von ungültigen Zeichen für Dateinamen
        String firstName = dto.getCandidateFirstname().replaceAll("[^a-zA-Z0-9_\\-]", "");
        String lastName = dto.getCandidateLastname().replaceAll("[^a-zA-Z0-9_\\-]", "");
        return String.format("Empfehlung_%s_%s.pdf", firstName, lastName);
    }


    private String extractFilename(String path) {
        if (path == null) return "unbekannt";
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return (lastSeparator >= 0) ? path.substring(lastSeparator + 1) : path;
    }




    private void showInfoDialog(String title, String content) {
        Platform.runLater(() -> DialogUtil.showInfo(title, content));
    }

    private void showErrorDialog(String title, String content) {
        Platform.runLater(() -> DialogUtil.showError(title, content));
    }
    private void showWarningDialog(String title, String content) {
        Platform.runLater(() -> DialogUtil.showWarning(title, content));
    }
}