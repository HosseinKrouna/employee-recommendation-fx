package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class EmployeeDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label errorLabel;


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
        String currentUsername = UserSession.getInstance().getUsername();
        System.out.println("EmployeeDashboardController: currentUsername = " + currentUsername);
        welcomeLabel.setText("Willkommen, " + currentUsername + "!");
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
            String username = UserSession.getInstance().getUsername();
            Long userId = UserSession.getInstance().getUserId();

            List<RecommendationDTO> recommendations = backendService.fetchRecommendationsForUser(userId);
            ObservableList<RecommendationDTO> observableList = FXCollections.observableArrayList(recommendations);
            recommendationsTable.setItems(observableList);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            if (errorLabel != null) {
                errorLabel.setText("Fehler beim Laden der Empfehlungen.");
            }
        }
    }


    @FXML
    private void handleNewRecommendation(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/employee-new-recommendation-view.fxml");
        System.out.println("Neue Empfehlung erstellen...");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().setUsername(null);
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
        System.out.println("Logout...");
    }


    /**
     * Hilfsmethode zum Wechseln der Scene.
     *
     * @param event    Das auslösende ActionEvent.
     * @param fxmlPath Der Pfad zur FXML-Datei, zu der gewechselt werden soll.
     */
    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Übernehme die aktuelle Größe des Fensters:
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
