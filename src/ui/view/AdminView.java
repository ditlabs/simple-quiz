// src/ui/view/AdminView.java
package ui.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.Question;
import model.Result;
import model.User;

import java.sql.Timestamp;
import java.util.List;

public class AdminView extends BorderPane {

    private final ToggleButton manageQuestionsButton;
    private final ToggleButton manageUsersButton;
    private final ToggleButton statisticsButton;
    private final Button logoutButton;

    // Komponen untuk Manajemen Soal
    private TableView<Question> questionTable;
    private Button addQuestionButton, editQuestionButton, deleteQuestionButton;

    // Komponen untuk Manajemen Pengguna
    private TableView<User> userTable;
    private Button editRoleButton, deleteUserButton;

    // Komponen untuk Statistik
    private TableView<Result> leaderboardTable;

    public AdminView() {
        this.getStyleClass().add("admin-root");

        VBox sidebar = createSidebar();
        this.setLeft(sidebar);
        BorderPane.setMargin(sidebar, new Insets(20, 0, 20, 20));

        // Inisialisasi tombol-tombol
        manageQuestionsButton = new ToggleButton("Manajemen Soal");
        manageUsersButton = new ToggleButton("Manajemen Pengguna");
        statisticsButton = new ToggleButton("Statistik Kuis");
        logoutButton = new Button("Logout");

        setupSidebar(sidebar);

        // Tampilkan view awal
        this.setCenter(createQuestionManagementView());
    }

    private void setupSidebar(VBox sidebar) {
        ToggleGroup toggleGroup = new ToggleGroup();
        manageQuestionsButton.setToggleGroup(toggleGroup);
        manageUsersButton.setToggleGroup(toggleGroup);
        statisticsButton.setToggleGroup(toggleGroup);
        toggleGroup.selectToggle(manageQuestionsButton);

        styleSidebarButton(manageQuestionsButton);
        styleSidebarButton(manageUsersButton);
        styleSidebarButton(statisticsButton);
        styleSidebarButton(logoutButton);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(manageQuestionsButton, manageUsersButton, statisticsButton, spacer, logoutButton);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private void styleSidebarButton(ButtonBase button) {
        button.getStyleClass().add("sidebar-button");
        button.setPrefWidth(200);
        button.setAlignment(Pos.CENTER_LEFT);
    }

    // --- View Builders ---
    public Node createQuestionManagementView() {
        VBox container = new VBox(20);
        container.getStyleClass().add("content-card");
        container.setPadding(new Insets(30));

        Label title = new Label("Manajemen Soal Kuis");
        title.getStyleClass().add("title-text");

        questionTable = new TableView<>();
        setupQuestionTable(questionTable);

        addQuestionButton = new Button("Tambah Soal");
        addQuestionButton.getStyleClass().addAll("action-button", "add-button");
        editQuestionButton = new Button("Edit Soal");
        editQuestionButton.getStyleClass().addAll("action-button", "edit-button");
        deleteQuestionButton = new Button("Hapus Soal");
        deleteQuestionButton.getStyleClass().addAll("action-button", "delete-button");

        HBox buttonBox = new HBox(15, addQuestionButton, editQuestionButton, deleteQuestionButton);
        container.getChildren().addAll(title, questionTable, buttonBox);
        VBox.setVgrow(questionTable, Priority.ALWAYS);
        BorderPane.setMargin(container, new Insets(20));
        return container;
    }

    public Node createUserManagementView() {
        VBox container = new VBox(20);
        container.getStyleClass().add("content-card");
        container.setPadding(new Insets(30));

        Label title = new Label("Manajemen Pengguna");
        title.getStyleClass().add("title-text");

        userTable = new TableView<>();
        setupUserTable(userTable);

        editRoleButton = new Button("Ubah Peran");
        editRoleButton.getStyleClass().addAll("action-button", "edit-button");
        deleteUserButton = new Button("Hapus Pengguna");
        deleteUserButton.getStyleClass().addAll("action-button", "delete-button");

        HBox buttonBox = new HBox(15, editRoleButton, deleteUserButton);
        container.getChildren().addAll(title, userTable, buttonBox);
        VBox.setVgrow(userTable, Priority.ALWAYS);
        BorderPane.setMargin(container, new Insets(20));
        return container;
    }

    public Node createStatsView(List<Result> allResults) {
        VBox container = new VBox(20);
        container.getStyleClass().add("content-card");
        container.setPadding(new Insets(30));

        Label title = new Label("Statistik Kuis");
        title.getStyleClass().add("title-text");

        Label leaderboardTitle = new Label("Papan Peringkat (Top 10)");
        leaderboardTable = new TableView<>();
        setupLeaderboardTable(leaderboardTable);
        leaderboardTable.setItems(FXCollections.observableArrayList(allResults));

        double averageScore = allResults.stream().mapToInt(Result::getScore).average().orElse(0);
        Label avgLabel = new Label(String.format("Rata-rata Skor Keseluruhan: %.2f", averageScore));

        container.getChildren().addAll(title, leaderboardTitle, leaderboardTable, avgLabel);
        VBox.setVgrow(leaderboardTable, Priority.ALWAYS);
        BorderPane.setMargin(container, new Insets(20));
        return container;
    }

    // --- Table Setups ---
    private void setupQuestionTable(TableView<Question> table) {
        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Question, String> textCol = new TableColumn<>("Teks Pertanyaan");
        textCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        TableColumn<Question, String> correctCol = new TableColumn<>("Jawaban");
        correctCol.setCellValueFactory(new PropertyValueFactory<>("correctOption"));
        idCol.setPrefWidth(50);
        textCol.setPrefWidth(400);
        correctCol.setPrefWidth(150);
        table.getColumns().addAll(idCol, textCol, correctCol);
    }

    private void setupUserTable(TableView<User> table) {
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        table.getColumns().addAll(idCol, usernameCol, roleCol);
    }

    private void setupLeaderboardTable(TableView<Result> table) {
        TableColumn<Result, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<Result, Integer> scoreCol = new TableColumn<>("Skor");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        TableColumn<Result, Timestamp> dateCol = new TableColumn<>("Tanggal");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("quizDate"));
        table.getColumns().addAll(usernameCol, scoreCol, dateCol);
    }

    // --- Getters for Controller ---
    public ToggleButton getManageQuestionsButton() { return manageQuestionsButton; }
    public ToggleButton getManageUsersButton() { return manageUsersButton; }
    public ToggleButton getStatisticsButton() { return statisticsButton; }
    public Button getLogoutButton() { return logoutButton; }

    public TableView<Question> getQuestionTable() { return questionTable; }
    public Button getAddQuestionButton() { return addQuestionButton; }
    public Button getEditQuestionButton() { return editQuestionButton; }
    public Button getDeleteQuestionButton() { return deleteQuestionButton; }

    public TableView<User> getUserTable() { return userTable; }
    public Button getEditRoleButton() { return editRoleButton; }
    public Button getDeleteUserButton() { return deleteUserButton; }
}