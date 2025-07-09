package ui.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RegisterView extends StackPane {

    private final TextField usernameField;
    private final PasswordField passwordField;
    private final PasswordField confirmField;
    private final Button registerButton;
    private final Hyperlink loginLink;

    public RegisterView() {
        VBox card = new VBox(25);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Create Account");
        title.getStyleClass().add("title-text");

        usernameField = new TextField();
        usernameField.setPromptText("Choose a username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Create a password");

        confirmField = new PasswordField();
        confirmField.setPromptText("Confirm your password");

        registerButton = new Button("Sign Up");
        registerButton.getStyleClass().add("primary-button");

        loginLink = new Hyperlink("Already have an account? Log In");
        loginLink.getStyleClass().add("hyperlink-style");

        card.getChildren().addAll(title, usernameField, passwordField, confirmField, registerButton, loginLink);

        this.getChildren().add(card);
        this.getStyleClass().add("root");
    }

    // Getters
    public TextField getUsernameField() { return usernameField; }
    public PasswordField getPasswordField() { return passwordField; }
    public PasswordField getConfirmField() { return confirmField; }
    public Button getRegisterButton() { return registerButton; }
    public Hyperlink getLoginLink() { return loginLink; }
}