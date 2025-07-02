import dao.UserDao;
import dao.questionDao;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Question;
import model.User;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class mainMenu extends Application {

    private Stage primaryStage;
    private UserDao userDao;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;

    private final int SCORE_BENAR = 10;
    private final int SCORE_SALAH = -2;

    private Label progressLabel;
    private ProgressBar progressBar;
    private Text questionText;
    private ImageView questionImageView;
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

    private void showAddQuestionDialog(TableView<Question> questionTable) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Tambah Soal Baru");
        dialog.setHeaderText("Silakan isi detail pertanyaan di bawah ini.");

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
                    showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap isi semua kolom wajib sebelum menyimpan.");
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
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Soal baru berhasil ditambahkan.");
                refreshQuestionTable(questionTable);
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan soal ke database.");
            }
        });
    }

    private void showEditQuestionDialog(Question questionToEdit, TableView<Question> questionTable) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Edit Soal");
        dialog.setHeaderText("Anda sedang mengedit soal dengan ID: " + questionToEdit.getId());

        ButtonType saveButtonType = new ButtonType("Simpan Perubahan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea questionText = new TextArea(questionToEdit.getQuestionText());
        questionText.setWrapText(true);

        TextField imagePathField = new TextField(questionToEdit.getImagePath());
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

        TextField optionA = new TextField(questionToEdit.getOptionA());
        TextField optionB = new TextField(questionToEdit.getOptionB());
        TextField optionC = new TextField(questionToEdit.getOptionC());
        TextField optionD = new TextField(questionToEdit.getOptionD());
        ComboBox<String> correctOption = new ComboBox<>();
        correctOption.getItems().addAll("A", "B", "C", "D");
        correctOption.setValue(questionToEdit.getCorrectOption());

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
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Soal berhasil diperbarui.");
                refreshQuestionTable(questionTable);
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui soal di database.");
            }
        });
    }

    private void showLoginScreen() {
        primaryStage.setTitle("Quiz App - Login");
        StackPane layout = createLoginLayout();
        Scene scene = new Scene(layout, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showRegisterScreen() {
        primaryStage.setTitle("Quiz App - Sign Up");
        StackPane layout = createRegisterLayout();
        Scene scene = new Scene(layout, 720, 720);
        scene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showQuizScreen() {
        primaryStage.setTitle("Quiz");
        StackPane quizRoot = new StackPane();
        Scene quizScene = new Scene(quizRoot, 720, 720);
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

        progressLabel = new Label();
        progressLabel.getStyleClass().add("progress-label");
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        VBox progressBox = new VBox(5, progressLabel, progressBar);
        progressBox.setPadding(new Insets(0, 0, 20, 0));

        questionText = new Text();
        questionText.getStyleClass().add("question-text");
        questionImageView = new ImageView();
        questionImageView.setPreserveRatio(true);
        questionImageView.setFitHeight(180);

        optionsGroup = new ToggleGroup();
        VBox optionsContainer = new VBox();
        optionsContainer.getStyleClass().add("options-container");
        for (int i = 0; i < 4; i++) {
            optionsContainer.getChildren().add(createStyledRadioButton());
        }

        questionArea = new VBox(10, questionText, questionImageView, optionsContainer);
        VBox.setVgrow(optionsContainer, Priority.ALWAYS);

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

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Username dan password tidak boleh kosong.");
            return;
        }
        User user = userDao.login(username, password);
        if (user != null) {
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                showAlert(Alert.AlertType.INFORMATION, "Login Berhasil", "Selamat datang, Admin " + user.getUsername() + "!");
                showAdminPanel();
            } else {
                showQuizScreen();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username atau password salah.");
        }
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
            score += SCORE_BENAR;
        } else {
            score += SCORE_SALAH;
        }

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
        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: 700;");
        Label subtitle = new Label("Skor akhir Anda adalah " + score + ".");
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

        String imagePath = q.getImagePath();
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                Image image = new Image(new File(imagePath).toURI().toString());
                questionImageView.setImage(image);
                questionImageView.setVisible(true);
            } catch (Exception e) {
                questionImageView.setImage(null);
                questionImageView.setVisible(false);
                System.err.println("Gagal memuat gambar: " + imagePath + ". Error: " + e.getMessage());
            }
        } else {
            questionImageView.setImage(null);
            questionImageView.setVisible(false);
        }

        ((RadioButton) optionsGroup.getToggles().get(0)).setText(q.getOptionA());
        ((RadioButton) optionsGroup.getToggles().get(1)).setText(q.getOptionB());
        ((RadioButton) optionsGroup.getToggles().get(2)).setText(q.getOptionC());
        ((RadioButton) optionsGroup.getToggles().get(3)).setText(q.getOptionD());
        optionsGroup.selectToggle(null);
        updateProgressInfo();
    }

    private void showAdminPanel() {
        primaryStage.setTitle("Admin Dashboard - Manajemen Soal");
        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("admin-root");

        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
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
        mainLayout.setLeft(sidebar);
        BorderPane.setMargin(sidebar, new Insets(20, 0, 20, 20));

        VBox contentCard = new VBox(20);
        contentCard.getStyleClass().add("content-card");
        contentCard.setPadding(new Insets(30));
        Label titleLabel = new Label("Manajemen Soal Kuis");
        titleLabel.getStyleClass().add("title-text");
        TableView<Question> questionTable = new TableView<>();
        setupQuestionTable(questionTable);

        Button addButton = new Button("Tambah Soal");
        addButton.getStyleClass().addAll("action-button", "add-button");
        addButton.setOnAction(e -> showAddQuestionDialog(questionTable));

        Button editButton = new Button("Edit Soal");
        editButton.getStyleClass().addAll("action-button", "edit-button");
        editButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Silakan pilih soal yang ingin diedit.");
                return;
            }
            showEditQuestionDialog(selectedQuestion, questionTable);
        });

        Button deleteButton = new Button("Hapus Soal");
        deleteButton.getStyleClass().addAll("action-button", "delete-button");
        deleteButton.setOnAction(e -> handleDeleteQuestion(questionTable));

        HBox buttonBox = new HBox(15, addButton, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        contentCard.getChildren().addAll(titleLabel, questionTable, buttonBox);
        mainLayout.setCenter(contentCard);
        BorderPane.setMargin(contentCard, new Insets(20));

        Scene adminScene = new Scene(mainLayout, 1000, 720);
        adminScene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        primaryStage.setScene(adminScene);
    }

    private void setupQuestionTable(TableView<Question> questionTable) {
        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Question, String> textCol = new TableColumn<>("Teks Pertanyaan");
        textCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        textCol.setPrefWidth(350);

        TableColumn<Question, String> imageCol = new TableColumn<>("Path Gambar");
        imageCol.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        imageCol.setPrefWidth(250);

        TableColumn<Question, String> correctCol = new TableColumn<>("Jawaban");
        correctCol.setCellValueFactory(new PropertyValueFactory<>("correctOption"));
        correctCol.setPrefWidth(100);

        questionTable.getColumns().addAll(idCol, textCol, imageCol, correctCol);
        refreshQuestionTable(questionTable);
    }

    private void handleDeleteQuestion(TableView<Question> questionTable) {
        Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Silakan pilih soal yang ingin dihapus.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Hapus");
        confirmationAlert.setHeaderText("Anda akan menghapus soal berikut:");
        confirmationAlert.setContentText("ID: " + selectedQuestion.getId() + "\nSoal: " + selectedQuestion.getQuestionText());

        Optional<ButtonType> response = confirmationAlert.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            boolean success = questionDao.deleteQuestion(selectedQuestion.getId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Soal berhasil dihapus.");
                refreshQuestionTable(questionTable);
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus soal dari database.");
            }
        }
    }

    private void updateProgressInfo() {
        progressLabel.setText("Soal " + (currentQuestionIndex + 1) + " dari " + questions.size());
        progressBar.setProgress((double) (currentQuestionIndex) / questions.size());
    }

    private String getSelectedOption(RadioButton selected) {
        for (int i = 0; i < optionsGroup.getToggles().size(); i++) {
            if (optionsGroup.getToggles().get(i).equals(selected)) {
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
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
                return questionDao.getQuestions();
            }
        };
    }

    private void showErrorState(Pane container, String message) {
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("subtitle-text");
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