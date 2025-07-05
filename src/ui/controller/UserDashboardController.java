package ui.controller;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import util.SessionManager;

public class UserDashboardController {

    private final Stage stage;
    private final User loggedInUser;

    public UserDashboardController(Stage stage) {
        this.stage = stage;
        this.loggedInUser = SessionManager.getInstance().getLoggedInUser();
    }

    public void show() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Selamat Datang, " + loggedInUser.getUsername() + "!");
        title.getStyleClass().add("title-text");

        Button startQuizButton = new Button("Mulai Kuis");
        startQuizButton.getStyleClass().add("primary-button");
        startQuizButton.setOnAction(e -> {
            QuizController quizController = new QuizController(stage);
            quizController.show();
        });

        Button historyButton = new Button("Riwayat Kuis");
        historyButton.getStyleClass().add("primary-button");
        historyButton.setOnAction(e -> {
            HistoryController historyController = new HistoryController(stage);
            historyController.show();
        });

        // --- PERUBAHAN DI SINI ---
        Button logoutButton = new Button("🚪 Logout"); // Tambahkan ikon
        // Ganti kelas CSS dari "delete-button"
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(e -> {
            SessionManager.getInstance().clear();
            new LoginController(stage).show();
        });
        // --- AKHIR PERUBAHAN ---

        card.getChildren().addAll(title, startQuizButton, historyButton, logoutButton);

        StackPane layout = new StackPane(card);
        layout.getStyleClass().add("root");

        Scene scene = new Scene(layout, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Dashboard");
    }
}