// src/ui/view/LoginView.java
package ui.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView extends StackPane { // Meng-extend StackPane agar bisa langsung dijadikan root

    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Button loginButton;
    private final Hyperlink registerLink;

    public LoginView() {
        // Inisialisasi semua komponen UI
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        loginButton = new Button("Log In");
        loginButton.getStyleClass().add("primary-button");

        registerLink = new Hyperlink("Don't have an account? Sign up");
        registerLink.getStyleClass().add("hyperlink-style");

        // Membuat card layout
        VBox card = new VBox(25);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Welcome Back!");
        title.getStyleClass().add("title-text");

        card.getChildren().addAll(title, usernameField, passwordField, loginButton, registerLink);

        // Menambahkan card ke root (StackPane ini sendiri)
        this.getChildren().add(card);
        this.getStyleClass().add("root");
    }

    // Getter untuk komponen yang perlu diakses oleh Controller
    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public Hyperlink getRegisterLink() {
        return registerLink;
    }
}