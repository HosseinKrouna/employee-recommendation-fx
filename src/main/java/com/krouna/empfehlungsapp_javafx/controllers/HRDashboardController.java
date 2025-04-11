package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.services.FileDownloadService;
import com.krouna.empfehlungsapp_javafx.ui.cells.DownloadButtonTableCell;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.URISyntaxException;
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

//        setupColumn(cvFileColumn, "documentCvPath");
//        setupColumn(businessLinkColumn, "businessLink");
//        setupColumn(pdfFileColumn, "documentPdfPath");

        // --- CellFactory für PDF Spalte (wie gehabt) ---
        pdfFileColumn.setCellValueFactory(new PropertyValueFactory<>("documentPdfPath"));
        pdfFileColumn.setCellFactory(col -> new DownloadButtonTableCell(fileDownloadService,
                recommendation -> fileDownloadService.downloadGeneratedFile(recommendation.getDocumentPdfPath())));

        // --- CellFactory für CV Spalte
        cvFileColumn.setCellValueFactory(new PropertyValueFactory<>("documentCvPath")); // Datenquelle bleibt
        cvFileColumn.setCellFactory(col -> new DownloadButtonTableCell(fileDownloadService,
                recommendation -> fileDownloadService.downloadCvFile(recommendation.getDocumentCvPath())));

        // --- NEU: CellFactory für Business-Link Spalte ---
        businessLinkColumn.setCellValueFactory(new PropertyValueFactory<>("businessLink")); // Datenquelle bleibt String

        businessLinkColumn.setCellFactory(col -> new TableCell<RecommendationDTO, String>() {
            private final Hyperlink link = new Hyperlink();

            { // Initialisierungsblock für die Zelle
                link.setOnAction(event -> {
                    String url = getItem(); // Holt den String (URL) aus der Zelle
                    if (url != null && !url.trim().isEmpty()) {
                        try {
                            // Stelle sicher, dass die URL ein Protokoll hat (http/https)
                            if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
                                url = "https://" + url; // Füge https:// hinzu, wenn es fehlt
                            }
                            // Verwende Desktop.browse, um den Standardbrowser zu öffnen
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(new URI(url));
                            } else {
                                // Fallback oder Fehlermeldung, wenn Browser nicht geöffnet werden kann
                                System.err.println("Desktop browse action not supported.");
                                DialogUtil.showError("Fehler", "Der Link konnte nicht im Browser geöffnet werden (Aktion nicht unterstützt).");
                            }
                        } catch (URISyntaxException | IOException e) {
                            // Fehler beim Parsen der URL oder Öffnen des Browsers
                            System.err.println("Fehler beim Öffnen des Links '" + url + "': " + e.getMessage());
                            DialogUtil.showError("Fehler", "Ungültiger Link oder Browser konnte nicht geöffnet werden:\n" + url);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); // Wichtig: Immer super aufrufen!

                if (empty || item == null || item.trim().isEmpty()) {
                    setText(null);
                    setGraphic(null); // Keine Grafik anzeigen, wenn leer oder null
                } else {
                    link.setText(item); // Setze den Link-Text (die URL selbst)
                    setGraphic(link);   // Setze den Hyperlink als Inhalt der Zelle
                    setText(null);      // Kein normaler Text neben dem Hyperlink
                }
            }
        });

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
        // Leere die gesamte UserSession, inklusive userId, username UND token
        UserSession.getInstance().clear(); // <-- ÄNDERUNG HIER

        // Wechsle zurück zur Rollenauswahl
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

}

