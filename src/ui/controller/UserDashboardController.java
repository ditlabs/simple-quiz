// src/ui/controller/UserDashboardController.java
package ui.controller;

import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;
import ui.view.UserDashboardView; // Import view
import util.SessionManager;

public class UserDashboardController {

    private final Stage stage;
    private final User loggedInUser;
    private final UserDashboardView view;

    public UserDashboardController(Stage stage) {
        this.stage = stage;
        this.loggedInUser = SessionManager.getInstance().getLoggedInUser();
        this.view = new UserDashboardView();

        setupView();
        setupEventHandlers();
    }

    public void show() {
        Scene scene = new Scene(view, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Dashboard");
    }

    private void setupView() {
        // Mengatur data spesifik pada view
        view.getTitleLabel().setText("Selamat Datang, " + loggedInUser.getUsername() + "!");
    }

    private void setupEventHandlers() {
        view.getStartQuizButton().setOnAction(e -> {
            QuizController quizController = new QuizController(stage);
            quizController.show();
        });

        view.getHistoryButton().setOnAction(e -> {
            HistoryController historyController = new HistoryController(stage);
            historyController.show();
        });

        view.getLogoutButton().setOnAction(e -> {
            SessionManager.getInstance().clear();
            new LoginController(stage).show();
        });
    }
}