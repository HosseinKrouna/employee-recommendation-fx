package com.krouna.empfehlungsapp_javafx.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.util.UserSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BackendService {


    private static final String BASE_API_URL = "http://localhost:8080/api";

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public BackendService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }




    public CompletableFuture<HttpResponse<String>> login(String username, String password) {

        var loginRequest = new java.util.HashMap<String, String>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        try {
            String jsonPayload = objectMapper.writeValueAsString(loginRequest);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_API_URL + "/users/login"))
                    .header("Content-Type", "application/json")

                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            return client.sendAsync(request, BodyHandlers.ofString());
        } catch (JsonProcessingException e) {

            return CompletableFuture.failedFuture(e);
        }
    }


    public CompletableFuture<HttpResponse<String>> registerEmployee(String username, String password) {
        var registrationRequest = new java.util.HashMap<String, String>();
        registrationRequest.put("username", username);
        registrationRequest.put("password", password);

        try {
            String jsonPayload = objectMapper.writeValueAsString(registrationRequest);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_API_URL + "/users/register-employee"))
                    .header("Content-Type", "application/json")

                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            return client.sendAsync(request, BodyHandlers.ofString());
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }




    public CompletableFuture<com.krouna.empfehlungsapp_javafx.util.HttpResponse> submitRecommendation(RecommendationRequestDTO dto) {
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(dto);
            HttpRequest request = buildAuthenticatedPostRequest(BASE_API_URL + "/recommendations", jsonPayload);

            return client.sendAsync(request, BodyHandlers.ofString())
                    .thenApply(response -> new com.krouna.empfehlungsapp_javafx.util.HttpResponse(response.statusCode(), response.body()));
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        } catch (IllegalStateException e) {
            return CompletableFuture.failedFuture(e);
        }
    }


    public List<RecommendationDTO> fetchAllRecommendations() throws IOException, InterruptedException, IllegalStateException {
        HttpRequest request = buildAuthenticatedGetRequest(BASE_API_URL + "/recommendations");
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            if (response.body() == null || response.body().isBlank()) {
                return Collections.emptyList();
            }

            return objectMapper.readValue(response.body(), new TypeReference<List<com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO>>() {});
        } else {

            handleErrorResponse(response, "Fehler beim Abrufen aller Empfehlungen");
            return Collections.emptyList();
        }
    }


    public List<RecommendationDTO> fetchRecommendationsForUser(Long userId) throws IOException, InterruptedException, IllegalStateException {
        HttpRequest request = buildAuthenticatedGetRequest(BASE_API_URL + "/recommendations/user/" + userId);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            if (response.body() == null || response.body().isBlank()) {
                return Collections.emptyList();
            }

            return objectMapper.readValue(response.body(), new TypeReference<List<com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO>>() {});
        } else {

            handleErrorResponse(response, "Fehler beim Abrufen der Benutzerepfehlungen für ID " + userId);
            return Collections.emptyList();
        }
    }



    public boolean updateRecommendationStatus(Long recommendationId, String newStatus) throws IOException, InterruptedException, IllegalStateException {
        var statusUpdateDto = new HashMap<String, String>();
        statusUpdateDto.put("status", newStatus);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(statusUpdateDto);
        } catch (JsonProcessingException e) {

            throw new IOException("Fehler beim Erstellen des Status-Update JSONs: " + e.getMessage(), e);
        }

        HttpRequest request = buildAuthenticatedRequest(
                BASE_API_URL + "/recommendations/" + recommendationId + "/status",
                "PUT",
                HttpRequest.BodyPublishers.ofString(jsonBody)
        );


        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return true;
        } else {

            handleErrorResponse(response, "Fehler beim Status-Update für ID " + recommendationId);
            return false;
        }
    }



    public RecommendationDTO withdrawRecommendation(Long recommendationId)
            throws IOException, InterruptedException, SecurityException, IllegalStateException, IllegalArgumentException {


        HttpRequest request = buildAuthenticatedRequest(
                BASE_API_URL + "/recommendations/" + recommendationId + "/withdraw",
                "PATCH",
                HttpRequest.BodyPublishers.noBody()
        );


        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());


        int statusCode = response.statusCode();

        if (statusCode == 200) {
            try {

                if (response.body() == null || response.body().isBlank()) {
                    throw new IOException("Erfolgreiche Antwort vom Server, aber kein Body mit DTO empfangen.");
                }
                return objectMapper.readValue(response.body(), RecommendationDTO.class);
            } catch (JsonProcessingException e) {
                throw new IOException("Fehler beim Parsen der Erfolgsantwort vom Zurückziehen: " + e.getMessage(), e);
            }
        } else if (statusCode == 403) {
            throw new SecurityException("Keine Berechtigung zum Zurückziehen dieser Empfehlung (403). Body: " + response.body());
        } else if (statusCode == 409) {
            throw new IllegalStateException("Empfehlung kann nicht zurückgezogen werden (z.B. falscher Status) (409). Body: " + response.body());
        } else if (statusCode == 404) {
            throw new IllegalArgumentException("Empfehlung mit ID " + recommendationId + " nicht gefunden (404). Body: " + response.body());
        } else if (statusCode == 401) {

            throw new IllegalStateException("Nicht autorisiert (ungültiges Token?) (401). Body: " + response.body());
        }
        else {

            handleErrorResponse(response, "Fehler beim Zurückziehen der Empfehlung ID " + recommendationId);

            return null;
        }
    }





    private HttpRequest buildAuthenticatedRequest(String url, String method, HttpRequest.BodyPublisher bodyPublisher) {
        String token = UserSession.getInstance().getToken();
        if (token == null || token.isBlank()) {
            System.err.println("FEHLER: Kein Token in UserSession für Request an: " + url);
            throw new IllegalStateException("Kein gültiges Token für geschützte Anfrage vorhanden.");
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token);


        if (bodyPublisher != HttpRequest.BodyPublishers.noBody() && !method.equalsIgnoreCase("GET")) {
            builder.header("Content-Type", "application/json");
        }


        builder.method(method.toUpperCase(), bodyPublisher);

        return builder.build();
    }


    private HttpRequest buildAuthenticatedGetRequest(String url) {
        return buildAuthenticatedRequest(url, "GET", HttpRequest.BodyPublishers.noBody());
    }


    private HttpRequest buildAuthenticatedPostRequest(String url, String jsonPayload) {
        return buildAuthenticatedRequest(url, "POST", HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8));
    }



    private void handleErrorResponse(HttpResponse<String> response, String contextMessage) throws IOException {
        String errorDetails = String.format("%s: Status %d, Body: %s",
                contextMessage,
                response.statusCode(),
                response.body() != null && !response.body().isBlank() ? response.body() : "[Kein Body]");
        System.err.println(errorDetails);
        throw new IOException(errorDetails);
    }
}