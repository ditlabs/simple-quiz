package ui.controller;

import dao.UserDao;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import ui.util.AlertHelper;
import util.SessionManager; // BARU: Untuk menyimpan sesi login

public class LoginController {

    private final Stage stage;
    private final UserDao userDao = new UserDao();

    public LoginController(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        StackPane layout = createLoginLayout();
        Scene scene = new Scene(layout, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Quiz App - Login");
    }

    private StackPane createLoginLayout() {
        VBox card = new VBox(25);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Welcome Back!");
        title.getStyleClass().add("title-text");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Button loginButton = new Button("Log In");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        Hyperlink registerLink = new Hyperlink("Don't have an account? Sign up");
        registerLink.getStyleClass().add("hyperlink-style");
        registerLink.setOnAction(e -> {
            RegisterController registerController = new RegisterController(stage);
            registerController.show();
        });

        card.getChildren().addAll(title, usernameField, passwordField, loginButton, registerLink);

        StackPane background = new StackPane(card);
        background.getStyleClass().add("root");
        return background;
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Username dan password tidak boleh kosong.");
            return;
        }

        User user = userDao.login(username, password);

        if (user != null) {
            // BARU: Simpan informasi user yang login
            SessionManager.getInstance().setLoggedInUser(user);

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Login Berhasil", "Selamat datang, Admin " + user.getUsername() + "!");
                AdminController adminController = new AdminController(stage);
                adminController.show();
            } else {
                // DIUBAH: Arahkan ke dashboard pengguna, bukan langsung ke kuis
                UserDashboardController dashboardController = new UserDashboardController(stage);
                dashboardController.show();
            }
        } else {
            AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Login Gagal", "Username atau password salah.");
        }
    }
}