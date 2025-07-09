package ui.controller;

import dao.UserDao;
import dao.questionDao;
import dao.resultDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Question;
import model.Result;
import model.User;
import ui.util.AlertHelper;
import ui.view.AdminView;
import util.SessionManager;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class AdminController {

    private final Stage stage;
    private final AdminView view;
    private final UserDao userDao = new UserDao();
    private final resultDao resultDao = new resultDao();
    private final questionDao questionDao = new questionDao();

    // Constructor
    public AdminController(Stage stage) {
        this.stage = stage;
        this.view = new AdminView();

        // Atur semua event handler utama sekali
        setupEventHandlers();

        // Muat data awal untuk panel pertama (Manajemen Soal)
        refreshQuestionTable();
    }

    // Metode untuk menampilkan dashboard admin
    public void show() {
        Scene adminScene = new Scene(view, 1200, 720);
        adminScene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(adminScene);
        stage.setTitle("Admin Dashboard");
    }

    // --- Setup Event Handlers ---
    private void setupEventHandlers() {
        // Navigasi Sidebar
        view.getManageQuestionsButton().setOnAction(e -> {
            view.setCenter(view.createQuestionManagementView());
            setupQuestionManagementHandlers(); // <-- PASANG ULANG HANDLER
            refreshQuestionTable();
        });

        view.getManageUsersButton().setOnAction(e -> {
            view.setCenter(view.createUserManagementView());
            setupUserManagementHandlers(); // <-- PASANG ULANG HANDLER
            refreshUserTable();
        });

        view.getStatisticsButton().setOnAction(e -> {
            List<Result> allResults = resultDao.getAllResults();
            view.setCenter(view.createStatsView(allResults));
        });

        view.getLogoutButton().setOnAction(e -> {
            SessionManager.getInstance().clear();
            new LoginController(stage).show();
        });

        // Pasang handler untuk panel pertama kali saat controller dibuat
        setupQuestionManagementHandlers();
    }

    // Metode untuk memasang handler pada tombol-tombol di panel soal
    private void setupQuestionManagementHandlers() {
        view.getAddQuestionButton().setOnAction(e -> showAddQuestionDialog());
        view.getEditQuestionButton().setOnAction(e -> {
            Question selected = view.getQuestionTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditQuestionDialog(selected);
            } else {
                AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Pilih soal untuk diedit.");
            }
        });
        view.getDeleteQuestionButton().setOnAction(e -> handleDeleteQuestion());
    }

    // Metode untuk memasang handler pada tombol-tombol di panel pengguna
    private void setupUserManagementHandlers() {
        view.getEditRoleButton().setOnAction(e -> {
            User selected = view.getUserTable().getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditRoleDialog(selected);
            } else {
                AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Pilih pengguna untuk diubah.");
            }
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

    // --- Dialog dan Handler ---
    private void showAddQuestionDialog() {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Tambah Soal Baru");
        dialog.setHeaderText("Silakan isi detail pertanyaan di bawah ini.");
        dialog.initOwner(stage);

        // Tombol untuk menyimpan soal baru
        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Layout untuk dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Komponen input untuk pertanyaan
        TextArea questionText = new TextArea();
        questionText.setPromptText("Tulis teks pertanyaan di sini...");
        questionText.setWrapText(true);

        // Komponen input untuk gambar (opsional)
        TextField imagePathField = new TextField();
        imagePathField.setPromptText("Opsional: C:\\path\\ke\\gambar.jpg");
        Button browseButton = new Button("Cari Gambar...");
        HBox imageBox = new HBox(5, imagePathField, browseButton);
        HBox.setHgrow(imagePathField, Priority.ALWAYS);

        // Event handler untuk tombol browse
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Gambar Soal");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        // Komponen input untuk pilihan jawaban
        TextField optionA = new TextField();
        optionA.setPromptText("Pilihan A");
        TextField optionB = new TextField();
        optionB.setPromptText("Pilihan B");
        TextField optionC = new TextField();
        optionC.setPromptText("Pilihan C");
        TextField optionD = new TextField();
        optionD.setPromptText("Pilihan D");
        ComboBox<String> correctOption = new ComboBox<>();
        correctOption.getItems().addAll("A", "B", "C", "D");
        correctOption.setPromptText("Jawaban Benar");

        // Tambahkan semua komponen ke grid
        grid.add(new Label("Pertanyaan:"), 0, 0);
        grid.add(questionText, 1, 0);
        grid.add(new Label("Path Gambar (Opsional):"), 0, 1);
        grid.add(imageBox, 1, 1);
        grid.add(new Label("Pilihan A:"), 0, 2);
        grid.add(optionA, 1, 2);
        grid.add(new Label("Pilihan B:"), 0, 3);
        grid.add(optionB, 1, 3);
        grid.add(new Label("Pilihan C:"), 0, 4);
        grid.add(optionC, 1, 4);
        grid.add(new Label("Pilihan D:"), 0, 5);
        grid.add(optionD, 1, 5);
        grid.add(new Label("Jawaban Benar:"), 0, 6);
        grid.add(correctOption, 1, 6);

        // Atur tinggi area teks pertanyaan
        questionText.setPrefHeight(100);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(questionText::requestFocus);

        // Konversi hasil dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (questionText.getText().trim().isEmpty() || optionA.getText().trim().isEmpty() ||
                        optionB.getText().trim().isEmpty() || optionC.getText().trim().isEmpty() ||
                        optionD.getText().trim().isEmpty() || correctOption.getValue() == null) {
                    AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Input Tidak Lengkap", "Harap isi semua kolom wajib sebelum menyimpan.");
                    return null;
                }
                // Buat objek Question baru dengan data dari dialog
                return new Question(
                        questionText.getText(),
                        imagePathField.getText(),
                        optionA.getText(),
                        optionB.getText(),
                        optionC.getText(),
                        optionD.getText(),
                        correctOption.getValue()
                );
            }
            return null;
        });

        // Tampilkan dialog dan tunggu hasilnya
        dialog.showAndWait().ifPresent(newQuestion -> {
            if (questionDao.addQuestion(newQuestion)) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Soal baru berhasil ditambahkan.");
                refreshQuestionTable();
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal menyimpan soal ke database.");
            }
        });
    }

    // Metode untuk menampilkan dialog edit soal
    private void showEditQuestionDialog(Question questionToEdit) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Edit Soal");
        dialog.setHeaderText("Anda sedang mengedit soal dengan ID: " + questionToEdit.getId());
        dialog.initOwner(stage);

        // Tombol untuk menyimpan perubahan
        ButtonType saveButtonType = new ButtonType("Simpan Perubahan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Layout untuk dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Komponen input untuk pertanyaan
        TextArea questionText = new TextArea(questionToEdit.getQuestionText());
        questionText.setWrapText(true);

        // Komponen input untuk gambar (opsional)
        TextField imagePathField = new TextField(questionToEdit.getImagePath());
        Button browseButton = new Button("Cari Gambar...");
        HBox imageBox = new HBox(5, imagePathField, browseButton);
        HBox.setHgrow(imagePathField, Priority.ALWAYS);

        // Event handler untuk tombol browse
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Gambar Soal");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        // Komponen input untuk pilihan jawaban
        TextField optionA = new TextField(questionToEdit.getOptionA());
        TextField optionB = new TextField(questionToEdit.getOptionB());
        TextField optionC = new TextField(questionToEdit.getOptionC());
        TextField optionD = new TextField(questionToEdit.getOptionD());
        ComboBox<String> correctOption = new ComboBox<>();
        correctOption.getItems().addAll("A", "B", "C", "D");
        correctOption.setValue(questionToEdit.getCorrectOption());

        // Tambahkan semua komponen ke grid
        grid.add(new Label("Pertanyaan:"), 0, 0);
        grid.add(questionText, 1, 0);
        grid.add(new Label("Path Gambar:"), 0, 1);
        grid.add(imageBox, 1, 1);
        grid.add(new Label("Pilihan A:"), 0, 2);
        grid.add(optionA, 1, 2);
        grid.add(new Label("Pilihan B:"), 0, 3);
        grid.add(optionB, 1, 3);
        grid.add(new Label("Pilihan C:"), 0, 4);
        grid.add(optionC, 1, 4);
        grid.add(new Label("Pilihan D:"), 0, 5);
        grid.add(optionD, 1, 5);
        grid.add(new Label("Jawaban Benar:"), 0, 6);
        grid.add(correctOption, 1, 6);
        questionText.setPrefHeight(100);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(questionText::requestFocus);

        // Konversi hasil dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                questionToEdit.setQuestionText(questionText.getText());
                questionToEdit.setImagePath(imagePathField.getText());
                questionToEdit.setOptionA(optionA.getText());
                questionToEdit.setOptionB(optionB.getText());
                questionToEdit.setOptionC(optionC.getText());
                questionToEdit.setOptionD(optionD.getText());
                questionToEdit.setCorrectOption(correctOption.getValue());
                return questionToEdit;
            }
            return null;
        });

        // Tampilkan dialog dan tunggu hasilnya
        dialog.showAndWait().ifPresent(editedQuestion -> {
            if (questionDao.updateQuestion(editedQuestion)) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Soal berhasil diperbarui.");
                refreshQuestionTable();
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal memperbarui soal di database.");
            }
        });
    }

    // Metode untuk menghapus soal yang dipilih
    private void handleDeleteQuestion() {
        Question selectedQuestion = view.getQuestionTable().getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Tidak Ada Pilihan", "Silakan pilih soal yang ingin dihapus.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Hapus");
        confirmationAlert.setHeaderText("Anda akan menghapus soal berikut:");
        confirmationAlert.setContentText("ID: " + selectedQuestion.getId() + "\nSoal: " + selectedQuestion.getQuestionText());
        confirmationAlert.initOwner(stage);

        Optional<ButtonType> response = confirmationAlert.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            if (questionDao.deleteQuestion(selectedQuestion.getId())) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Soal berhasil dihapus.");
                refreshQuestionTable();
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal menghapus soal dari database.");
            }
        }
    }

    private void showEditRoleDialog(User user) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Ubah Peran Pengguna");
        dialog.setHeaderText("Ubah peran untuk: " + user.getUsername());
        dialog.initOwner(stage);

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("USER", "ADMIN");
        roleComboBox.setValue(user.getRole());

        GridPane grid = new GridPane();
        grid.add(new Label("Peran:"), 0, 0);
        grid.add(roleComboBox, 1, 0);
        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return roleComboBox.getValue();
            }
            return null;
        });

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
        User selectedUser = view.getUserTable().getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Pilih pengguna untuk dihapus.");
            return;
        }

        if (selectedUser.getId() == SessionManager.getInstance().getLoggedInUser().getId()) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Anda tidak dapat menghapus akun Anda sendiri.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Hapus");
        confirmation.setHeaderText("Hapus Pengguna: " + selectedUser.getUsername());
        confirmation.setContentText("Apakah Anda yakin?");
        confirmation.initOwner(stage);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (userDao.deleteUser(selectedUser.getId())) {
                    AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Pengguna berhasil dihapus.");
                    refreshUserTable();
                } else {
                    AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal menghapus pengguna.");
                }
            }
        });
    }
}