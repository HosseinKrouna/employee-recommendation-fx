package com.krouna.empfehlungsapp_javafx.services;

// Nötige Imports
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference; // Wichtig für Listen
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// Stelle sicher, dass diese DTOs aus deinem JavaFX Projekt kommen
import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
// UserDataDTO wird nicht mehr benötigt, wenn Login direkt verarbeitet wird
// import com.krouna.empfehlungsapp_javafx.dto.UserDataDTO;
//import com.krouna.empfehlungsapp_javafx.util.HttpResponse; // Deine eigene HttpResponse-Klasse (optional)
import com.krouna.empfehlungsapp_javafx.util.UserSession; // Für den Token-Zugriff

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers; // Statischer Import
import java.net.http.HttpResponse; // Standard HttpResponse importieren
import java.nio.charset.StandardCharsets;
import java.time.Duration; // Für Timeout
import java.util.Collections; // Für leere Liste
import java.util.HashMap;
import java.util.List;
// Optional wird nicht mehr benötigt, wenn authenticateUser entfernt wird
// import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BackendService {

    // Konstante für die Basis-URL der API
    private static final String BASE_API_URL = "http://localhost:8080/api";

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public BackendService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)) // Timeout setzen
                .build();
        this.objectMapper = new ObjectMapper();
        // Konfiguriere den ObjectMapper
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // --- Authentifizierung ---

    /**
     * Sendet eine Login-Anfrage an das Backend.
     * KEIN Authorization-Header hier!
     * @param username Der Benutzername.
     * @param password Das Passwort.
     * @return Ein CompletableFuture mit der HttpResponse<String> des Servers.
     */
    public CompletableFuture<HttpResponse<String>> login(String username, String password) {
        // Verwende ein Map-Objekt für mehr Lesbarkeit und Sicherheit (vermeidet String-Formatierung)
        var loginRequest = new java.util.HashMap<String, String>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        try {
            String jsonPayload = objectMapper.writeValueAsString(loginRequest);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_API_URL + "/users/login"))
                    .header("Content-Type", "application/json")
                    // KEIN Authorization Header!
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            return client.sendAsync(request, BodyHandlers.ofString());
        } catch (JsonProcessingException e) {
            // Fehler bei der JSON-Erstellung
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sendet eine Registrierungsanfrage für einen Mitarbeiter.
     * KEIN Authorization-Header hier!
     */
    public CompletableFuture<HttpResponse<String>> registerEmployee(String username, String password) {
        var registrationRequest = new java.util.HashMap<String, String>();
        registrationRequest.put("username", username);
        registrationRequest.put("password", password);
        // Die Rolle wird im Backend gesetzt, hier nicht nötig (oder explizit "MITARBEITER" falls API es braucht)
        // registrationRequest.put("role", "MITARBEITER");

        try {
            String jsonPayload = objectMapper.writeValueAsString(registrationRequest);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_API_URL + "/users/register-employee"))
                    .header("Content-Type", "application/json")
                    // KEIN Authorization Header!
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            return client.sendAsync(request, BodyHandlers.ofString());
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // Die Methode authenticateUser ist redundant, da der Login-Prozess
    // jetzt direkt im Controller stattfindet und das DTO dort geparst wird.
    // public CompletableFuture<Optional<UserDataDTO>> authenticateUser(...) { ... }


    // --- Empfehlungen (Recommendations) ---

    /**
     * Sendet eine neue Empfehlung an das Backend.
     * Fügt den Authorization-Header hinzu.
     * @param dto Das DTO mit den Empfehlungsdaten.
     * @return Ein CompletableFuture mit der vereinfachten HttpResponse (Status, Body).
     */
    public CompletableFuture<com.krouna.empfehlungsapp_javafx.util.HttpResponse> submitRecommendation(RecommendationRequestDTO dto) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(dto);
            HttpRequest request = buildAuthenticatedPostRequest(BASE_API_URL + "/recommendations", jsonPayload);
            // Sende asynchron und mappe auf deine eigene HttpResponse-Klasse
            return client.sendAsync(request, BodyHandlers.ofString())
                    .thenApply(response -> new com.krouna.empfehlungsapp_javafx.util.HttpResponse(response.statusCode(), response.body()));
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Ruft ALLE Empfehlungen vom Backend ab (nur für HR gedacht).
     * Fügt den Authorization-Header hinzu.
     * @return Eine Liste von RecommendationDTOs.
     * @throws IOException Bei Netzwerk- oder Parsing-Fehlern.
     * @throws InterruptedException Wenn der Thread unterbrochen wird.
     */
    public List<RecommendationDTO> fetchAllRecommendations() throws IOException, InterruptedException {
        HttpRequest request = buildAuthenticatedGetRequest(BASE_API_URL + "/recommendations");
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            if (response.body() == null || response.body().isBlank()) {
                return Collections.emptyList(); // Leere Liste bei leerem Body
            }
            // Verwende TypeReference für die Liste und den korrekten DTO-Typ
            return objectMapper.readValue(response.body(), new TypeReference<List<com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO>>() {});
        } else {
            // Werfe Exception mit mehr Details
            handleErrorResponse(response, "Fehler beim Abrufen aller Empfehlungen");
            return Collections.emptyList(); // Wird nicht erreicht, da Exception geworfen wird
        }
    }

    /**
     * Ruft Empfehlungen für einen bestimmten Benutzer vom Backend ab.
     * Fügt den Authorization-Header hinzu.
     * @param userId Die ID des Benutzers.
     * @return Eine Liste von RecommendationDTOs.
     * @throws IOException Bei Netzwerk- oder Parsing-Fehlern.
     * @throws InterruptedException Wenn der Thread unterbrochen wird.
     */
    public List<RecommendationDTO> fetchRecommendationsForUser(Long userId) throws IOException, InterruptedException {
        // Der Pfad im Backend war laut Controller "/api/recommendations/user/{userId}", nicht "by-user"
        HttpRequest request = buildAuthenticatedGetRequest(BASE_API_URL + "/recommendations/user/" + userId);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            if (response.body() == null || response.body().isBlank()) {
                return Collections.emptyList(); // Leere Liste bei leerem Body
            }
            // Verwende TypeReference für die Liste und den korrekten DTO-Typ
            return objectMapper.readValue(response.body(), new TypeReference<List<com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO>>() {});
        } else {
            // Werfe Exception mit mehr Details
            handleErrorResponse(response, "Fehler beim Abrufen der Benutzerepfehlungen für ID " + userId);
            return Collections.emptyList(); // Wird nicht erreicht
        }
    }

    // Die alte fetchRecommendations ist jetzt in den spezifischen Methoden integriert.
    // private List<RecommendationDTO> fetchRecommendations(String url) ...


    // --- Private Hilfsmethoden für Requests ---

    /**
     * Erstellt ein GET-Request mit Authorization-Header.
     * @param url Die Ziel-URL.
     * @return Das HttpRequest-Objekt.
     */
    private HttpRequest buildAuthenticatedGetRequest(String url) {
        String token = UserSession.getInstance().getToken();
        // Füge Header nur hinzu, wenn Token vorhanden ist (optional, aber sicherer)
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        } else {
            System.err.println("WARNUNG: Kein Token in UserSession für GET Request an: " + url);
            // Hier könnte man auch eine Exception werfen, wenn ein Token erwartet wird.
        }
        return builder.GET().build();
    }

    /**
     * Erstellt ein POST-Request mit JSON-Body und Authorization-Header.
     * @param url Die Ziel-URL.
     * @param jsonPayload Der JSON-String für den Body.
     * @return Das HttpRequest-Objekt.
     */
    private HttpRequest buildAuthenticatedPostRequest(String url, String jsonPayload) {
        String token = UserSession.getInstance().getToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        } else {
            System.err.println("WARNUNG: Kein Token in UserSession für POST Request an: " + url);
            // Hier sollte man wahrscheinlich eine Exception werfen, da POSTs meist geschützt sind.
            // throw new IllegalStateException("Kein gültiges Token für geschützte Anfrage vorhanden.");
        }
        return builder.POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
    }


    // In BackendService.java

    /**
     * Aktualisiert den Status einer Empfehlung im Backend.
     * @param recommendationId Die ID der zu aktualisierenden Empfehlung.
     * @param newStatus Der neue Status als String.
     * @return true bei Erfolg, false bei Misserfolg.
     * @throws IOException Bei Netzwerkfehlern.
     * @throws InterruptedException Wenn der Thread unterbrochen wird.
     */
    public boolean updateRecommendationStatus(Long recommendationId, String newStatus) throws IOException, InterruptedException {
        // Erstelle den Request Body (z.B. als JSON mit dem neuen Status)
        // Einfache Variante: Nur den Status senden
        // String jsonBody = String.format("{\"status\":\"%s\"}", newStatus);
        // Besser: Ein kleines DTO verwenden
        var statusUpdateDto = new HashMap<String, String>();
        statusUpdateDto.put("status", newStatus);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(statusUpdateDto);
        } catch (JsonProcessingException e) {
            System.err.println("Fehler beim Erstellen des Status-Update JSONs: " + e.getMessage());
            return false; // Oder Exception werfen
        }


        // Erstelle den PUT oder PATCH Request (PUT ist oft für komplettes Update, PATCH für Teilupdate)
        // Annahme: Endpunkt ist PUT /api/recommendations/{id}/status
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_API_URL + "/recommendations/" + recommendationId + "/status")) // Beispiel-Endpunkt
                .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody)) // Oder .method("PATCH", ...)
                .build();

        // Sende den Request synchron (oder asynchron, wenn gewünscht)
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        // Prüfe den Statuscode
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return true; // Erfolg
        } else {
            System.err.println("Fehler beim Status-Update für ID " + recommendationId + ": Status " + response.statusCode() + ", Body: " + response.body());
            handleErrorResponse(response, "Fehler beim Status-Update für ID " + recommendationId); // Wirft IOException
            return false; // Wird wegen Exception nicht erreicht
        }
    }

    /**
     * Hilfsmethode zur Behandlung von Fehlerantworten (Statuscode != 200).
     * Wirft eine IOException mit Details.
     */
    private void handleErrorResponse(HttpResponse<String> response, String contextMessage) throws IOException {
        String errorDetails = String.format("%s: Status %d, Body: %s",
                contextMessage,
                response.statusCode(),
                response.body() != null ? response.body() : "N/A");
        System.err.println(errorDetails); // Logge den Fehler
        throw new IOException(errorDetails);
    }
}