package com.krouna.empfehlungsapp_javafx.ui.cells;

import com.krouna.empfehlungsapp_javafx.dto.RecommendationDTO;
import com.krouna.empfehlungsapp_javafx.services.FileDownloadService;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.function.Consumer;

public class DownloadButtonTableCell extends TableCell<RecommendationDTO, String> {
    private final Button downloadButton = new Button();
    private final FileDownloadService downloadService;
    private final Consumer<RecommendationDTO> downloadAction;



    public DownloadButtonTableCell(FileDownloadService downloadService, Consumer<RecommendationDTO> downloadAction) {
        this.downloadService = downloadService;
        this.downloadAction = downloadAction;


        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/pdf-icon.png")));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        downloadButton.setGraphic(icon);


        downloadButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        Tooltip.install(downloadButton, new Tooltip("Download"));

        downloadButton.setOnMouseEntered(e -> downloadButton.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;"));
        downloadButton.setOnMouseExited(e -> downloadButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));


        downloadButton.setOnAction(e -> {
            RecommendationDTO recommendation = getTableView().getItems().get(getIndex());
            downloadAction.accept(recommendation);
        });
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty || item == null || item.isBlank() ? null : downloadButton);
    }
}
