package com.krouna.empfehlungsapp_javafx.util;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;

public class MultipartUtils {

    public static final String BOUNDARY = "----JavaFXBoundary" + UUID.randomUUID();

    /**
     * Baut den Multipart-BodyPublisher für den Dateiupload
     */
    public static HttpRequest.BodyPublisher ofFileUpload(String fieldName, File file) throws IOException {
        var byteArrays = new ByteArrayOutputStream();
        var writer = new PrintWriter(new OutputStreamWriter(byteArrays, StandardCharsets.UTF_8), true);

        String fileName = file.getName();
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) contentType = "application/octet-stream";

        // Header
        writer.append("--").append(BOUNDARY).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"")
                .append(fieldName).append("\"; filename=\"")
                .append(fileName).append("\"\r\n");
        writer.append("Content-Type: ").append(contentType).append("\r\n\r\n");
        writer.flush();

        // Dateiinhalt
        Files.copy(file.toPath(), byteArrays);
        byteArrays.write("\r\n".getBytes(StandardCharsets.UTF_8));

        // Abschluss
        writer.append("--").append(BOUNDARY).append("--").append("\r\n");
        writer.flush();

        return HttpRequest.BodyPublishers.ofByteArray(byteArrays.toByteArray());
    }

    /**
     * Komfort-Methode zum Hochladen einer Datei inkl. Rückmeldung des Dateinamens.
     */
    public static void uploadFile(File file, Consumer<String> onSuccess) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/files/upload"))
                    .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
                    .POST(ofFileUpload("file", file))
                    .build();

            HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            // Server-Antwort auswerten
                            String savedFilename = response.body().replace("✅ Hochgeladen unter: ", "").trim();
                            onSuccess.accept(savedFilename);
                        } else {
                            System.err.println("❌ Upload fehlgeschlagen: " + response.body());
                        }
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
