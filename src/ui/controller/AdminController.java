// src/ui/controller/AdminController.java
package ui.controller;

import dao.UserDao;
import dao.questionDao;
import dao.resultDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Question;
import model.Result;
import model.User;
import ui.util.AlertHelper;
import ui.view.AdminView; // Import view
import util.SessionManager;

import java.io.File;
import java.util.Optional;
import java.util.List;

public class AdminController {

    private final Stage stage;
    private final AdminView view;
    private final UserDao userDao = new UserDao();
    private final resultDao resultDao = new resultDao();
    private final questionDao questionDao = new questionDao();

    public AdminController(Stage stage) {
        this.stage = stage;
        this.view = new AdminView();
        setupEventHandlers();
        refreshQuestionTable(); // Muat data awal untuk panel pertama
    }

    public void show() {
        Scene adminScene = new Scene(view, 1200, 720);
        adminScene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(adminScene);
        stage.setTitle("Admin Dashboard");
    }

    private void setupEventHandlers() {
        // Navigasi Sidebar
        view.getManageQuestionsButton().setOnAction(e -> {
            view.setCenter(view.createQuestionManagementView());
            setupQuestionManagementHandlers();
            refreshQuestionTable();
        });
        view.getManageUsersButton().setOnAction(e -> {
            view.setCenter(view.createUserManagementView());
            setupUserManagementHandlers();
            refreshUserTable();
        });
        view.getStatisticsButton().setOnAction(e -> {
            List<Result> allResults = resultDao.getAllResults();
            view.setCenter(view.createStatsView(allResults));
            // Tidak ada handler tambahan untuk view statistik
        });
        view.getLogoutButton().setOnAction(e -> {
            SessionManager.getInstance().clear();
            new LoginController(stage).show();
        });

        // Setup handler untuk panel awal
        setupQuestionManagementHandlers();
    }

    private void setupQuestionManagementHandlers() {
        view.getAddQuestionButton().setOnAction(e -> showAddQuestionDialog());
        view.getEditQuestionButton().setOnAction(e -> {
            Question selected = view.getQuestionTable().getSelectionModel().getSelectedItem();
            if (selected != null) showEditQuestionDialog(selected);
            else AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Pilih soal untuk diedit.");
        });
        view.getDeleteQuestionButton().setOnAction(e -> handleDeleteQuestion());
    }

    private void setupUserManagementHandlers() {
        view.getEditRoleButton().setOnAction(e -> {
            User selected = view.getUserTable().getSelectionModel().getSelectedItem();
            if (selected != null) showEditRoleDialog(selected);
            else AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Pilih pengguna untuk diubah.");
        });
        view.getDeleteUserButton().setOnAction(e -> handleDeleteUser());
    }

    // --- Refresh Data Tabel ---
    private void refreshQuestionTable() {
        view.getQuestionTable().setItems(FXCollections.observableArrayList(questionDao.getQuestions()));
    }
    private void refreshUserTable() {
        view.getUserTable().setItems(FXCollections.observableArrayList(userDao.getAllUsers()));
    }

    // --- Dialog dan Handler (Tetap di Controller) ---
    private void showAddQuestionDialog() {
        // (Kode dialog dari file asli Anda bisa ditempel di sini, sedikit modifikasi)
        Dialog<Question> dialog = new Dialog<>();
        // ... (seluruh kode untuk membuat dialog tambah soal)
        // ...

        // Pada akhir dialog:
        dialog.showAndWait().ifPresent(newQuestion -> {
            if (questionDao.addQuestion(newQuestion)) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Soal baru berhasil ditambahkan.");
                refreshQuestionTable();
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal menyimpan soal ke database.");
            }
        });
    }

    private void showEditQuestionDialog(Question questionToEdit) {
        // (Kode dialog dari file asli Anda bisa ditempel di sini)
        Dialog<Question> dialog = new Dialog<>();
        // ... (seluruh kode untuk membuat dialog edit soal)
        // ...

        // Pada akhir dialog:
        dialog.showAndWait().ifPresent(editedQuestion -> {
            if (questionDao.updateQuestion(editedQuestion)) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Soal berhasil diperbarui.");
                refreshQuestionTable();
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal memperbarui soal di database.");
            }
        });
    }

    private void handleDeleteQuestion() {
        Question selected = view.getQuestionTable().getSelectionModel().getSelectedItem();
        // ... (logika hapus dari file asli)
        if (selected != null) {
            // ... konfirmasi, lalu panggil questionDao.deleteQuestion dan refreshQuestionTable()
        }
    }

    private void showEditRoleDialog(User user) {
        // (Kode dialog dari file asli Anda bisa ditempel di sini)
        Dialog<String> dialog = new Dialog<>();
        // ... (seluruh kode untuk membuat dialog ubah peran)
        // ...

        // Pada akhir dialog:
        dialog.showAndWait().ifPresent(newRole -> {
            if (userDao.updateUserRole(user.getId(), newRole)) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Peran pengguna berhasil diubah.");
                refreshUserTable();
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal mengubah peran pengguna.");
            }
        });
    }

    private void handleDeleteUser() {
        User selected = view.getUserTable().getSelectionModel().getSelectedItem();
        // ... (logika hapus dari file asli)
        if (selected != null) {
            // ... konfirmasi, lalu panggil userDao.deleteUser dan refreshUserTable()
        }
    }

    // CATATAN: Saya telah memotong kode pembuatan dialog yang berulang untuk keringkasan.
    // Anda hanya perlu menyalin-tempel kode pembuatan dialog dari AdminController.java
    // lama Anda ke dalam metode-metode di atas (showAddQuestionDialog, dll.).
}