package ui.controller;

import dao.UserDao;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.User;
import ui.util.AlertHelper;
import ui.view.LoginView;
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

    // Method untuk menampilkan view login
    public void show() {
        Scene scene = new Scene(view, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Quiz App - Login");
    }

    // Method untuk setup event handlers
    private void setupEventHandlers() {
        // Event handler untuk tombol login
        view.getLoginButton().setOnAction(e -> handleLogin());

        // Event handler untuk link registrasi
        view.getRegisterLink().setOnAction(e -> {
            RegisterController registerController = new RegisterController(stage);
            registerController.show();
        });
    }

    // Method untuk menangani proses login
    private void handleLogin() {
        String username = view.getUsernameField().getText();
        String password = view.getPasswordField().getText();

        // Validasi input
        if (username.isEmpty() || password.isEmpty()) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Username dan password tidak boleh kosong.");
            return;
        }

        // Panggil DAO untuk melakukan login
        User user = userDao.login(username, password);

        // Jika login berhasil, simpan user di SessionManager dan tampilkan dashboard sesuai role
        if (user != null) {
            SessionManager.getInstance().setLoggedInUser(user);

            // Logika login admin dan user
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Login Berhasil", "Selamat datang, Admin " + user.getUsername() + "!");
                // Jika admin, tampilkan dashboard admin
                AdminController adminController = new AdminController(stage);
                adminController.show();
            } else {
                // Jika bukan admin, tampilkan dashboard pengguna
                UserDashboardController userDashboardController = new UserDashboardController(stage);
                userDashboardController.show();
            }
            // Hapus view login dari stage
        } else {
            AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Login Gagal", "Username atau password salah.");
        }
    }
}