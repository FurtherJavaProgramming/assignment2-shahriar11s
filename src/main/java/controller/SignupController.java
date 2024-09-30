package controller;

import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;
import model.User;

public class SignupController {
    @FXML
    private TextField username;
    @FXML
    private TextField password;
    @FXML
    private TextField firstName;  // New field for first name
    @FXML
    private TextField lastName;   // New field for last name
    @FXML
    private Button createUser;
    @FXML
    private Button close;
    @FXML
    private Label status;

    private Stage stage;
    private Stage parentStage;
    private Model model;

    public SignupController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        createUser.setOnAction(event -> {
            if (!username.getText().isEmpty() && !password.getText().isEmpty()
                    && !firstName.getText().isEmpty() && !lastName.getText().isEmpty()) {
                try {
                    User user = model.getUserDao().createUser(username.getText(), password.getText(),
                            firstName.getText(), lastName.getText());
                    if (user != null) {
                        status.setText("User created successfully! Redirecting to login...");
                        status.setStyle("-fx-text-fill: green;");
                        
                        // Wait for a couple of seconds and then navigate back to the login page
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);  // Wait for 2 seconds before redirecting
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            javafx.application.Platform.runLater(() -> {
                                parentStage.show();
                                stage.close();  // Close the signup page
                            });
                        }).start();
                    } else {
                        status.setText("Cannot create user");
                        status.setStyle("-fx-text-fill: red;");
                    }
                } catch (SQLException e) {
                    status.setText(e.getMessage());
                    status.setStyle("-fx-text-fill: red;");
                }
            } else {
                status.setText("Please fill all fields");
                status.setStyle("-fx-text-fill: red;");
            }
        });

        close.setOnAction(event -> {
            parentStage.show();
            stage.close();
        });
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Sign Up");
        stage.show();
    }
}
