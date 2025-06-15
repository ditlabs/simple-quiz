import dao.UserDao;
import dao.questionDao;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.Question;
import model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.ComboBox;

import java.util.List;

public class mainMenu extends Application {

    private Stage primaryStage;
    private UserDao userDao;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;

    private Label progressLabel;
    private ProgressBar progressBar;
    private Text questionText;
    private ToggleGroup optionsGroup;
    private VBox questionArea;

    private void refreshQuestionTable(TableView<Question> table) {
        table.setItems(FXCollections.observableArrayList(questionDao.getQuestions()));
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userDao = new UserDao();

        showLoginScreen();
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void showEditQuestionDialog(Question questionToEdit, TableView<Question> questionTable) {
        // 1. Membuat Dialog
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Edit Soal");
        dialog.setHeaderText("Anda sedang mengedit soal dengan ID: " + questionToEdit.getId());

        // 2. Membuat Tombol
        ButtonType saveButtonType = new ButtonType("Simpan Perubahan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 3. Merancang Form dan MENGISI DATA LAMA
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea questionText = new TextArea(questionToEdit.getQuestionText()); // Isi dengan data lama
        questionText.setWrapText(true);
        TextField optionA = new TextField(questionToEdit.getOptionA()); // Isi dengan data lama
        TextField optionB = new TextField(questionToEdit.getOptionB()); // Isi dengan data lama
        TextField optionC = new TextField(questionToEdit.getOptionC()); // Isi dengan data lama
        TextField optionD = new TextField(questionToEdit.getOptionD()); // Isi dengan data lama
        ComboBox<String> correctOption = new ComboBox<>();
        correctOption.getItems().addAll("A", "B", "C", "D");
        correctOption.setValue(questionToEdit.getCorrectOption()); // Isi dengan data lama

        // ... (Kode untuk menambahkan komponen ke grid sama persis seperti di add)
        grid.add(new Label("Pertanyaan:"), 0, 0);
        grid.add(questionText, 1, 0);
        grid.add(new Label("Pilihan A:"), 0, 1);
        grid.add(optionA, 1, 1);
        grid.add(new Label("Pilihan B:"), 0, 2);
        grid.add(optionB, 1, 2);
        grid.add(new Label("Pilihan C:"), 0, 3);
        grid.add(optionC, 1, 3);
        grid.add(new Label("Pilihan D:"), 0, 4);
        grid.add(optionD, 1, 4);
        grid.add(new Label("Jawaban Benar:"), 0, 5);
        grid.add(correctOption, 1, 5);
        questionText.setPrefHeight(100);
        GridPane.setVgrow(questionText, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(questionText::requestFocus);

        // 4. Logika untuk menyimpan perubahan
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validasi (sama seperti add)
                if (questionText.getText().trim().isEmpty() || /* ... validasi lainnya ... */ correctOption.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap isi semua kolom.");
                    return null;
                }
                // Update field di objek questionToEdit
                questionToEdit.setQuestionText(questionText.getText());
                questionToEdit.setOptionA(optionA.getText());
                questionToEdit.setOptionB(optionB.getText());
                questionToEdit.setOptionC(optionC.getText());
                questionToEdit.setOptionD(optionD.getText());
                questionToEdit.setCorrectOption(correctOption.getValue());
                return questionToEdit;
            }
            return null;
        });

        // 5. Tampilkan dialog dan proses hasilnya
        dialog.showAndWait().ifPresent(editedQuestion -> {
            boolean success = questionDao.updateQuestion(editedQuestion);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Soal berhasil diperbarui.");
                refreshQuestionTable(questionTable);
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui soal di database.");
            }
        });
    }

    // --- MANAJEMEN SCENE ---

    private void showLoginScreen() {
        primaryStage.setTitle("Quiz App - Login");
        StackPane layout = createLoginLayout();
        Scene scene = new Scene(layout, 720, 720); // UKURAN BARU
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showRegisterScreen() {
        primaryStage.setTitle("Quiz App - Sign Up");
        StackPane layout = createRegisterLayout();
        Scene scene = new Scene(layout, 720, 720); // UKURAN BARU
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showQuizScreen() {
        primaryStage.setTitle("Quiz");
        StackPane quizRoot = new StackPane();
        Scene quizScene = new Scene(quizRoot, 720, 720); // UKURAN BARU
        quizScene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        quizRoot.getChildren().add(loadingIndicator);
        quizRoot.getStyleClass().add("root");

        Task<List<Question>> loadQuestionsTask = createTaskToLoadQuestions();
        loadQuestionsTask.setOnSucceeded(e -> Platform.runLater(() -> {
            questions = loadQuestionsTask.getValue();
            quizRoot.getChildren().remove(loadingIndicator);
            if (questions == null || questions.isEmpty()) {
                showErrorState(quizRoot, "Tidak ada soal yang dapat dimuat.");
            } else {
                StackPane mainQuizLayout = createQuizLayout();
                quizRoot.getChildren().add(mainQuizLayout);
            }
        }));
        loadQuestionsTask.setOnFailed(e -> Platform.runLater(() -> {
            quizRoot.getChildren().remove(loadingIndicator);
            showErrorState(quizRoot, "Gagal terhubung ke database.");
        }));

        new Thread(loadQuestionsTask).start();
        primaryStage.setScene(quizScene);
    }

    // --- PEMBUATAN LAYOUT ---

    private StackPane createLoginLayout() {
        VBox card = new VBox();
        card.getStyleClass().add("card");

        Label title = new Label("Welcome Back!");
        title.getStyleClass().add("title-text");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Button loginButton = new Button("Log In");
        loginButton.getStyleClass().add("primary-button");

        Hyperlink registerLink = new Hyperlink("Don't have an account? Sign up");
        registerLink.getStyleClass().add("hyperlink-style");

        card.getChildren().addAll(title, usernameField, passwordField, loginButton, registerLink);

        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));
        registerLink.setOnAction(e -> showRegisterScreen());

        StackPane background = new StackPane(card);
        background.getStyleClass().add("root");
        return background;
    }

    private StackPane createRegisterLayout() {
        VBox card = new VBox();
        card.getStyleClass().add("card");

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

        Hyperlink loginLink = new Hyperlink("Already have an account? Log In");
        loginLink.getStyleClass().add("hyperlink-style");

        card.getChildren().addAll(title, usernameField, passwordField, confirmField, registerButton, loginLink);

        registerButton.setOnAction(e -> handleRegister(usernameField.getText(), passwordField.getText(), confirmField.getText()));
        loginLink.setOnAction(e -> showLoginScreen());

        StackPane background = new StackPane(card);
        background.getStyleClass().add("root");
        return background;
    }

    private StackPane createQuizLayout() {
        VBox card = new VBox();
        card.getStyleClass().add("quiz-card");

        // Header: Progress
        progressLabel = new Label();
        progressLabel.getStyleClass().add("progress-label");
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        VBox progressBox = new VBox(5, progressLabel, progressBar);
        progressBox.setPadding(new Insets(0, 0, 20, 0));

        // Content: Pertanyaan dan Pilihan
        questionText = new Text();
        questionText.getStyleClass().add("question-text");

        optionsGroup = new ToggleGroup();
        VBox optionsContainer = new VBox();
        optionsContainer.getStyleClass().add("options-container");
        for (int i = 0; i < 4; i++) {
            optionsContainer.getChildren().add(createStyledRadioButton());
        }

        questionArea = new VBox(questionText, optionsContainer);
        VBox.setVgrow(optionsContainer, Priority.ALWAYS);

        // Footer: Tombol Submit
        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("primary-button");
        submitButton.setOnAction(e -> handleNextQuestion());

        card.getChildren().addAll(progressBox, questionArea, submitButton);
        VBox.setVgrow(questionArea, Priority.ALWAYS);

        displayQuestion(false);

        StackPane background = new StackPane(card);
        background.getStyleClass().add("root");
        return background;
    }

    // --- (Sisa kode seperti handleLogin, handleRegister, displayQuestion, dll. tetap sama) ---

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Username dan password tidak boleh kosong.");
            return;
        }
        User user = userDao.login(username, password);
        if (user != null) {
            // PENGECEKAN ROLE DI SINI
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                // Jika user adalah ADMIN, tampilkan Admin Panel
                showAlert(Alert.AlertType.INFORMATION, "Login Berhasil", "Selamat datang, Admin " + user.getUsername() + "!");
                showAdminPanel(); // Metode baru yang akan kita buat
            } else {
                // Jika user biasa, tampilkan layar kuis seperti biasa
                showQuizScreen();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username atau password salah.");
        }
    }

    private void showAddQuestionDialog(TableView<Question> questionTable) {
        // 1. Membuat Dialog (sama seperti sebelumnya)
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Tambah Soal Baru");
        dialog.setHeaderText("Silakan isi detail pertanyaan di bawah ini.");

        // 2. Membuat Tombol di Dialog (sama seperti sebelumnya)
        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 3. Merancang Form (sama seperti sebelumnya)
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea questionText = new TextArea();
        questionText.setPromptText("Tulis teks pertanyaan di sini...");
        questionText.setWrapText(true);
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
        grid.add(new Label("Pilihan A:"), 0, 1);
        grid.add(optionA, 1, 1);
        grid.add(new Label("Pilihan B:"), 0, 2);
        grid.add(optionB, 1, 2);
        grid.add(new Label("Pilihan C:"), 0, 3);
        grid.add(optionC, 1, 3);
        grid.add(new Label("Pilihan D:"), 0, 4);
        grid.add(optionD, 1, 4);
        grid.add(new Label("Jawaban Benar:"), 0, 5);
        grid.add(correctOption, 1, 5);
        questionText.setPrefHeight(100);
        GridPane.setVgrow(questionText, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(questionText::requestFocus);

        // --- BAGIAN BARU: LOGIKA PENYIMPANAN ---
        // 4. Konversi hasil dialog menjadi objek Question saat tombol Simpan ditekan
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validasi: Pastikan semua field terisi
                if (questionText.getText().trim().isEmpty() || optionA.getText().trim().isEmpty() ||
                        optionB.getText().trim().isEmpty() || optionC.getText().trim().isEmpty() ||
                        optionD.getText().trim().isEmpty() || correctOption.getValue() == null) {

                    showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap isi semua kolom sebelum menyimpan.");
                    return null; // Kembalikan null agar dialog tidak tertutup
                }
                // Buat objek Question baru dari input form
                return new Question(
                        questionText.getText(),
                        optionA.getText(),
                        optionB.getText(),
                        optionC.getText(),
                        optionD.getText(),
                        correctOption.getValue()
                );
            }
            return null; // Jika tombol Batal atau lainnya ditekan
        });

        // 5. Tampilkan dialog dan proses hasilnya
        dialog.showAndWait().ifPresent(newQuestion -> {
            // Jika dialog menghasilkan objek Question (artinya valid dan disimpan)
            boolean success = questionDao.addQuestion(newQuestion);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Soal baru berhasil ditambahkan.");
                // Refresh tabel untuk menampilkan data baru
                refreshQuestionTable(questionTable);
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan soal ke database.");
            }
        });
    }

    private void showAdminPanel() {
        primaryStage.setTitle("Admin Dashboard - Manajemen Soal");

        // --- Layout Utama ---
        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("admin-root");

        // --- Sidebar (Menu di Kiri) ---
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // ... (Kode untuk membuat tombol-tombol sidebar tetap sama) ...
        Button manageQuestionsButton = new Button("Manajemen Soal");
        manageQuestionsButton.getStyleClass().add("sidebar-button");
        manageQuestionsButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
        Button manageUsersButton = new Button("Manajemen Pengguna");
        manageUsersButton.getStyleClass().add("sidebar-button");
        Button statisticsButton = new Button("Statistik Kuis");
        statisticsButton.getStyleClass().add("sidebar-button");
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("sidebar-button");
        logoutButton.setOnAction(e -> showLoginScreen());
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(manageQuestionsButton, manageUsersButton, statisticsButton, spacer, logoutButton);

        // Menempatkan sidebar di KIRI dan memberikan MARGIN
        mainLayout.setLeft(sidebar);
        BorderPane.setMargin(sidebar, new Insets(20, 0, 20, 20));

        // --- Area Konten (di Tengah) ---
        VBox contentCard = new VBox(20);
        contentCard.getStyleClass().add("content-card");
        contentCard.setPadding(new Insets(30));

        // ... (Kode untuk membuat title, table, dan tombol aksi tetap sama) ...
        Label titleLabel = new Label("Manajemen Soal Kuis");
        titleLabel.getStyleClass().add("title-text");
        TableView<Question> questionTable = new TableView<>();
        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Question, String> textCol = new TableColumn<>("Teks Pertanyaan");
        textCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        textCol.setPrefWidth(350);
        TableColumn<Question, String> correctCol = new TableColumn<>("Jawaban Benar");
        correctCol.setCellValueFactory(new PropertyValueFactory<>("correctOption"));
        questionTable.getColumns().addAll(idCol, textCol, correctCol);
        ObservableList<Question> questionList = FXCollections.observableArrayList(questionDao.getQuestions());
        questionTable.setItems(questionList);

        //Tambah Soal
        Button addButton = new Button("Tambah Soal");
        addButton.getStyleClass().addAll("action-button", "add-button");
        addButton.setOnAction(e -> showAddQuestionDialog(questionTable));

        //Edit Soal
        Button editButton = new Button("Edit Soal");
        editButton.getStyleClass().addAll("action-button", "edit-button");

        //Logika untuk mengedit soal
        editButton.setOnAction(e -> {
            // Dapatkan soal yang dipilih, sama seperti delete
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();

            if (selectedQuestion == null) {
                showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Silakan pilih soal yang ingin diedit terlebih dahulu.");
                return;
            }

            // Panggil dialog edit dengan membawa data soal yang dipilih
            showEditQuestionDialog(selectedQuestion, questionTable);
        });

        //Hapus Soal
        Button deleteButton = new Button("Hapus Soal");
        deleteButton.getStyleClass().addAll("action-button", "delete-button");

        // Logika untuk menghapus soal
        deleteButton.setOnAction(e -> {
            // 1. Dapatkan soal yang dipilih dari tabel
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();

            // 2. Periksa apakah ada soal yang dipilih
            if (selectedQuestion == null) {
                showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Silakan pilih soal yang ingin dihapus terlebih dahulu.");
                return; // Hentikan proses jika tidak ada yang dipilih
            }

            // 3. Tampilkan dialog konfirmasi
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Konfirmasi Hapus");
            confirmationAlert.setHeaderText("Anda akan menghapus soal berikut:");
            confirmationAlert.setContentText("ID: " + selectedQuestion.getId() + "\nSoal: " + selectedQuestion.getQuestionText());

            // Tampilkan dialog dan tunggu respons dari user
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // 4. Jika user menekan OK, hapus soal dari database
                    boolean success = questionDao.deleteQuestion(selectedQuestion.getId());

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Soal berhasil dihapus.");
                        // 5. Refresh tabel untuk menampilkan perubahan
                        refreshQuestionTable(questionTable);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus soal dari database.");
                    }
                }
            });
        });

        HBox buttonBox = new HBox(15, addButton, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        contentCard.getChildren().addAll(titleLabel, questionTable, buttonBox);

        // ----> INI BAGIAN PENTING YANG HILANG <----
        // Menempatkan 'kartu' konten di TENGAH dan memberikan MARGIN
        mainLayout.setCenter(contentCard);
        BorderPane.setMargin(contentCard, new Insets(20));

        // --- Scene ---
        Scene adminScene = new Scene(mainLayout, 950, 720);
        adminScene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        primaryStage.setScene(adminScene);
    }

    private void handleRegister(String username, String password, String confirmPassword) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua kolom harus diisi.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password dan konfirmasi password tidak cocok.");
            return;
        }
        boolean success = userDao.createUser(new User(username, password));
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Akun berhasil dibuat! Silakan login.");
            showLoginScreen();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuat akun. Username mungkin sudah digunakan.");
        }
    }

    private void handleNextQuestion() {
        RadioButton selectedRadioButton = (RadioButton) optionsGroup.getSelectedToggle();
        if (selectedRadioButton == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan pilih jawaban terlebih dahulu!");
            return;
        }
        if (getSelectedOption(selectedRadioButton).equalsIgnoreCase(questions.get(currentQuestionIndex).getCorrectOption())) {
            score++;
        }
        progressBar.setProgress((double) (currentQuestionIndex + 1) / questions.size());
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion(true);
        } else {
            showQuizComplete();
        }
    }

    private void showQuizComplete() {
        VBox card = new VBox();
        card.getStyleClass().add("card");

        Label title = new Label("Quiz Selesai!");
        title.getStyleClass().add("title-text");

        Label scoreLabel = new Label(score + "/" + questions.size());
        scoreLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: 700;");

        Label subtitle = new Label("Anda menjawab " + score + " dari " + questions.size() + " pertanyaan dengan benar.");
        subtitle.getStyleClass().add("subtitle-text");

        Button playAgainButton = new Button("Mainkan Lagi");
        playAgainButton.getStyleClass().add("primary-button");

        card.getChildren().addAll(title, scoreLabel, subtitle, playAgainButton);

        StackPane background = (StackPane) primaryStage.getScene().getRoot();
        background.getChildren().add(card);

        playAgainButton.setOnAction(e -> {
            background.getChildren().remove(card);
            resetQuiz();
            displayQuestion(false);
        });
    }

    private void resetQuiz() {
        currentQuestionIndex = 0;
        score = 0;
    }

    private void displayQuestion(boolean useAnimation) {
        if (useAnimation) {
            FadeTransition ft = new FadeTransition(Duration.millis(350), questionArea);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        }
        Question q = questions.get(currentQuestionIndex);
        questionText.setText(q.getQuestionText());
        ((RadioButton) optionsGroup.getToggles().get(0)).setText(q.getOptionA());
        ((RadioButton) optionsGroup.getToggles().get(1)).setText(q.getOptionB());
        ((RadioButton) optionsGroup.getToggles().get(2)).setText(q.getOptionC());
        ((RadioButton) optionsGroup.getToggles().get(3)).setText(q.getOptionD());
        optionsGroup.selectToggle(null);
        updateProgressInfo();
    }

    private void updateProgressInfo() {
        progressLabel.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
        progressBar.setProgress((double) (currentQuestionIndex) / questions.size());
    }

    private String getSelectedOption(RadioButton selected) {
        for (int i = 0; i < optionsGroup.getToggles().size(); i++) {
            if (optionsGroup.getToggles().get(i) == selected) {
                return String.valueOf((char) ('A' + i));
            }
        }
        return "";
    }

    private RadioButton createStyledRadioButton() {
        RadioButton rb = new RadioButton();
        rb.setToggleGroup(optionsGroup);
        rb.getStyleClass().add("radio-button");
        rb.setMaxWidth(Double.MAX_VALUE);
        return rb;
    }

    private Task<List<Question>> createTaskToLoadQuestions() {
        return new Task<>() {
            @Override
            protected List<Question> call() {
                return questionDao.getQuestions();
            }
        };
    }

    private void showErrorState(Pane container, String message) {
        Label errorLabel = new Label(message);
        container.getChildren().add(errorLabel);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }
}