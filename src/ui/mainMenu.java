import dao.UserDao;
import dao.questionDao;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
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

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userDao = new UserDao();

        showLoginScreen();
        primaryStage.setResizable(false);
        primaryStage.show();
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
            showQuizScreen();
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

}