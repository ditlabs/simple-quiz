// src/ui/controller/HistoryController.java
package ui.controller;

import dao.resultDao;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ui.view.HistoryView; // Import view
import util.SessionManager;

public class HistoryController {

    private final Stage stage;
    private final resultDao resultDao = new resultDao();
    private final HistoryView view;

    public HistoryController(Stage stage) {
        this.stage = stage;
        this.view = new HistoryView();

        loadHistoryData();
        setupEventHandlers();
    }

    public void show() {
        // Bungkus view dalam StackPane agar konsisten dengan root style
        StackPane root = new StackPane(view);
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Riwayat Kuis");
    }

    private void loadHistoryData() {
        int userId = SessionManager.getInstance().getLoggedInUser().getId();
        view.getHistoryTable().setItems(FXCollections.observableArrayList(resultDao.getHistoryForUser(userId)));
    }

    private void setupEventHandlers() {
        view.getBackButton().setOnAction(e -> {
            UserDashboardController dashboardController = new UserDashboardController(stage);
            dashboardController.show();
        });
    }
}