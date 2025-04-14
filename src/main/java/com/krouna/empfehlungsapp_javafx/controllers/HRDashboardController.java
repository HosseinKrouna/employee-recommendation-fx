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

        // --- Status Spalte ---
        statusColumn.setPrefWidth(140); // Oder ein anderer passender Wert
        // 1. Datenquelle bleibt der String "status"
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
//        statusColumn.getStyleClass().add("editable-status-cell");
        // 2. Benutzerdefinierte CellFactory mit der Update-Aktion setzen
        statusColumn.setCellFactory(col -> new StatusComboBoxTableCell(
                // Lambda-Ausdruck für die BiConsumer<RecommendationDTO, String> updateAction
                (recommendation, newStatus) -> {
                    // Diese Aktion wird ausgeführt, wenn ein neuer Status gewählt wird
                    handleStatusUpdate(recommendation, newStatus); // Ruft die Logik auf
                }
        ));

        statusColumn.setOnEditCommit(event -> {
            RecommendationDTO dto = event.getRowValue();
            String committedValue = event.getNewValue(); // Der Wert aus commitEdit()
            System.out.println("DEBUG: onEditCommit für Status-Spalte. Committed Wert: " + committedValue + " für ID: " + dto.getId());

        });


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
                            // Sicherstellen, dass die URL ein Protokoll hat (http/https)
                            if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
                                url = "https://" + url; // Füge https:// hinzu, wenn es fehlt
                            }
                            // Desktop.browse verwenden, um den Standardbrowser zu öffnen
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
                    link.setText(item); // Den Link-Text (die URL selbst) setzen
                    setGraphic(link);   // Den Hyperlink als Inhalt der Zelle setzen
                    setText(null);      // Kein normaler Text neben dem Hyperlink
                }
            }
        });

    }

    /**
     * Diese Methode wird aufgerufen, wenn der Status in der ComboBox geändert wird.
     * Implementiert den Aufruf an den BackendService zur Statusaktualisierung.
     *
     * @param recommendation Das DTO der Zeile, deren Status geändert wurde.
     * @param newStatus Der neu ausgewählte Status-String.
     */
    private void handleStatusUpdate(RecommendationDTO recommendation, String newStatus) {
        // Optional: Keine Aktion, wenn Status nicht geändert wurde (sollte die Cell verhindern, aber sicher ist sicher)
        if (newStatus.equals(recommendation.getStatus())) {
            return;
        }

        System.out.println("Versuche Status zu ändern für ID: " + recommendation.getId() + " zu: " + newStatus);

        // Optional: Bestätigungsdialog anzeigen
        boolean confirmed = DialogUtil.showConfirmation("Status ändern",
                "Soll der Status für '" + recommendation.getCandidateFirstname() + " " + recommendation.getCandidateLastname() +
                        "' wirklich auf '" + newStatus + "' geändert werden?");

        if (confirmed) {
            try {
                boolean success = backendService.updateRecommendationStatus(recommendation.getId(), newStatus);

                if (success) {
                    // Wenn Backend erfolgreich war, DTO in Tabelle aktualisieren
                    // (wird meist schon durch commitEdit in der Cell erledigt)
                    recommendation.setStatus(newStatus);
                    // Manchmal nötig, um die Anzeige zu erzwingen:
                    // recommendationsTable.refresh();
                    DialogUtil.showInfo("Erfolg", "Status erfolgreich geändert.");
                    recommendationsTable.refresh();
                } else {
                    // Fehler vom Backend (z.B. Update nicht erlaubt, DB-Fehler)
                    DialogUtil.showError("Fehler", "Status konnte nicht geändert werden (Backend-Fehler).");
                    recommendationsTable.refresh(); // Alten Status wiederherstellen
                }
            } catch (IOException | InterruptedException e) { // Passende Exceptions fangen
                e.printStackTrace();
                DialogUtil.showError("Fehler", "Fehler bei der Anfrage an das Backend: " + e.getMessage());
                recommendationsTable.refresh(); // Alten Status wiederherstellen
            } catch (Exception e) { // Generischer Fallback
                e.printStackTrace();
                DialogUtil.showError("Fehler", "Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
                recommendationsTable.refresh();
            }
        } else {
            // Benutzer hat abgebrochen -> Tabelle neu laden/aktualisieren, um alten Wert sicher anzuzeigen
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
        // Leert die gesamte UserSession, inklusive userId, username UND token
        UserSession.getInstance().clear();

        // Zurück zur Rollenauswahl wechseln
        SceneUtil.switchScene(event, "/com/krouna/empfehlungsapp_javafx/role-selection-view.fxml");
    }

}

