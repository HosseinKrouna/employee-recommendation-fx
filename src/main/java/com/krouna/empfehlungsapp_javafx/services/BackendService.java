package com.krouna.empfehlungsapp_javafx.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class BackendService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BackendService() {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
    }

    public List<RecommendationDTO> fetchRecommendations() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/recommendations"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Mappen der JSON-Antwort auf ein Array von RecommendationDTO
        RecommendationDTO[] recommendations = objectMapper.readValue(response.body(), RecommendationDTO[].class);
        return Arrays.asList(recommendations);
    }
}

