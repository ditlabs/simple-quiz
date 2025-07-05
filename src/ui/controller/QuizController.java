// ... (semua import tetap sama)
package ui.controller;

import dao.questionDao;
import dao.resultDao; // BARU
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Question;
import ui.util.AlertHelper;
import util.SessionManager; // BARU

import java.io.File;
import java.util.List;
public class QuizController {
    private final Stage stage;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private final int SCORE_BENAR = 10;
    private final int SCORE_SALAH = -2;
    private final resultDao resultDao = new resultDao(); // BARU

    private Label progressLabel;
    private ProgressBar progressBar;
    private Text questionText;
    private ImageView questionImageView;
    private ToggleGroup optionsGroup;
    private VBox questionArea;
    private StackPane rootPane;

    public QuizController(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        stage.setTitle("Quiz");
        rootPane = new StackPane();
        Scene quizScene = new Scene(rootPane, 720, 720);
        quizScene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());
        rootPane.getStyleClass().add("root");

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        rootPane.getChildren().add(loadingIndicator);

        Task<List<Question>> loadQuestionsTask = createTaskToLoadQuestions();
        loadQuestionsTask.setOnSucceeded(e -> Platform.runLater(() -> {
            questions = loadQuestionsTask.getValue();
            rootPane.getChildren().remove(loadingIndicator);
            if (questions == null || questions.isEmpty()) {
                showErrorState("Tidak ada soal yang dapat dimuat.");
            } else {
                StackPane mainQuizLayout = createQuizLayout();
                rootPane.getChildren().add(mainQuizLayout);
            }
        }));
        loadQuestionsTask.setOnFailed(e -> Platform.runLater(() -> {
            rootPane.getChildren().remove(loadingIndicator);
            showErrorState("Gagal terhubung ke database.");
        }));

        new Thread(loadQuestionsTask).start();
        stage.setScene(quizScene);
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

    private void handleNextQuestion() {
        RadioButton selectedRadioButton = (RadioButton) optionsGroup.getSelectedToggle();
        if (selectedRadioButton == null) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, stage, "Peringatan", "Silakan pilih jawaban terlebih dahulu!");
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
        // BARU: Simpan hasil kuis ke database
        int userId = SessionManager.getInstance().getLoggedInUser().getId();
        resultDao.saveResult(userId, score);

        VBox card = new VBox();
        card.getStyleClass().add("card");
        Label title = new Label("Quiz Selesai!");
        title.getStyleClass().add("title-text");
        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: 700;");
        Label subtitle = new Label("Skor akhir Anda adalah " + score + ".");
        subtitle.getStyleClass().add("subtitle-text");

        Button backToDashboardButton = new Button("Kembali ke Dashboard"); // DIUBAH
        backToDashboardButton.getStyleClass().add("primary-button");

        card.getChildren().addAll(title, scoreLabel, subtitle, backToDashboardButton);
        rootPane.getChildren().add(card);

        backToDashboardButton.setOnAction(e -> { // DIUBAH
            UserDashboardController dashboardController = new UserDashboardController(stage);
            dashboardController.show();
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
                questionImageView.setManaged(true);
            } catch (Exception e) {
                questionImageView.setImage(null);
                questionImageView.setVisible(false);
                questionImageView.setManaged(false);
                System.err.println("Gagal memuat gambar: " + imagePath + ". Error: " + e.getMessage());
            }
        } else {
            questionImageView.setImage(null);
            questionImageView.setVisible(false);
            questionImageView.setManaged(false);
        }

        ((RadioButton) optionsGroup.getToggles().get(0)).setText(q.getOptionA());
        ((RadioButton) optionsGroup.getToggles().get(1)).setText(q.getOptionB());
        ((RadioButton) optionsGroup.getToggles().get(2)).setText(q.getOptionC());
        ((RadioButton) optionsGroup.getToggles().get(3)).setText(q.getOptionD());
        optionsGroup.selectToggle(null);
        updateProgressInfo();
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
                // Simulasi delay untuk UX yang lebih baik
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                return questionDao.getQuestions();
            }
        };
    }

    private void showErrorState(String message) {
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("subtitle-text");
        rootPane.getChildren().add(errorLabel);
    }
}