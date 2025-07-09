// src/ui/view/QuizView.java
package ui.view;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.Question;

import java.io.File;

public class QuizView extends StackPane {

    // ... (semua deklarasi variabel tetap sama) ...
    private final Label progressLabel;
    private final ProgressBar progressBar;
    private final Text questionText;
    private final ImageView questionImageView;
    private final ToggleGroup optionsGroup;
    private final VBox optionsContainer;
    private final Button submitButton;
    private final VBox questionArea;
    private final ProgressIndicator loadingIndicator;
    private VBox quizCard;
    private VBox completionCard;


    public QuizView() {
        // ... (isi constructor tetap sama) ...
        this.getStyleClass().add("root");
        loadingIndicator = new ProgressIndicator();
        quizCard = new VBox();
        progressLabel = new Label();
        progressBar = new ProgressBar(0);
        questionText = new Text();
        questionImageView = new ImageView();
        optionsGroup = new ToggleGroup();
        optionsContainer = new VBox();
        submitButton = new Button("Submit");
        questionArea = new VBox(10);
        buildQuizUI();
        this.getChildren().add(loadingIndicator);
    }

    public void buildQuizUI() {
        // ... (isi metode ini tetap sama) ...
    }

    public void showQuizCard(boolean show) {
        // ... (isi metode ini tetap sama) ...
    }

    public void displayQuestion(Question q, boolean useAnimation) {
        if (useAnimation) {
            FadeTransition ft = new FadeTransition(Duration.millis(350), questionArea);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        }
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
        // updateProgressInfo(); // Controller yang akan memanggil ini
    }

    public void updateProgress(int current, int total) {
        progressLabel.setText("Soal " + (current + 1) + " dari " + total);
        progressBar.setProgress((double) (current) / total);
    }

    public void showCompletionScreen(int finalScore, Button backToDashboardButton) {
        this.getChildren().remove(quizCard);

        completionCard = new VBox();
        completionCard.getStyleClass().add("card");
        Label title = new Label("Quiz Selesai!");
        title.getStyleClass().add("title-text");
        Label scoreLabel = new Label(String.valueOf(finalScore));
        scoreLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: 700;");
        Label subtitle = new Label("Skor akhir Anda adalah " + finalScore + ".");
        subtitle.getStyleClass().add("subtitle-text");

        completionCard.getChildren().addAll(title, scoreLabel, subtitle, backToDashboardButton);
        this.getChildren().add(completionCard);
    }

    public void showLoading(boolean isLoading) {
        loadingIndicator.setVisible(isLoading);
    }

    public void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("subtitle-text");
        this.getChildren().add(errorLabel);
    }

    // --- PERBAIKAN DI SINI ---
    private RadioButton createStyledRadioButton() {
        RadioButton rb = new RadioButton();
        rb.setToggleGroup(optionsGroup);
        rb.getStyleClass().add("radio-button");
        rb.setMaxWidth(Double.MAX_VALUE);
        return rb; // Baris inilah yang hilang
    }

    // Getters
    public ToggleGroup getOptionsGroup() { return optionsGroup; }
    public Button getSubmitButton() { return submitButton; }
}