// src/ui/view/UserDashboardView.java
package ui.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class UserDashboardView extends StackPane {

    private final Button startQuizButton;
    private final Button historyButton;
    private final Button logoutButton;
    private final Label titleLabel;

    public UserDashboardView() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);

        titleLabel = new Label(); // Teks akan diatur oleh controller
        titleLabel.getStyleClass().add("title-text");

        startQuizButton = new Button("Mulai Kuis");
        startQuizButton.getStyleClass().add("primary-button");

        historyButton = new Button("Riwayat Kuis");
        historyButton.getStyleClass().add("primary-button");

        logoutButton = new Button("🚪 Logout");
        logoutButton.getStyleClass().add("logout-button");

        card.getChildren().addAll(titleLabel, startQuizButton, historyButton, logoutButton);

        this.getChildren().add(card);
        this.getStyleClass().add("root");
    }

    // Getters untuk diakses Controller
    public Label getTitleLabel() {
        return titleLabel;
    }

    public Button getStartQuizButton() {
        return startQuizButton;
    }

    public Button getHistoryButton() {
        return historyButton;
    }

    public Button getLogoutButton() {
        return logoutButton;
    }
}