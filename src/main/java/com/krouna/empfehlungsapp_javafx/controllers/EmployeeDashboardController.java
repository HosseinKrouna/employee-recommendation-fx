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
//        setupColumn(cvFileColumn, "documentCvPath");
//        setupColumn(businessLinkColumn, "businessLink");
//        setupColumn(pdfFileColumn, "documentPdfPath");

        // --- CellFactory für PDF Spalte
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

        // --- NEU: CellFactory für die Aktionsspalte ---
        actionColumn.setCellFactory(col -> new TableCell<RecommendationDTO, Void>() {
            private final Button withdrawButton = new Button("Zurückziehen");

            { // Initialisierungsblock für die Zelle
                withdrawButton.setOnAction(event -> {
                    RecommendationDTO recommendation = getTableView().getItems().get(getIndex());
                    handleWithdrawAction(recommendation);
                });
                // Optional: Style für den Button
                withdrawButton.getStyleClass().add("withdraw-button"); // CSS-Klasse hinzufügen
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    RecommendationDTO recommendation = getTableView().getItems().get(getIndex());
                    // Zeige Button nur, wenn Status "Eingereicht" ist
                    // Verwende Konstanten, wenn möglich (hier als String für Einfachheit)
                    if ("Eingereicht".equalsIgnoreCase(recommendation.getStatus())) {
                        setGraphic(withdrawButton);
                    } else {
                        setGraphic(null); // Kein Button für andere Status
                    }
                }
            }
        });
    }

// --- NEU: Handler für die Zurückziehen-Aktion ---
private void handleWithdrawAction(RecommendationDTO recommendation) {
    String confirmMsg = "Möchten Sie die Empfehlung für '" +
            recommendation.getCandidateFirstname() + " " + recommendation.getCandidateLastname() +
            "' wirklich zurückziehen?";

    boolean confirmed = DialogUtil.showConfirmation("Empfehlung zurückziehen", confirmMsg);

    if (confirmed) {
        // Führe Backend-Aufruf in einem Hintergrundthread aus
        new Thread(() -> {
            try {
                RecommendationDTO updatedDto = backendService.withdrawRecommendation(recommendation.getId());
                // UI Update im JavaFX Application Thread
                Platform.runLater(() -> {
                    DialogUtil.showInfo("Erfolg", "Empfehlung wurde zurückgezogen.");
                    loadRecommendations(); // Tabelle neu laden, um Status zu aktualisieren
                    // Optional: Nur die spezifische Zeile aktualisieren, wenn DTO zurückgegeben wird
                    // int index = recommendationsTable.getItems().indexOf(recommendation);
                    // if (index != -1) {
                    //     recommendationsTable.getItems().set(index, updatedDto);
                    //     recommendationsTable.refresh(); // Sicherstellen, dass die Zeile neu gezeichnet wird
                    // } else {
                    //     loadRecommendations(); // Fallback
                    // }
                });
            } catch (IOException | InterruptedException | SecurityException | IllegalStateException | IllegalArgumentException e) {
                // UI Update im JavaFX Application Thread
                Platform.runLater(() -> {
                    DialogUtil.showError("Fehler", "Zurückziehen fehlgeschlagen: " + e.getMessage());
                    // Optional: Tabelle neu laden, falls der Fehler durch veraltete Daten kam
                    loadRecommendations();
                });
            } catch (Exception e) { // Generischer Fallback
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
        // Leere die gesamte UserSession, inklusive userId, username UND token
        UserSession.getInstance().clear(); // <-- ÄNDERUNG HIER

        // Wechsle zurück zur Rollenauswahl
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }
}