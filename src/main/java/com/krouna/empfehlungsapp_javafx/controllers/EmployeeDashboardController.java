package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

public class EmployeeDashboardController {

    @FXML
    private TableView<RecommendationDTO> recommendationsTable;

    @FXML
    private TableColumn<RecommendationDTO, Long> idColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> candidateFirstnameColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> candidateLastnameColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> positionColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> statusColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> submittedAtColumn;

    private final BackendService backendService = new BackendService();

    @FXML
    public void initialize() {
        // Initialisiere die TableView-Spalten
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        candidateFirstnameColumn.setCellValueFactory(new PropertyValueFactory<>("candidateFirstname"));
        candidateLastnameColumn.setCellValueFactory(new PropertyValueFactory<>("candidateLastname"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        submittedAtColumn.setCellValueFactory(new PropertyValueFactory<>("submittedAt"));

        // Lade die Daten vom Backend
        loadRecommendations();
    }

    private void loadRecommendations() {
        try {
            List<RecommendationDTO> recommendations = backendService.fetchRecommendations();
            ObservableList<RecommendationDTO> observableList = FXCollections.observableArrayList(recommendations);
            recommendationsTable.setItems(observableList);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Hier könntest du auch eine Fehlermeldung in der UI anzeigen
        }
    }

    @FXML
    private void handleNewRecommendation() {
        // Logik zum Erstellen einer neuen Empfehlung, z.B. durch Wechseln zu einem neuen Formular
        System.out.println("Neue Empfehlung erstellen...");
    }

    @FXML
    private void handleLogout() {
        // Logik zum Ausloggen, z.B. Wechsel zur Login-Seite
        System.out.println("Logout...");
    }
}
