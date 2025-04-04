package com.krouna.empfehlungsapp_javafx.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
import com.krouna.empfehlungsapp_javafx.dto.UserDataDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BackendService {

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public BackendService() {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<HttpResponse<String>> login(String username, String password) {
        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        return sendPostRequest("http://localhost:8080/api/users/login", jsonPayload);
    }

    public CompletableFuture<HttpResponse<String>> submitRecommendation(RecommendationRequestDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            return sendPostRequest("http://localhost:8080/api/recommendations", json);
        } catch (IOException e) {
            CompletableFuture<HttpResponse<String>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    public List<RecommendationDTO> fetchAllRecommendations() throws IOException, InterruptedException {
        return fetchRecommendations("http://localhost:8080/api/recommendations");
    }

    public List<RecommendationDTO> fetchRecommendationsForUser(Long userId) throws IOException, InterruptedException {
        return fetchRecommendations("http://localhost:8080/api/recommendations/by-user/" + userId);
    }

    private List<RecommendationDTO> fetchRecommendations(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.body() == null || response.body().isBlank() || response.statusCode() != 200) {
            return List.of();
        }

        return Arrays.asList(objectMapper.readValue(response.body(), RecommendationDTO[].class));
    }

    public CompletableFuture<Optional<UserDataDTO>> authenticateUser(String username, String password) {
        return login(username, password).thenApply(response -> {
            if (response.statusCode() == 200) {
                try {
                    JsonNode json = objectMapper.readTree(response.body());
                    long userId = json.get("id").asLong();
                    String returnedUsername = json.get("username").asText();
                    return Optional.of(new UserDataDTO(userId, returnedUsername));
                } catch (Exception e) {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        });
    }

    public CompletableFuture<HttpResponse<String>> registerEmployee(String username, String password) {
        String jsonBody = String.format("{\"username\":\"%s\", \"password\":\"%s\", \"role\":\"MITARBEITER\"}", username, password);
        return sendPostRequest("http://localhost:8080/api/users/register-employee", jsonBody);
    }

    private CompletableFuture<HttpResponse<String>> sendPostRequest(String url, String jsonPayload) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}










//package com.krouna.empfehlungsapp_javafx.services;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
//import com.krouna.empfehlungsapp_javafx.dto.RecommendationRequestDTO;
//import com.krouna.empfehlungsapp_javafx.dto.UserDataDTO;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//
//public class BackendService {
//
//    private final HttpClient client;
//    private final ObjectMapper objectMapper;
//
//
//    public BackendService() {
//        this.client = HttpClient.newHttpClient();
//        this.objectMapper = new ObjectMapper();
//    }
//
//
//
//    /**
//     * Sendet einen Login-Request an das Backend.
//     * @param username Der Benutzername.
//     * @param password Das Passwort.
//     * @return Ein CompletableFuture, das die HttpResponse als String zurückgibt.
//     */
//    public CompletableFuture<HttpResponse<String>> login(String username, String password) {
//        // Erstelle den JSON-Payload
//        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/api/users/login"))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
//                .build();
//
//        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//    }
//
//
//    public CompletableFuture<HttpResponse<String>> submitRecommendation(RecommendationRequestDTO dto) {
//
//        try {
//            String json = objectMapper.writeValueAsString(dto);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:8080/api/recommendations"))
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
//                    .build();
//
//            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//        } catch (IOException e) {
//            e.printStackTrace();
//            CompletableFuture<HttpResponse<String>> failedFuture = new CompletableFuture<>();
//            failedFuture.completeExceptionally(e);
//            return failedFuture;
//        }
//    }
//
//    public List<RecommendationDTO> fetchAllRecommendations() throws IOException, InterruptedException {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/api/recommendations"))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        System.out.println("HR Backend Response: " + response.body()); // Debug-Ausgabe
//
//        if (response.body() == null || response.body().isBlank()) {
//            return List.of(); // Leere Liste zurückgeben
//        }
//
//        return Arrays.asList(objectMapper.readValue(response.body(), RecommendationDTO[].class));
//    }
//
//
//
//
//    /**
//     * Ruft eine Liste von Empfehlungen vom Backend ab.
//     * @return Liste von RecommendationDTOs.
//     */
//    public List<RecommendationDTO> fetchRecommendationsForUser(Long userId) throws IOException, InterruptedException {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/api/recommendations/by-user/" + userId))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        System.out.println("Backend Response: " + response.body()); // Zum Debuggen!
//
//        if (response.body() == null || response.body().isBlank()) {
//            return List.of(); // leere Liste zurückgeben, kein JSON parsen!
//        }
//
//        if (response.statusCode() != 200) {
//            System.out.println("Fehler vom Backend: " + response.statusCode());
//            return List.of();
//        }
//
//
//        RecommendationDTO[] array = objectMapper.readValue(response.body(), RecommendationDTO[].class);
//        return Arrays.asList(array);
//    }
//
//
//    public CompletableFuture<Optional<UserDataDTO>> authenticateUser(String username, String password) {
//        ObjectMapper mapper = new ObjectMapper();
//        return login(username, password).thenApply(response -> {
//            if (response.statusCode() == 200) {
//                try {
//                    JsonNode json = mapper.readTree(response.body());
//                    long userId = json.get("id").asLong();
//                    String returnedUsername = json.get("username").asText();
//                    return Optional.of(new UserDataDTO(userId, returnedUsername));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return Optional.empty();
//                }
//            } else {
//                return Optional.empty();
//            }
//        });
//    }
//
//    public CompletableFuture<HttpResponse<String>> registerEmployee(String username, String password) {
//        String jsonBody = String.format(
//                "{\"username\":\"%s\", \"password\":\"%s\", \"role\":\"MITARBEITER\"}",
//                username, password
//        );
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8080/api/users/register-employee"))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
//                .build();
//
//        return HttpClient.newHttpClient()
//                .sendAsync(request, HttpResponse.BodyHandlers.ofString());
//    }
//
//
//}
//
