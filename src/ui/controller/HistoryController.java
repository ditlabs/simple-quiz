package ui.controller;

import dao.resultDao;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Result;
import util.SessionManager;

import java.sql.Timestamp;

public class HistoryController {

    private final Stage stage;
    private final resultDao resultDao = new resultDao();

    public HistoryController(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.getStyleClass().add("content-card");

        Label title = new Label("Riwayat Kuis Anda");
        title.getStyleClass().add("title-text");

        TableView<Result> historyTable = new TableView<>();
        setupHistoryTable(historyTable);
        int userId = SessionManager.getInstance().getLoggedInUser().getId();
        historyTable.setItems(FXCollections.observableArrayList(resultDao.getHistoryForUser(userId)));

        Button backButton = new Button("Kembali ke Dashboard");
        backButton.getStyleClass().add("primary-button");
        backButton.setOnAction(e -> {
            UserDashboardController dashboardController = new UserDashboardController(stage);
            dashboardController.show();
        });

        container.getChildren().addAll(title, historyTable, backButton);

        Scene scene = new Scene(new VBox(container), 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Riwayat Kuis");
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
}