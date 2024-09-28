package controller;

import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.User;

public class LoginController {
    @FXML
    private TextField name; // Text field for username input
    @FXML
    private PasswordField password; // Password field for password input
    @FXML
    private Label message; // Label to display error messages or feedback
    @FXML
    private Button login; // Button to trigger login action
    @FXML
    private Button signup; // Button to trigger signup action

    private Model model; // Model to access data and logic
    private Stage stage; // Current stage

    public LoginController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    @FXML
    public void initialize() {        
        login.setOnAction(event -> {
            if (!name.getText().isEmpty() && !password.getText().isEmpty()) {
                User user;
                try {
                    user = model.getUserDao().getUser(name.getText(), password.getText());
                    if (user != null) {
                        model.setCurrentUser(user);
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
                            HomeController homeController = new HomeController(stage, model);
                            loader.setController(homeController);
                            VBox root = loader.load();
                            homeController.showStage(root);
                            stage.close();
                        } catch (IOException e) {
                            message.setText(e.getMessage());
                        }
                    } else {
                        message.setText("Wrong username or password");
                    }
                } catch (SQLException e) {
                    message.setText(e.getMessage());
                }
            } else {
                message.setText("Empty username or password");
            }
            name.clear();
            password.clear();
        });

        signup.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SignupView.fxml"));
                SignupController signupController = new SignupController(stage, model);
                loader.setController(signupController);
                VBox root = loader.load();
                signupController.showStage(root);
                stage.close();
            } catch (IOException e) {
                message.setText(e.getMessage());
            }
        });
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Welcome");
        stage.show(); 
    }
}
