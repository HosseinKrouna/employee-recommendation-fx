package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import com.krouna.empfehlungsapp_javafx.services.FileDownloadService;
import com.krouna.empfehlungsapp_javafx.ui.cells.DownloadButtonTableCell;
import com.krouna.empfehlungsapp_javafx.util.DialogUtil;
import com.krouna.empfehlungsapp_javafx.util.SceneUtil;
import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

    @FXML
    private TableColumn<RecommendationDTO, Void> actionColumn;


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

        // --- CellFactory für PDF Spalte
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


        actionColumn.setCellFactory(col -> new TableCell<RecommendationDTO, Void>() {
            private final Button withdrawButton = new Button("Zurückziehen");

            {
                withdrawButton.setOnAction(event -> {
                    RecommendationDTO recommendation = getTableView().getItems().get(getIndex());
                    handleWithdrawAction(recommendation);
                });

                withdrawButton.getStyleClass().add("withdraw-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    RecommendationDTO recommendation = getTableView().getItems().get(getIndex());

                    if ("Eingereicht".equalsIgnoreCase(recommendation.getStatus())) {
                        setGraphic(withdrawButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }


private void handleWithdrawAction(RecommendationDTO recommendation) {
    String confirmMsg = "Möchten Sie die Empfehlung für '" +
            recommendation.getCandidateFirstname() + " " + recommendation.getCandidateLastname() +
            "' wirklich zurückziehen?";

    boolean confirmed = DialogUtil.showConfirmation("Empfehlung zurückziehen", confirmMsg);

    if (confirmed) {

        new Thread(() -> {
            try {
                RecommendationDTO updatedDto = backendService.withdrawRecommendation(recommendation.getId());

                Platform.runLater(() -> {
                    DialogUtil.showInfo("Erfolg", "Empfehlung wurde zurückgezogen.");
                    loadRecommendations();

                });
            } catch (IOException | InterruptedException | SecurityException | IllegalStateException | IllegalArgumentException e) {

                Platform.runLater(() -> {
                    DialogUtil.showError("Fehler", "Zurückziehen fehlgeschlagen: " + e.getMessage());

                    loadRecommendations();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    DialogUtil.showError("Fehler", "Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
                    loadRecommendations();
                });
            }
        }).start();
    }
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

        UserSession.getInstance().clear();


        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }
}