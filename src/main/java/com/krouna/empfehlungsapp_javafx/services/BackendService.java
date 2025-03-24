package com.krouna.empfehlungsapp_javafx.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BackendService {

    private final HttpClient client;
    private final ObjectMapper objectMapper;


    public BackendService() {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }



    /**
     * Sendet einen Login-Request an das Backend.
     * @param username Der Benutzername.
     * @param password Das Passwort.
     * @return Ein CompletableFuture, das die HttpResponse als String zurückgibt.
     */
    public CompletableFuture<HttpResponse<String>> login(String username, String password) {
        // Erstelle den JSON-Payload
        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }


    public CompletableFuture<HttpResponse<String>> submitRecommendation(RecommendationRequestDTO dto) {

        try {
            String json = objectMapper.writeValueAsString(dto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/recommendations"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
            CompletableFuture<HttpResponse<String>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }



    /**
     * Ruft eine Liste von Empfehlungen vom Backend ab.
     * @return Liste von RecommendationDTOs.
     */
    public List<RecommendationDTO> fetchRecommendationsForUser(Long userId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/recommendations/by-user/" + userId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Backend Response: " + response.body()); // Zum Debuggen!

        if (response.body() == null || response.body().isBlank()) {
            return List.of(); // leere Liste zurückgeben, kein JSON parsen!
        }

        if (response.statusCode() != 200) {
            System.out.println("Fehler vom Backend: " + response.statusCode());
            return List.of();
        }


        RecommendationDTO[] array = objectMapper.readValue(response.body(), RecommendationDTO[].class);
        return Arrays.asList(array);
    }

}

