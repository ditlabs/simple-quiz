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

    
}