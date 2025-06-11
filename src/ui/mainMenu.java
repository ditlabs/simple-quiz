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

}