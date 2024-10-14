package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;
import model.User;

public class AdminDashboardController {

    @FXML
    private Label adminWelcomeLabel;  // Updated to match the ID from FXML

    private Stage stage;
    private Model model;
    private User currentUser;

    public AdminDashboardController(Stage stage, Model model, User currentUser) {
        this.stage = stage;
        this.model = model;
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        if (currentUser != null) {
            String firstName = currentUser.getFirstName();
            adminWelcomeLabel.setText("Welcome, " + firstName + "!");  // Update to use the correct label
        } else {
            adminWelcomeLabel.setText("Welcome, Admin!");
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Admin Dashboard");
        stage.show();
    }
}
