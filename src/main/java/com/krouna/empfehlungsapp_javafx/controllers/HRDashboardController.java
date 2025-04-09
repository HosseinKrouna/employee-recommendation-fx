package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.services.FileDownloadService;
import com.krouna.empfehlungsapp_javafx.ui.cells.DownloadButtonTableCell;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class HRDashboardController implements Initializable {

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
    private TableColumn<RecommendationDTO, String> recommendedByColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> cvFileColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> businessLinkColumn;
    @FXML
    private TableColumn<RecommendationDTO, String> pdfFileColumn;


    @FXML
    private Label errorLabel;

    private final BackendService backendService = new BackendService();
    private final FileDownloadService fileDownloadService = new FileDownloadService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        handleRefresh();
    }

    private void setupColumns() {
        setupColumn(idColumn, "id");
        setupColumn(candidateFirstnameColumn, "candidateFirstname");
        setupColumn(candidateLastnameColumn, "candidateLastname");
        setupColumn(positionColumn, "position");
        setupColumn(statusColumn, "status");
        setupColumn(submittedAtColumn, "submittedAt");

        recommendedByColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRecommendedByUsername()));

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

    @FXML
    private void handleRefresh() {
        try {
            List<RecommendationDTO> recommendations = backendService.fetchAllRecommendations();
            recommendationsTable.setItems(FXCollections.observableArrayList(recommendations));
        } catch (IOException | InterruptedException e) {
            DialogUtil.showError("Fehler", "Fehler beim Laden der Empfehlungen.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

}

