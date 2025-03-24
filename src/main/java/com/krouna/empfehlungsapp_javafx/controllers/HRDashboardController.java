package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HRDashboardController  implements Initializable{


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
    private TableColumn<RecommendationDTO, String> submittedAtColumn; // Passe den Typ ggf. an

    @FXML
    private TableColumn<RecommendationDTO, String> recommendedByColumn;


    private final BackendService backendService = new BackendService();


    // Diese Methode wird nach dem Laden der FXML automatisch aufgerufen.
    @FXML
    public void initialize(URL location, ResourceBundle resources){
        // Hier könntest du z.B. Daten aus einer REST-API laden und in der UI darstellen.
        // Konfiguriere die Spalten, damit sie die entsprechenden Eigenschaften des DTO auslesen
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        candidateFirstnameColumn.setCellValueFactory(new PropertyValueFactory<>("candidateFirstname"));
        candidateLastnameColumn.setCellValueFactory(new PropertyValueFactory<>("candidateLastname"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        submittedAtColumn.setCellValueFactory(new PropertyValueFactory<>("submittedAt"));

        recommendedByColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRecommendedByUsername())
        );


        // Lade die Daten beim Start einmalig
        handleRefresh();
        System.out.println("HR Dashboard Controller initialisiert.");
    }

    /**
     * Wird aufgerufen, wenn der "Daten aktualisieren"-Button betätigt wird.
     * Lädt die aktuellen Empfehlungen vom Backend und aktualisiert die Tabelle.
     */
    @FXML
    private void handleRefresh() {
        try {
            Long userId = UserSession.getInstance().getUserId();
            List<RecommendationDTO> recommendations = backendService.fetchRecommendationsForUser(userId);
            recommendationsTable.setItems(FXCollections.observableArrayList(recommendations));
        } catch (IOException | InterruptedException e) {
            // Hier könntest du auch eine Fehlermeldung in der GUI anzeigen
            e.printStackTrace();
        }
    }

    // Logout-Button: Wechselt zurück zur Rollenauswahl oder zum Login.
    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
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
