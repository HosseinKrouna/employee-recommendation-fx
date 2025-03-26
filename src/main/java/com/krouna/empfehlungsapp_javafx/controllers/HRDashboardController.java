package com.krouna.empfehlungsapp_javafx.controllers;

import com.krouna.empfehlungsapp_javafx.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.BackendService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private TableColumn<RecommendationDTO, String> submittedAtColumn; // Passe den Typ ggf. an

    @FXML
    private TableColumn<RecommendationDTO, String> recommendedByColumn;

    @FXML
    private TableColumn<RecommendationDTO, String> cvFileColumn;

    @FXML
    private TableColumn<RecommendationDTO, String> coverLetterFileColumn;

    @FXML
    private Label errorLabel;


    private final BackendService backendService = new BackendService();


    // Diese Methode wird nach dem Laden der FXML automatisch aufgerufen.
    public void initialize(URL location, ResourceBundle resources) {
        // Hier könntest du z.B. Daten aus einer REST-API laden und in der UI darstellen.
        // Konfiguriere die Spalten, damit sie die entsprechenden Eigenschaften des DTO auslesen
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        candidateFirstnameColumn.setCellValueFactory(new PropertyValueFactory<>("candidateFirstname"));
        candidateLastnameColumn.setCellValueFactory(new PropertyValueFactory<>("candidateLastname"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        submittedAtColumn.setCellValueFactory(new PropertyValueFactory<>("submittedAt"));

        recommendedByColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRecommendedByUsername()));

        cvFileColumn.setCellValueFactory(new PropertyValueFactory<>("documentCvPath"));
        cvFileColumn.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final Button downloadButton = new Button();

            {
                // Bild setzen
                ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/pdf-icon.png")));
                icon.setFitWidth(16);
                icon.setFitHeight(16);
                downloadButton.setGraphic(icon);
                downloadButton.setStyle("-fx-background-color: transparent;");

                // 🔍 Tooltip setzen
                Tooltip tooltip = new Tooltip("PDF herunterladen");
                Tooltip.install(downloadButton, tooltip);

                // ✨ Hover-Effekt
                downloadButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                downloadButton.setOnMouseEntered(e -> downloadButton.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;"));
                downloadButton.setOnMouseExited(e -> downloadButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

                downloadButton.setOnAction(e -> {
                    RecommendationDTO recommendation = getTableView().getItems().get(getIndex());
                    downloadFile(recommendation.getDocumentCvPath());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null || item.isBlank() ? null : downloadButton);
            }
        });
        coverLetterFileColumn.setCellValueFactory(new PropertyValueFactory<>("documentCoverLetterPath"));
        coverLetterFileColumn.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final Button downloadButton = new Button();

            {
                // Bild setzen
                ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/pdf-icon.png")));
                icon.setFitWidth(16);
                icon.setFitHeight(16);
                downloadButton.setGraphic(icon);
                downloadButton.setStyle("-fx-background-color: transparent;");

                // 🔍 Tooltip setzen
                Tooltip tooltip = new Tooltip("PDF herunterladen");
                Tooltip.install(downloadButton, tooltip);

                // ✨ Hover-Effekt
                downloadButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                downloadButton.setOnMouseEntered(e -> downloadButton.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;"));
                downloadButton.setOnMouseExited(e -> downloadButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

                downloadButton.setOnAction(e -> {
                    RecommendationDTO recommendation = getTableView().getItems().get(getIndex());
                    downloadFile(recommendation.getDocumentCoverLetterPath());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null || item.isBlank() ? null : downloadButton);
            }
        });

        // Lade die Daten beim Start einmalig
        handleRefresh();
        System.out.println("HR Dashboard Controller initialisiert.");
    }

    private void downloadFile(String filename) {
        if (filename == null || filename.isBlank()) {
            System.out.println("⚠️ Kein Dateiname zum Herunterladen.");
            return;
        }

        try {
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            String downloadUrl = "http://localhost:8080/api/files/download/" + encodedFilename;

            HttpClient.newHttpClient().sendAsync(
                    HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build(),
                    HttpResponse.BodyHandlers.ofByteArray()
            ).thenAccept(response -> {
                if (response.statusCode() == 200) {
                    Platform.runLater(() -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Speichern unter");
                        fileChooser.setInitialFileName(filename);
                        fileChooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter("PDF-Datei", "*.pdf")
                        );

                        File targetFile = fileChooser.showSaveDialog(null);
                        if (targetFile != null) {
                            try {
                                Files.write(targetFile.toPath(), response.body());
                                System.out.println("📥 Datei gespeichert unter: " + targetFile.getAbsolutePath());
                            } catch (IOException e) {
                                e.printStackTrace();
                                showError("Error", "Fehler beim Speichern der Datei!");
                            }
                        } else {
                            System.out.println("❌ Speichern abgebrochen.");
                        }
                    });
                } else {
                    System.err.println("❌ Download fehlgeschlagen: " + response.statusCode());
                    showError("Error","Download fehlgeschlagen (Status: " + response.statusCode() + ")");
                }
            }).exceptionally(e -> {
                e.printStackTrace();
                showError("Error","Verbindungsfehler beim Herunterladen!");
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error","Interner Fehler beim Herunterladen!");
        }
    }



    /**
     * Wird aufgerufen, wenn der "Daten aktualisieren"-Button betätigt wird.
     * Lädt die aktuellen Empfehlungen vom Backend und aktualisiert die Tabelle.
     */
    @FXML
    private void handleRefresh() {
        try {
            List<RecommendationDTO> recommendations = backendService.fetchAllRecommendations(); // <-- wichtig
            recommendationsTable.setItems(FXCollections.observableArrayList(recommendations));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            errorLabel.setText("Fehler beim Laden der Empfehlungen.");
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


    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
