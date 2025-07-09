package ui.controller;

import dao.UserDao;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.User;
import ui.util.AlertHelper;
import ui.view.RegisterView; // Import view

public class RegisterController {

    private final Stage stage;
    private final UserDao userDao = new UserDao();
    private final RegisterView view;

    // Constructor untuk RegisterController
    public RegisterController(Stage stage) {
        this.stage = stage;
        this.view = new RegisterView();
        setupEventHandlers();
    }

    // Method untuk menampilkan view registrasi
    public void show() {
        Scene scene = new Scene(view, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Quiz App - Sign Up");
    }

    // Method untuk setup event handlers
    private void setupEventHandlers() {
        view.getRegisterButton().setOnAction(e -> handleRegister());
        view.getLoginLink().setOnAction(e -> {
            LoginController loginController = new LoginController(stage);
            loginController.show();
        });
    }

    // Method untuk menangani proses registrasi
    private void handleRegister() {
        String username = view.getUsernameField().getText();
        String password = view.getPasswordField().getText();
        String confirmPassword = view.getConfirmField().getText();

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