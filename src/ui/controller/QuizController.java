// src/ui/controller/QuizController.java
package ui.controller;

import dao.questionDao;
import dao.resultDao;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import model.Question;
import ui.util.AlertHelper;
import ui.view.QuizView; // Import view
import util.SessionManager;

import java.util.List;

public class QuizController {
    private final Stage stage;
    private final QuizView view;
    private final resultDao resultDao = new resultDao();

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private final int SCORE_BENAR = 10;
    private final int SCORE_SALAH = -2;

    public QuizController(Stage stage) {
        this.stage = stage;
        this.view = new QuizView();
    }

    public void show() {
        stage.setTitle("Quiz");
        Scene quizScene = new Scene(view, 720, 720);
        quizScene.getStylesheets().add(getClass().getResource("/ui/styles.css").toExternalForm());

        loadQuestions();

        stage.setScene(quizScene);
    }

    private void loadQuestions() {
        view.showLoading(true);
        Task<List<Question>> loadQuestionsTask = new Task<>() {
            @Override
            protected List<Question> call() {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                return questionDao.getQuestions();
            }
        };

        loadQuestionsTask.setOnSucceeded(e -> Platform.runLater(() -> {
            questions = loadQuestionsTask.getValue();
            view.showLoading(false);
            if (questions == null || questions.isEmpty()) {
                view.showError("Tidak ada soal yang dapat dimuat.");
            } else {
                startQuiz();
            }
        }));

        loadQuestionsTask.setOnFailed(e -> Platform.runLater(() -> {
            view.showLoading(false);
            view.showError("Gagal terhubung ke database.");
        }));

        new Thread(loadQuestionsTask).start();
    }

    private void startQuiz() {
        view.buildAndShowQuizUI();
        setupEventHandlers();
        displayQuestion(false);
    }

    private void setupEventHandlers() {
        view.getSubmitButton().setOnAction(e -> handleNextQuestion());
    }

    private void handleNextQuestion() {
        RadioButton selectedRadioButton = (RadioButton) view.getOptionsGroup().getSelectedToggle();
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
        int userId = SessionManager.getInstance().getLoggedInUser().getId();
        resultDao.saveResult(userId, score);

        Button backToDashboardButton = new Button("Kembali ke Dashboard");
        backToDashboardButton.getStyleClass().add("primary-button");
        backToDashboardButton.setOnAction(e -> {
            new UserDashboardController(stage).show();
        });

        view.showCompletionScreen(score, backToDashboardButton);
    }

    private void displayQuestion(boolean useAnimation) {
        Question q = questions.get(currentQuestionIndex);
        view.displayQuestion(q, useAnimation);
        view.updateProgress(currentQuestionIndex, questions.size());
    }

    private String getSelectedOption(RadioButton selected) {
        for (int i = 0; i < view.getOptionsGroup().getToggles().size(); i++) {
            if (view.getOptionsGroup().getToggles().get(i).equals(selected)) {
                return String.valueOf((char) ('A' + i));
            }
        }
        return "";
    }
}