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

    // --- Empfehlungen (Recommendations) ---

    /**
     * Sendet eine neue Empfehlung an das Backend.
     * Fügt den Authorization-Header hinzu.
     * @param dto Das DTO mit den Empfehlungsdaten.
     * @return Ein CompletableFuture mit der vereinfachten HttpResponse (Status, Body).
     */
    public CompletableFuture<com.krouna.empfehlungsapp_javafx.util.HttpResponse> submitRecommendation(RecommendationRequestDTO dto) {
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(dto);
            HttpRequest request = buildAuthenticatedPostRequest(BASE_API_URL + "/recommendations", jsonPayload);
            // Sende asynchron und mappe auf deine eigene HttpResponse-Klasse
            return client.sendAsync(request, BodyHandlers.ofString())
                    .thenApply(response -> new com.krouna.empfehlungsapp_javafx.util.HttpResponse(response.statusCode(), response.body()));
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e); // Direkt das fehlgeschlagene Future zurückgeben
        } catch (IllegalStateException e) { // Fange den Fehler vom fehlenden Token ab
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Ruft ALLE Empfehlungen vom Backend ab (nur für HR gedacht).
     * Fügt den Authorization-Header hinzu.
     * @return Eine Liste von RecommendationDTOs.
     * @throws IOException Bei Netzwerk- oder Parsing-Fehlern.
     * @throws InterruptedException Wenn der Thread unterbrochen wird.
     * @throws IllegalStateException Wenn kein Token vorhanden ist.
     */
    public List<RecommendationDTO> fetchAllRecommendations() throws IOException, InterruptedException, IllegalStateException {
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
     * @throws IllegalStateException Wenn kein Token vorhanden ist.
     */
    public List<RecommendationDTO> fetchRecommendationsForUser(Long userId) throws IOException, InterruptedException, IllegalStateException {
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


    /**
     * Aktualisiert den Status einer Empfehlung im Backend.
     * @param recommendationId Die ID der zu aktualisierenden Empfehlung.
     * @param newStatus Der neue Status als String.
     * @return true bei Erfolg, false bei Misserfolg (wird aber meist durch Exception behandelt).
     * @throws IOException Bei Netzwerk- oder Parsing-Fehlern.
     * @throws InterruptedException Wenn der Thread unterbrochen wird.
     * @throws IllegalStateException Wenn kein Token vorhanden ist.
     */
    public boolean updateRecommendationStatus(Long recommendationId, String newStatus) throws IOException, InterruptedException, IllegalStateException {
        var statusUpdateDto = new HashMap<String, String>();
        statusUpdateDto.put("status", newStatus);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(statusUpdateDto);
        } catch (JsonProcessingException e) {
            // Dieser Fehler sollte nicht auftreten, aber zur Sicherheit
            throw new IOException("Fehler beim Erstellen des Status-Update JSONs: " + e.getMessage(), e);
        }

        // Annahme: Endpunkt ist PUT /api/recommendations/{id}/status
        HttpRequest request = buildAuthenticatedRequest(
                BASE_API_URL + "/recommendations/" + recommendationId + "/status",
                "PUT", // Methode
                HttpRequest.BodyPublishers.ofString(jsonBody) // Body Publisher
        );


        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return true; // Erfolg
        } else {
            // Werfe Exception mit Details
            handleErrorResponse(response, "Fehler beim Status-Update für ID " + recommendationId);
            return false; // Wird wegen Exception nicht erreicht
        }
    }


    // --- NEUE METHODE ---
    /**
     * Sendet eine Anfrage zum Zurückziehen einer Empfehlung an das Backend.
     * Funktioniert nur für den Ersteller und wenn der Status "Eingereicht" ist.
     *
     * @param recommendationId Die ID der zurückzuziehenden Empfehlung.
     * @return Das aktualisierte RecommendationDTO bei Erfolg.
     * @throws IOException Bei Netzwerk- oder Parsing-Fehlern oder unerwarteten Serverfehlern.
     * @throws InterruptedException Wenn der Thread unterbrochen wird.
     * @throws SecurityException Wenn der Benutzer keine Berechtigung hat (HTTP 403).
     * @throws IllegalStateException Wenn die Empfehlung nicht zurückgezogen werden kann (z.B. falscher Status, HTTP 409) oder kein Token vorhanden ist.
     * @throws IllegalArgumentException Wenn die Empfehlung nicht gefunden wurde (HTTP 404).
     */
    public RecommendationDTO withdrawRecommendation(Long recommendationId)
            throws IOException, InterruptedException, SecurityException, IllegalStateException, IllegalArgumentException {

        // Verwende die zentrale Methode zum Bauen des Requests
        HttpRequest request = buildAuthenticatedRequest(
                BASE_API_URL + "/recommendations/" + recommendationId + "/withdraw",
                "PATCH", // HTTP Methode
                HttpRequest.BodyPublishers.noBody() // Kein Request Body
        );

        // Sende den Request synchron
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        // Bearbeite die Antwort
        int statusCode = response.statusCode();

        if (statusCode == 200) { // Erfolg
            try {
                // Parse das zurückgegebene DTO (angenommen, das Backend sendet es)
                if (response.body() == null || response.body().isBlank()) {
                    throw new IOException("Erfolgreiche Antwort vom Server, aber kein Body mit DTO empfangen.");
                }
                return objectMapper.readValue(response.body(), RecommendationDTO.class);
            } catch (JsonProcessingException e) {
                throw new IOException("Fehler beim Parsen der Erfolgsantwort vom Zurückziehen: " + e.getMessage(), e);
            }
        } else if (statusCode == 403) { // Forbidden
            throw new SecurityException("Keine Berechtigung zum Zurückziehen dieser Empfehlung (403). Body: " + response.body());
        } else if (statusCode == 409) { // Conflict
            throw new IllegalStateException("Empfehlung kann nicht zurückgezogen werden (z.B. falscher Status) (409). Body: " + response.body());
        } else if (statusCode == 404) { // Not Found
            throw new IllegalArgumentException("Empfehlung mit ID " + recommendationId + " nicht gefunden (404). Body: " + response.body());
        } else if (statusCode == 401) { // Unauthorized
            // Sollte durch buildAuthenticatedRequest abgefangen werden, aber zur Sicherheit
            throw new IllegalStateException("Nicht autorisiert (ungültiges Token?) (401). Body: " + response.body());
        }
        else { // Andere Fehler
            // Nutze die allgemeine Fehlerbehandlung
            handleErrorResponse(response, "Fehler beim Zurückziehen der Empfehlung ID " + recommendationId);
            // Diese Zeile wird nicht erreicht, da handleErrorResponse eine IOException wirft
            return null;
        }
    }


    // --- Private Hilfsmethoden für Requests ---

    /**
     * Zentrale Methode zum Erstellen eines authentifizierten HTTP-Requests.
     * Prüft auf Token und wirft eine IllegalStateException, wenn keins vorhanden ist.
     *
     * @param url Der Endpunkt-URL.
     * @param method Die HTTP-Methode (z.B. "GET", "POST", "PUT", "PATCH", "DELETE").
     * @param bodyPublisher Der BodyPublisher für den Request (z.B. noBody(), ofString()).
     * @return Das konfigurierte HttpRequest-Objekt.
     * @throws IllegalStateException Wenn kein gültiges Token in der UserSession gefunden wurde.
     */
    private HttpRequest buildAuthenticatedRequest(String url, String method, HttpRequest.BodyPublisher bodyPublisher) {
        String token = UserSession.getInstance().getToken();
        if (token == null || token.isBlank()) {
            System.err.println("FEHLER: Kein Token in UserSession für Request an: " + url);
            throw new IllegalStateException("Kein gültiges Token für geschützte Anfrage vorhanden.");
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token);

        // Setze Content-Type nur, wenn ein Body gesendet wird (nicht bei GET oder noBody)
        if (bodyPublisher != HttpRequest.BodyPublishers.noBody() && !method.equalsIgnoreCase("GET")) {
            builder.header("Content-Type", "application/json");
        }

        // Setze Methode und Body
        builder.method(method.toUpperCase(), bodyPublisher);

        return builder.build();
    }

    /**
     * Erstellt ein authentifiziertes GET-Request (vereinfachte Hilfsmethode).
     * @param url Die Ziel-URL.
     * @return Das HttpRequest-Objekt.
     * @throws IllegalStateException Wenn kein Token vorhanden ist.
     */
    private HttpRequest buildAuthenticatedGetRequest(String url) {
        return buildAuthenticatedRequest(url, "GET", HttpRequest.BodyPublishers.noBody());
    }

    /**
     * Erstellt ein authentifiziertes POST-Request mit JSON-Body (vereinfachte Hilfsmethode).
     * @param url Die Ziel-URL.
     * @param jsonPayload Der JSON-String für den Body.
     * @return Das HttpRequest-Objekt.
     * @throws IllegalStateException Wenn kein Token vorhanden ist.
     */
    private HttpRequest buildAuthenticatedPostRequest(String url, String jsonPayload) {
        return buildAuthenticatedRequest(url, "POST", HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8));
    }


    /**
     * Hilfsmethode zur Behandlung von Fehlerantworten (Statuscode nicht 2xx).
     * Wirft eine IOException mit Details.
     */
    private void handleErrorResponse(HttpResponse<String> response, String contextMessage) throws IOException {
        String errorDetails = String.format("%s: Status %d, Body: %s",
                contextMessage,
                response.statusCode(),
                response.body() != null && !response.body().isBlank() ? response.body() : "[Kein Body]");
        System.err.println(errorDetails); // Logge den Fehler
        throw new IOException(errorDetails); // Wirf die Exception, damit der Aufrufer sie fangen kann
    }
}