import javafx.application.Application;
import javafx.stage.Stage;
import ui.controller.LoginController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Aplikasi Kuis");
        primaryStage.setResizable(false);

        // Langsung arahkan ke LoginController sebagai layar pertama
        LoginController loginController = new LoginController(primaryStage);
        loginController.show();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}