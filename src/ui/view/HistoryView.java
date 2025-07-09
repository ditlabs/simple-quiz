package ui.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import model.Result;

import java.sql.Timestamp;

public class HistoryView extends VBox {

    private final TableView<Result> historyTable;
    private final Button backButton;

    public HistoryView() {
        this.setSpacing(20);
        this.setPadding(new Insets(30));
        this.getStyleClass().add("content-card"); // Menggunakan style yang mirip admin

        Label title = new Label("Riwayat Kuis Anda");
        title.getStyleClass().add("title-text");

        historyTable = new TableView<>();
        setupHistoryTable(historyTable);

        backButton = new Button("Kembali ke Dashboard");
        backButton.getStyleClass().add("primary-button");

        this.getChildren().addAll(title, historyTable, backButton);
    }

    private void setupHistoryTable(TableView<Result> table) {
        TableColumn<Result, Integer> scoreCol = new TableColumn<>("Skor");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<Result, Timestamp> dateCol = new TableColumn<>("Tanggal Pengerjaan");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("quizDate"));

        scoreCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        dateCol.prefWidthProperty().bind(table.widthProperty().multiply(0.7));

        table.getColumns().addAll(scoreCol, dateCol);
    }

    // Getters
    public TableView<Result> getHistoryTable() {
        return historyTable;
    }

    public Button getBackButton() {
        return backButton;
    }
}