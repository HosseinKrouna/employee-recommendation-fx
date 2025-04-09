package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.services.FileDownloadService;
import com.krouna.empfehlungsapp_javafx.ui.cells.DownloadButtonTableCell;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class EmployeeDashboardController implements Initializable {

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
    @FXML
    private TableColumn<RecommendationDTO, String> cvFileColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> businessLinkColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> pdfFileColumn;


    private final BackendService backendService = new BackendService();
    private final FileDownloadService fileDownloadService = new FileDownloadService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        welcomeLabel.setText("Willkommen, " + UserSession.getInstance().getUsername() + "!");
        setupTableView();
        loadRecommendations();
    }

    private void setupTableView() {
        setupColumn(idColumn, "id");
        setupColumn(candidateFirstnameColumn, "candidateFirstname");
        setupColumn(candidateLastnameColumn, "candidateLastname");
        setupColumn(positionColumn, "position");
        setupColumn(statusColumn, "status");
        setupColumn(submittedAtColumn, "submittedAt");
        setupColumn(cvFileColumn, "documentCvPath");
        setupColumn(businessLinkColumn, "businessLink");
        setupColumn(pdfFileColumn, "documentPdfPath");

        // CV-Spalte bleibt wie gehabt:
        cvFileColumn.setCellFactory(col -> new DownloadButtonTableCell(fileDownloadService,
                recommendation -> fileDownloadService.downloadFile(recommendation.getDocumentCvPath())));

// Für die PDF-Spalte: Hier wird die neue Methode aufgerufen
        pdfFileColumn.setCellFactory(col -> new DownloadButtonTableCell(fileDownloadService,
                recommendation -> fileDownloadService.downloadGeneratedFile(recommendation.getDocumentPdfPath())));


    }

    private <T> void setupColumn(TableColumn<RecommendationDTO, T> column, String propertyName) {
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
    }

    private void loadRecommendations() {
        Long userId = UserSession.getInstance().getUserId();
        try {
            recommendationsTable.setItems(FXCollections.observableArrayList(
                    backendService.fetchRecommendationsForUser(userId)
            ));
        } catch (IOException | InterruptedException e) {
            DialogUtil.showError("Fehler beim Laden", "Empfehlungen konnten nicht geladen werden.");
        }
    }

    @FXML
    private void handleNewRecommendation(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/formular-recommendation-view.fxml", 0.8);
    }


    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().setUsername(null);
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

}