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

    private final Label progressLabel;
    private final ProgressBar progressBar;
    private final Text questionText;
    private final ImageView questionImageView;
    private final ToggleGroup optionsGroup;
    private final VBox optionsContainer;
    private final Button submitButton;
    private final VBox questionArea;
    private final ProgressIndicator loadingIndicator;
    private VBox quizCard; // Dibuat saat dibutuhkan

    public QuizView() {
        this.getStyleClass().add("root");

        // Inisialisasi semua komponen yang akan digunakan
        loadingIndicator = new ProgressIndicator();
        progressLabel = new Label();
        progressBar = new ProgressBar(0);
        questionText = new Text();
        questionImageView = new ImageView();
        optionsGroup = new ToggleGroup();
        optionsContainer = new VBox();
        submitButton = new Button("Submit");
        questionArea = new VBox(10);

        // Tampilkan loading indicator secara default
        this.getChildren().add(loadingIndicator);
    }

    /**
     * Metode ini membangun panel kuis dan menampilkannya di layar.
     * Dipanggil oleh Controller setelah data soal siap.
     */
    public void buildAndShowQuizUI() {
        // 1. Buat VBox utama untuk kartu kuis
        quizCard = new VBox();
        quizCard.getStyleClass().add("quiz-card");

        // 2. Siapkan bagian progress bar
        progressLabel.getStyleClass().add("progress-label");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        VBox progressBox = new VBox(5, progressLabel, progressBar);
        progressBox.setPadding(new Insets(0, 0, 20, 0));

        // 3. Siapkan bagian teks pertanyaan dan gambar
        questionText.getStyleClass().add("question-text");
        questionImageView.setPreserveRatio(true);
        questionImageView.setFitHeight(180);

        // 4. Siapkan bagian pilihan jawaban (RadioButton)
        optionsContainer.getStyleClass().add("options-container");
        optionsContainer.getChildren().clear(); // Pastikan kosong sebelum diisi ulang
        for (int i = 0; i < 4; i++) {
            optionsContainer.getChildren().add(createStyledRadioButton());
        }

        // 5. Gabungkan teks, gambar, dan pilihan jawaban
        questionArea.getChildren().setAll(questionText, questionImageView, optionsContainer);
        VBox.setVgrow(optionsContainer, Priority.ALWAYS);

        // 6. Siapkan tombol submit
        submitButton.getStyleClass().add("primary-button");

        // 7. Masukkan semua bagian ke dalam kartu kuis
        quizCard.getChildren().addAll(progressBox, questionArea, submitButton);
        VBox.setVgrow(questionArea, Priority.ALWAYS);

        // 8. Tampilkan kartu kuis di layar
        this.getChildren().add(quizCard);
    }

    /**
     * Metode ini mengisi data pertanyaan ke komponen UI yang sudah ada.
     */
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
    }

    /**
     * Memperbarui progress bar dan label soal.
     */
    public void updateProgress(int current, int total) {
        progressLabel.setText("Soal " + (current + 1) + " dari " + total);
        progressBar.setProgress((double) (current) / total);
    }

    /**
     * Menampilkan layar hasil akhir kuis.
     */
    public void showCompletionScreen(int finalScore, Button backToDashboardButton) {
        // Hapus kartu kuis sebelum menampilkan hasil
        if (quizCard != null) {
            this.getChildren().remove(quizCard);
        }

        VBox completionCard = new VBox();
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

    /**
     * Menampilkan atau menyembunyikan indikator loading.
     */
    public void showLoading(boolean isLoading) {
        loadingIndicator.setVisible(isLoading);
    }

    /**
     * Menampilkan pesan error di tengah layar.
     */
    public void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("subtitle-text");
        this.getChildren().add(errorLabel);
    }

    /**
     * Membuat satu buah RadioButton dengan style yang sudah ditentukan.
     */
    private RadioButton createStyledRadioButton() {
        RadioButton rb = new RadioButton();
        rb.setToggleGroup(optionsGroup);
        rb.getStyleClass().add("radio-button");
        rb.setMaxWidth(Double.MAX_VALUE);
        return rb;
    }

    // Getters untuk diakses oleh Controller
    public ToggleGroup getOptionsGroup() { return optionsGroup; }
    public Button getSubmitButton() { return submitButton; }
}