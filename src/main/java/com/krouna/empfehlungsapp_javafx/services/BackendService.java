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
