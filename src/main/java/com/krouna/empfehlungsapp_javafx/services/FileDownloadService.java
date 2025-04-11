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

    // --- Konstanten ---
    private static final String BASE_URL = "http://localhost:8080"; // Basis-URL zentral definieren
    private static final String API_FILES_DOWNLOAD = "/api/files/download/";
    private static final String API_FILES_DOWNLOAD_GENERATED = "/api/files/download-generated/";
    private static final String API_RECOMMENDATIONS_PDF = "/api/recommendations/pdf";

    // --- Konfiguration ---
    // Einmaliger HttpClient für die gesamte Service-Lebensdauer
    private final HttpClient httpClient;
    // Einmaliger ObjectMapper für JSON-Konvertierung (wichtig für Performance/Konsistenz)
    private final ObjectMapper objectMapper;

    public FileDownloadService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15)) // Etwas längeres Timeout
                .build();
        this.objectMapper = new ObjectMapper();
        // Wichtig, wenn DTOs Java 8+ Zeit-Typen (LocalDate etc.) enthalten
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // --- Öffentliche Download-Methoden ---

    /**
     * Lädt eine Standarddatei (z.B. CV) vom Backend herunter.
     * @param filename Der Dateiname oder Pfad, wie er im Backend erwartet wird.
     */
    public void downloadCvFile(String filename) { // <<-- HEISST JETZT downloadCvFile
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

    /**
     * Lädt eine zuvor generierte Datei (z.B. PDF) vom Backend herunter.
     * @param filename Der Dateiname, unter dem die generierte Datei im Backend gespeichert ist.
     */
    public void downloadGeneratedFile(String filename) { // <<-- DIESE EXISTIERT
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

    /**
     * Fordert die Generierung eines PDFs basierend auf Empfehlungsdaten an und lädt es herunter.
     * @param recommendation Das DTO mit den Daten für die PDF-Generierung.
     */
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

    // --- Private Hilfsmethoden ---

    /**
     * Führt den asynchronen Download durch, zeigt den Speicher-Dialog an und speichert die Datei.
     * @param request Das vorbereitete HttpRequest-Objekt.
     * @param defaultFilename Der vorgeschlagene Dateiname im Speicher-Dialog.
     * @param filterDescription Beschreibung für den Dateityp-Filter.
     * @param filterExtensions Dateiendungen für den Filter (z.B. "*.pdf").
     */
    private void performDownloadAndSave(HttpRequest request, String defaultFilename, String filterDescription, String... filterExtensions) {
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAcceptAsync(response -> { // Weiterverarbeitung im Hintergrund
                    if (response.statusCode() == 200) {
                        // UI-Interaktion (FileChooser) muss auf dem JavaFX Application Thread laufen
                        Platform.runLater(() -> showSaveDialogAndSave(response.body(), defaultFilename, filterDescription, filterExtensions));
                    } else {
                        // Fehler vom Server -> UI-Thread für Dialog
                        Platform.runLater(() -> showErrorDialog("Download fehlgeschlagen",
                                "Server antwortete mit Status: " + response.statusCode()));
                    }
                }, Platform::runLater) // Sicherstellen, dass UI-Updates auf dem FX-Thread landen
                .exceptionally(e -> {
                    // Netzwerkfehler oder andere Probleme -> UI-Thread für Dialog
                    Platform.runLater(() -> showErrorDialog("Download Fehler",
                            "Verbindungsproblem oder unerwarteter Fehler: " + e.getMessage()));
                    e.printStackTrace(); // Fehler loggen
                    return null; // Erforderlich für exceptionally
                });
    }

    /**
     * Zeigt den FileChooser an und ruft das Speichern auf.
     * Muss auf dem JavaFX Application Thread ausgeführt werden.
     */
    private void showSaveDialogAndSave(byte[] data, String defaultFilename, String filterDescription, String... filterExtensions) {
        FileChooser fileChooser = configureFileChooser(defaultFilename, filterDescription, filterExtensions);
        File targetFile = fileChooser.showSaveDialog(new Stage()); // Braucht eine Stage, null ist nicht ideal

        if (targetFile != null) {
            saveToFileAndOpen(targetFile.toPath(), data);
        }
        // Wenn targetFile null ist, hat der Benutzer abgebrochen -> nichts tun.
    }

    /**
     * Speichert die heruntergeladenen Daten in eine Datei und versucht, sie zu öffnen.
     * @param targetPath Der Pfad zur Zieldatei.
     * @param data Die heruntergeladenen Bytes.
     */
    private void saveToFileAndOpen(Path targetPath, byte[] data) {
        try {
            Files.write(targetPath, data);
            showInfoDialog("Erfolg", "Datei erfolgreich gespeichert:\n" + targetPath.toString());
            openFileAfterSave(targetPath.toFile()); // Datei nach dem Speichern öffnen
        } catch (IOException e) {
            showErrorDialog("Speicherfehler", "Fehler beim Schreiben der Datei: " + e.getMessage());
            e.printStackTrace();
        } catch (UncheckedIOException e) {
            showErrorDialog("Speicherfehler", "Fehler beim Schreiben der Datei (Unerwartet): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Versucht, die angegebene Datei mit der Standardanwendung zu öffnen.
     * @param file Die zu öffnende Datei.
     */
    private void openFileAfterSave(File file) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                System.err.println("Desktop 'open' action not supported. File saved but not opened.");
                // Optional: Kleinen Hinweis im Erfolgsdialog oder separaten Info-Dialog
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            // Wichtig: Nur eine Warnung, da der Download erfolgreich war!
            showWarningDialog("Öffnen fehlgeschlagen", "Datei wurde heruntergeladen, konnte aber nicht automatisch geöffnet werden.\nFehler: " + ex.getMessage());
        }
    }

    /**
     * Konfiguriert und gibt einen FileChooser zurück.
     */
    private FileChooser configureFileChooser(String defaultFilename, String filterDescription, String... filterExtensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Datei speichern unter...");
        fileChooser.setInitialFileName(defaultFilename);
        if (filterExtensions != null && filterExtensions.length > 0) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterDescription, filterExtensions));
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Alle Dateien", "*.*")); // Immer anbieten
        return fileChooser;
    }

    /**
     * Erstellt ein GET HttpRequest mit Authentifizierungsheader.
     */
    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url)) // Wirft IllegalArgumentException bei ungültiger URL
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken()) // Authentifizierung!
                .GET()
                .build();
    }

    /**
     * Erstellt ein POST HttpRequest mit JSON-Body und Authentifizierungsheader.
     */
    private HttpRequest buildPostRequest(String url, String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken()) // Authentifizierung!
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
    }

    /**
     * Konvertiert ein Objekt (z.B. RecommendationDTO) in einen JSON-String.
     */
    private String convertToJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Validiert, ob ein Dateiname nicht null oder leer ist. Zeigt ggf. einen Fehlerdialog an.
     */
    private boolean validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            showErrorDialog("Fehler", "Kein Dateiname angegeben.");
            return false;
        }
        return true;
    }

    /**
     * Kodiert einen String für die Verwendung als URL-Parameter.
     */
    private String encodeUrlParameter(String parameter) {
        return URLEncoder.encode(parameter, StandardCharsets.UTF_8);
    }

    /**
     * Erstellt einen Standard-Dateinamen für das generierte PDF.
     */
    private String createPdfFilename(RecommendationDTO dto) {
        // Bereinige Namen von ungültigen Zeichen für Dateinamen
        String firstName = dto.getCandidateFirstname().replaceAll("[^a-zA-Z0-9_\\-]", "");
        String lastName = dto.getCandidateLastname().replaceAll("[^a-zA-Z0-9_\\-]", "");
        return String.format("Empfehlung_%s_%s.pdf", firstName, lastName);
    }

    /**
     * Extrahiert den Dateinamen aus einem Pfad (plattformunabhängig).
     */
    private String extractFilename(String path) {
        if (path == null) return "unbekannt";
        // Einfache Methode, die sowohl / als auch \ berücksichtigt
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return (lastSeparator >= 0) ? path.substring(lastSeparator + 1) : path;
    }


    // --- Dialog-Wrapper (um sicherzustellen, dass sie auf dem FX-Thread laufen) ---
    // Diese können optional auch in DialogUtil selbst mit Platform.runLater versehen werden.

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