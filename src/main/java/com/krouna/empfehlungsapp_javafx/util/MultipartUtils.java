package com.krouna.empfehlungsapp_javafx.util;

import javafx.application.Platform;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;

public class MultipartUtils {

    private static final String BOUNDARY = "----JavaFXBoundary" + UUID.randomUUID();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static HttpRequest.BodyPublisher ofFileUpload(String fieldName, File file) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteStream, StandardCharsets.UTF_8), true);

        String fileName = file.getName();
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) contentType = "application/octet-stream";

        writer.append("--").append(BOUNDARY).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"")
                .append(fieldName).append("\"; filename=\"")
                .append(fileName).append("\"\r\n");
        writer.append("Content-Type: ").append(contentType).append("\r\n\r\n");
        writer.flush();

        Files.copy(file.toPath(), byteStream);
        byteStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

        writer.append("--").append(BOUNDARY).append("--").append("\r\n");
        writer.flush();

        return HttpRequest.BodyPublishers.ofByteArray(byteStream.toByteArray());
    }

    public static void uploadFile(File file, Consumer<String> onSuccess) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/files/upload"))
                    .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
                    .POST(ofFileUpload("file", file))
                    .build();

            CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        String savedFilename = response.body().replace("✅ Hochgeladen unter: ", "").trim();
                        onSuccess.accept(savedFilename);
                        DialogUtil.showInfo("Erfolg", "Datei erfolgreich hochgeladen.");
                    } else {
                        DialogUtil.showError("Fehler", "Upload fehlgeschlagen: " + response.body());
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> DialogUtil.showError("Fehler", "Verbindungsfehler beim Hochladen."));
                return null;
            });

        } catch (Exception e) {
            Platform.runLater(() -> DialogUtil.showError("Fehler", "Interner Fehler beim Hochladen."));
        }
    }
}
