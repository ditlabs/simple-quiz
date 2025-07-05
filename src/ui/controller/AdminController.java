package ui.controller;

import dao.UserDao;
import dao.questionDao;
import dao.resultDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Question;
import model.Result;
import model.User;
import ui.util.AlertHelper;
import util.SessionManager;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class AdminController {

    private final Stage stage;
    private final UserDao userDao = new UserDao();
    private final resultDao resultDao = new resultDao();
    private final questionDao questionDao = new questionDao();

    public AdminController(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("admin-root");

        // --- Sidebar ---
        VBox sidebar = createSidebar(mainLayout);
        mainLayout.setLeft(sidebar);
        BorderPane.setMargin(sidebar, new Insets(20, 0, 20, 20));

        // --- Konten Awal ---
        mainLayout.setCenter(createQuestionManagementView());
        BorderPane.setMargin(mainLayout.getCenter(), new Insets(20));

        Scene adminScene = new Scene(mainLayout, 1200, 720);
        // Pastikan Anda memiliki styles.css yang mendukung kelas .sidebar-button
        adminScene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        stage.setScene(adminScene);
        stage.setTitle("Admin Dashboard");
    }

    private VBox createSidebar(BorderPane mainLayout) {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar"); // Pastikan kelas ini ada di CSS Anda

        // --- Tombol Navigasi ---
        ToggleButton manageQuestionsButton = new ToggleButton("Manajemen Soal");
        ToggleButton manageUsersButton = new ToggleButton("Manajemen Pengguna");
        ToggleButton statisticsButton = new ToggleButton("Statistik Kuis");

        ToggleGroup toggleGroup = new ToggleGroup();
        manageQuestionsButton.setToggleGroup(toggleGroup);
        manageUsersButton.setToggleGroup(toggleGroup);
        statisticsButton.setToggleGroup(toggleGroup);

        // Atur agar tombol pertama terpilih
        toggleGroup.selectToggle(manageQuestionsButton);

        // Styling dan Aksi
        styleSidebarButton(manageQuestionsButton);
        styleSidebarButton(manageUsersButton);
        styleSidebarButton(statisticsButton);

        manageQuestionsButton.setOnAction(e -> {
            Node questionView = createQuestionManagementView();
            mainLayout.setCenter(questionView);
            BorderPane.setMargin(questionView, new Insets(20));
        });

        manageUsersButton.setOnAction(e -> {
            Node userView = createUserManagementView();
            mainLayout.setCenter(userView);
            BorderPane.setMargin(userView, new Insets(20));
        });

        statisticsButton.setOnAction(e -> {
            Node statsView = createStatsView();
            mainLayout.setCenter(statsView);
            BorderPane.setMargin(statsView, new Insets(20));
        });

        // --- Tombol Logout ---
        Button logoutButton = new Button("Logout");
        styleSidebarButton(logoutButton); // Gunakan style yang sama
        logoutButton.setOnAction(e -> {
            SessionManager.getInstance().clear();
            new LoginController(stage).show();
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(manageQuestionsButton, manageUsersButton, statisticsButton, spacer, logoutButton);

        return sidebar;
    }

    private void styleSidebarButton(ButtonBase button) {
        button.getStyleClass().add("sidebar-button");
        button.setPrefWidth(200);
        button.setAlignment(Pos.CENTER_LEFT);
    }

    // --- View untuk Manajemen Soal ---
    private VBox createQuestionManagementView() {
        VBox container = new VBox(20);
        container.getStyleClass().add("content-card");
        container.setPadding(new Insets(30));

        Label title = new Label("Manajemen Soal Kuis");
        title.getStyleClass().add("title-text");

        TableView<Question> table = new TableView<>();
        setupQuestionTable(table);
        refreshQuestionTable(table);

        Button addButton = new Button("Tambah Soal");
        addButton.getStyleClass().addAll("action-button", "add-button");
        addButton.setOnAction(e -> showAddQuestionDialog(table));

        Button editButton = new Button("Edit Soal");
        editButton.getStyleClass().addAll("action-button", "edit-button");
        editButton.setOnAction(e -> {
            Question selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showEditQuestionDialog(selected, table);
            else AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Pilih soal untuk diedit.");
        });

        Button deleteButton = new Button("Hapus Soal");
        deleteButton.getStyleClass().addAll("action-button", "delete-button");
        deleteButton.setOnAction(e -> handleDeleteQuestion(table));

        HBox buttonBox = new HBox(15, addButton, editButton, deleteButton);
        container.getChildren().addAll(title, table, buttonBox);
        VBox.setVgrow(table, Priority.ALWAYS);
        return container;
    }

    // --- View untuk Manajemen Pengguna ---
    private VBox createUserManagementView() {
        VBox container = new VBox(20);
        container.getStyleClass().add("content-card");
        container.setPadding(new Insets(30));

        Label title = new Label("Manajemen Pengguna");
        title.getStyleClass().add("title-text");

        TableView<User> table = new TableView<>();
        setupUserTable(table);
        refreshUserTable(table);

        Button editRoleButton = new Button("Ubah Peran");
        editRoleButton.getStyleClass().addAll("action-button", "edit-button");
        editRoleButton.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showEditRoleDialog(selected, table);
            else AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Pilih pengguna untuk diubah.");
        });

        Button deleteUserButton = new Button("Hapus Pengguna");
        deleteUserButton.getStyleClass().addAll("action-button", "delete-button");
        deleteUserButton.setOnAction(e -> handleDeleteUser(table));

        HBox buttonBox = new HBox(15, editRoleButton, deleteUserButton);
        container.getChildren().addAll(title, table, buttonBox);
        VBox.setVgrow(table, Priority.ALWAYS);
        return container;
    }

    // --- View untuk Statistik ---
    private VBox createStatsView() {
        VBox container = new VBox(20);
        container.getStyleClass().add("content-card");
        container.setPadding(new Insets(30));

        Label title = new Label("Statistik Kuis");
        title.getStyleClass().add("title-text");

        Label leaderboardTitle = new Label("Papan Peringkat (Top 10)");
        TableView<Result> leaderboardTable = new TableView<>();
        setupLeaderboardTable(leaderboardTable);
        leaderboardTable.setItems(FXCollections.observableArrayList(resultDao.getAllResults()));

        List<Result> allResults = resultDao.getAllResults();
        double averageScore = allResults.stream().mapToInt(Result::getScore).average().orElse(0);
        Label avgLabel = new Label(String.format("Rata-rata Skor Keseluruhan: %.2f", averageScore));

        container.getChildren().addAll(title, leaderboardTitle, leaderboardTable, avgLabel);
        VBox.setVgrow(leaderboardTable, Priority.ALWAYS);
        return container;
    }

    // --- Setup Tabel ---
    private void setupQuestionTable(TableView<Question> table) {
        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Question, String> textCol = new TableColumn<>("Teks Pertanyaan");
        textCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        textCol.setPrefWidth(400);

        TableColumn<Question, String> correctCol = new TableColumn<>("Jawaban");
        correctCol.setCellValueFactory(new PropertyValueFactory<>("correctOption"));
        correctCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, textCol, correctCol);
    }

    private void setupUserTable(TableView<User> table) {
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        table.getColumns().addAll(idCol, usernameCol, roleCol);
    }

    private void setupLeaderboardTable(TableView<Result> table) {
        TableColumn<Result, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Result, Integer> scoreCol = new TableColumn<>("Skor");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<Result, Timestamp> dateCol = new TableColumn<>("Tanggal");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("quizDate"));

        table.getColumns().addAll(usernameCol, scoreCol, dateCol);
    }

    // --- Refresh Data Tabel ---
    private void refreshQuestionTable(TableView<Question> table) {
        table.setItems(FXCollections.observableArrayList(questionDao.getQuestions()));
    }
    private void refreshUserTable(TableView<User> table) {
        table.setItems(FXCollections.observableArrayList(userDao.getAllUsers()));
    }

    // --- Dialog dan Handler ---
    private void showAddQuestionDialog(TableView<Question> questionTable) {
        // (Kode sama persis seperti sebelumnya)
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Tambah Soal Baru");
        dialog.setHeaderText("Silakan isi detail pertanyaan di bawah ini.");
        dialog.initOwner(stage);

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea questionText = new TextArea();
        questionText.setPromptText("Tulis teks pertanyaan di sini...");
        questionText.setWrapText(true);

        TextField imagePathField = new TextField();
        imagePathField.setPromptText("Opsional: C:\\path\\ke\\gambar.jpg");
        Button browseButton = new Button("Cari Gambar...");
        HBox imageBox = new HBox(5, imagePathField, browseButton);
        HBox.setHgrow(imagePathField, Priority.ALWAYS);

        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Gambar Soal");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
            }
        });

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

        questionText.setPrefHeight(100);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(questionText::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (questionText.getText().trim().isEmpty() || optionA.getText().trim().isEmpty() ||
                        optionB.getText().trim().isEmpty() || optionC.getText().trim().isEmpty() ||
                        optionD.getText().trim().isEmpty() || correctOption.getValue() == null) {
                    AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Input Tidak Lengkap", "Harap isi semua kolom wajib sebelum menyimpan.");
                    return null;
                }
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

        dialog.showAndWait().ifPresent(newQuestion -> {
            boolean success = questionDao.addQuestion(newQuestion);
            if (success) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Soal baru berhasil ditambahkan.");
                refreshQuestionTable(questionTable);
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal menyimpan soal ke database.");
            }
        });
    }

    private void showEditQuestionDialog(Question questionToEdit, TableView<Question> questionTable) {
        // (Kode sama persis seperti sebelumnya)
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Edit Soal");
        dialog.setHeaderText("Anda sedang mengedit soal dengan ID: " + questionToEdit.getId());
        dialog.initOwner(stage);

        ButtonType saveButtonType = new ButtonType("Simpan Perubahan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea questionText = new TextArea(questionToEdit.getQuestionText());
        questionText.setWrapText(true);

        TextField imagePathField = new TextField(questionToEdit.getImagePath());
        Button browseButton = new Button("Cari Gambar...");
        HBox imageBox = new HBox(5, imagePathField, browseButton);
        HBox.setHgrow(imagePathField, Priority.ALWAYS);

        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Gambar Soal");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                imagePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        TextField optionA = new TextField(questionToEdit.getOptionA());
        TextField optionB = new TextField(questionToEdit.getOptionB());
        TextField optionC = new TextField(questionToEdit.getOptionC());
        TextField optionD = new TextField(questionToEdit.getOptionD());
        ComboBox<String> correctOption = new ComboBox<>();
        correctOption.getItems().addAll("A", "B", "C", "D");
        correctOption.setValue(questionToEdit.getCorrectOption());

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

        dialog.showAndWait().ifPresent(editedQuestion -> {
            boolean success = questionDao.updateQuestion(editedQuestion);
            if (success) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Soal berhasil diperbarui.");
                refreshQuestionTable(questionTable);
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal memperbarui soal di database.");
            }
        });
    }

    private void handleDeleteQuestion(TableView<Question> questionTable) {
        // (Kode sama persis seperti sebelumnya)
        Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
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
            boolean success = questionDao.deleteQuestion(selectedQuestion.getId());
            if (success) {
                AlertHelper.showAlert(Alert.AlertType.INFORMATION, stage, "Sukses", "Soal berhasil dihapus.");
                refreshQuestionTable(questionTable);
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal menghapus soal dari database.");
            }
        }
    }

    private void showEditRoleDialog(User user, TableView<User> userTable) {
        // (Kode sama persis seperti sebelumnya)
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
                refreshUserTable(userTable);
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal mengubah peran pengguna.");
            }
        });
    }

    private void handleDeleteUser(TableView<User> userTable) {
        // (Kode sama persis seperti sebelumnya)
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
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
                    refreshUserTable(userTable);
                } else {
                    AlertHelper.showAlert(Alert.AlertType.ERROR, stage, "Gagal", "Gagal menghapus pengguna.");
                }
            }
        });
    }
}