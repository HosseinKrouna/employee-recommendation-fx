package com.krouna.empfehlungsapp_javafx.ui.cells;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.FileDownloadService;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DownloadButtonTableCell extends TableCell<RecommendationDTO, String> {
    private final Button downloadButton = new Button();
    private final FileDownloadService downloadService;

    public DownloadButtonTableCell(FileDownloadService downloadService) {
        this.downloadService = downloadService;
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/pdf-icon.png")));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        downloadButton.setGraphic(icon);
        downloadButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        Tooltip.install(downloadButton, new Tooltip("PDF herunterladen"));

        downloadButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        downloadButton.setOnMouseEntered(e -> downloadButton.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;"));
        downloadButton.setOnMouseExited(e -> downloadButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

        downloadButton.setOnAction(e -> {
            String filePath = getItem();
            if (filePath != null && !filePath.isBlank()) {
                downloadService.downloadFile(filePath);
            }
        });
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty || item == null || item.isBlank() ? null : downloadButton);
    }
}

