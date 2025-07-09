package ui.controller;

import dao.UserDao;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.User;
import ui.util.AlertHelper;
import ui.view.LoginView; // Import kelas view yang baru
import util.SessionManager;

public class LoginController {

    private final Stage stage;
    private final UserDao userDao = new UserDao();
    private final LoginView view; // Tambahkan instance dari LoginView

    public LoginController(Stage stage) {
        this.stage = stage;
        this.view = new LoginView(); // Buat view di constructor
        setupEventHandlers(); // Panggil method untuk setup event handler
    }

    public void show() {
        Scene scene = new Scene(view, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Quiz App - Login");
    }

    private void setupEventHandlers() {
        // Event handler untuk tombol login
        view.getLoginButton().setOnAction(e -> handleLogin());

        // Event handler untuk link registrasi
        view.getRegisterLink().setOnAction(e -> {
            RegisterController registerController = new RegisterController(stage);
            registerController.show();
        });
    }

    private void handleLogin() {
        String username = view.getUsernameField().getText();
        String password = view.getPasswordField().getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Username dan password tidak boleh kosong.");
            return;
        }

        User user = userDao.login(username, password);

        if (user != null) {
            SessionManager.getInstance().setLoggedInUser(user);

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Login Berhasil", "Selamat datang, Admin " + user.getUsername() + "!");
                // AdminController akan direfaktor selanjutnya
                AdminController adminController = new AdminController(stage);
                adminController.show();
            } else {
                // UserDashboardController akan direfaktor selanjutnya
                UserDashboardController dashboardController = new UserDashboardController(stage);
                dashboardController.show();
            }
        } else {
            AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Login Gagal", "Username atau password salah.");
        }
    }
}