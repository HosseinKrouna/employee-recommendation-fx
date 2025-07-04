package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.services.FileDownloadService;
import com.krouna.empfehlungsapp_javafx.ui.cells.DownloadButtonTableCell;
import com.krouna.empfehlungsapp_javafx.ui.cells.StatusComboBoxTableCell;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
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
        recommendationsTable.setEditable(true);
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


        statusColumn.setPrefWidth(140);

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusColumn.setCellFactory(col -> new StatusComboBoxTableCell(

                (recommendation, newStatus) -> {

                    handleStatusUpdate(recommendation, newStatus);
                }
        ));

        statusColumn.setOnEditCommit(event -> {
            RecommendationDTO dto = event.getRowValue();
            String committedValue = event.getNewValue();
            System.out.println("DEBUG: onEditCommit für Status-Spalte. Committed Wert: " + committedValue + " für ID: " + dto.getId());

        });



        pdfFileColumn.setCellValueFactory(new PropertyValueFactory<>("documentPdfPath"));
        pdfFileColumn.setCellFactory(col -> new DownloadButtonTableCell(fileDownloadService,
                recommendation -> fileDownloadService.downloadGeneratedFile(recommendation.getDocumentPdfPath())));


        cvFileColumn.setCellValueFactory(new PropertyValueFactory<>("documentCvPath"));
        cvFileColumn.setCellFactory(col -> new DownloadButtonTableCell(fileDownloadService,
                recommendation -> fileDownloadService.downloadCvFile(recommendation.getDocumentCvPath())));


        businessLinkColumn.setCellValueFactory(new PropertyValueFactory<>("businessLink"));

        businessLinkColumn.setCellFactory(col -> new TableCell<RecommendationDTO, String>() {
            private final Hyperlink link = new Hyperlink();

            {
                link.setOnAction(event -> {
                    String url = getItem();
                    if (url != null && !url.trim().isEmpty()) {
                        try {

                            if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
                                url = "https://" + url;
                            }

                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(new URI(url));
                            } else {

                                System.err.println("Desktop browse action not supported.");
                                DialogUtil.showError("Fehler", "Der Link konnte nicht im Browser geöffnet werden (Aktion nicht unterstützt).");
                            }
                        } catch (URISyntaxException | IOException e) {

                            System.err.println("Fehler beim Öffnen des Links '" + url + "': " + e.getMessage());
                            DialogUtil.showError("Fehler", "Ungültiger Link oder Browser konnte nicht geöffnet werden:\n" + url);
                        }
                    }
                });
            }


            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.trim().isEmpty()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    link.setText(item);
                    setGraphic(link);
                    setText(null);
                }
            }
        });

    }


    private void handleStatusUpdate(RecommendationDTO recommendation, String newStatus) {

        if (newStatus.equals(recommendation.getStatus())) {
            return;
        }

        System.out.println("Versuche Status zu ändern für ID: " + recommendation.getId() + " zu: " + newStatus);


        boolean confirmed = DialogUtil.showConfirmation("Status ändern",
                "Soll der Status für '" + recommendation.getCandidateFirstname() + " " + recommendation.getCandidateLastname() +
                        "' wirklich auf '" + newStatus + "' geändert werden?");

        if (confirmed) {
            try {
                boolean success = backendService.updateRecommendationStatus(recommendation.getId(), newStatus);

                if (success) {

                    recommendation.setStatus(newStatus);

                    DialogUtil.showInfo("Erfolg", "Status erfolgreich geändert.");
                    recommendationsTable.refresh();
                } else {

                    DialogUtil.showError("Fehler", "Status konnte nicht geändert werden (Backend-Fehler).");
                    recommendationsTable.refresh();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                DialogUtil.showError("Fehler", "Fehler bei der Anfrage an das Backend: " + e.getMessage());
                recommendationsTable.refresh();
            } catch (Exception e) {
                e.printStackTrace();
                DialogUtil.showError("Fehler", "Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
                recommendationsTable.refresh();
            }
        } else {

            recommendationsTable.refresh();
        }
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

        UserSession.getInstance().clear();


        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

}

