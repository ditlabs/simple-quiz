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

public class RegisterController {

    private final Stage stage;
    private final UserDao userDao = new UserDao();

    public RegisterController(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        StackPane layout = createRegisterLayout();
        Scene scene = new Scene(layout, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Quiz App - Sign Up");
    }

    private StackPane createRegisterLayout() {
        VBox card = new VBox(25);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Create Account");
        title.getStyleClass().add("title-text");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Create a password");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm your password");

        Button registerButton = new Button("Sign Up");
        registerButton.getStyleClass().add("primary-button");
        registerButton.setOnAction(e -> handleRegister(usernameField.getText(), passwordField.getText(), confirmField.getText()));

        Hyperlink loginLink = new Hyperlink("Already have an account? Log In");
        loginLink.getStyleClass().add("hyperlink-style");
        loginLink.setOnAction(e -> {
            LoginController loginController = new LoginController(stage);
            loginController.show();
        });

        card.getChildren().addAll(title, usernameField, passwordField, confirmField, registerButton, loginLink);

        StackPane background = new StackPane(card);
        background.getStyleClass().add("root");
        return background;
    }

    private void handleRegister(String username, String password, String confirmPassword) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Semua kolom harus diisi.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Error", "Password dan konfirmasi password tidak cocok.");
            return;
        }

        boolean success = userDao.createUser(new User(username, password));
        if (success) {
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Akun berhasil dibuat! Silakan login.");
            LoginController loginController = new LoginController(stage);
            loginController.show();
        } else {
            AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Error", "Gagal membuat akun. Username mungkin sudah digunakan.");
        }
    }
}