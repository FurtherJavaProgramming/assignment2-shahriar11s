package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;
import model.User;
import model.Book;  // Import Book model

public class LoginController {
    @FXML
    private TextField name;  
    @FXML
    private PasswordField password;  
    @FXML
    private Label message;  
    @FXML
    private Button login;  
    @FXML
    private Button signup;  

    private Model model;
    private Stage stage;
    private Map<Book, Integer> cart;  // Cart to be passed to HomeController

    public LoginController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
        this.cart = new HashMap<>();  // Initialize an empty cart
    }

    @FXML
    public void initialize() {        
        login.setOnAction(event -> {
            if (!name.getText().isEmpty() && !password.getText().isEmpty()) {
                User user;
                try {
                    user = model.getUserDao().getUser(name.getText(), password.getText());
                    if (user != null) {
                        System.out.println("User authenticated successfully."); // Debugging print
                        model.setCurrentUser(user);
                        try {
                            System.out.println("Loading HomeView.fxml...");  // Debugging print
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
                            
                            // Pass cart to HomeController along with stage and model
                            HomeController homeController = new HomeController(stage, model, cart);
                            loader.setController(homeController);
                            
                            VBox root = loader.load();  // Ensure this matches the root layout in the FXML
                            System.out.println("HomeView loaded successfully.");  // Debugging print
                            homeController.showStage(root);  // Pass the root to the showStage method
                            
                            // Commenting this out to prevent closing the main stage too early
                            // stage.close();
                        } catch (IOException e) {
                            message.setText(e.getMessage());
                            e.printStackTrace(); // Print the error to the console
                        }
                    } else {
                        message.setText("Wrong username or password");
                        System.out.println("Authentication failed."); // Debugging print
                    }
                } catch (SQLException e) {
                    message.setText(e.getMessage());
                    e.printStackTrace(); // Print the error to the console
                }
            } else {
                message.setText("Empty username or password");
                System.out.println("Empty username or password."); // Debugging print
            }
            name.clear();
            password.clear();
        });

        signup.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SignupView.fxml"));
                SignupController signupController = new SignupController(stage, model);
                loader.setController(signupController);
                Pane root = loader.load();  
                signupController.showStage(root);
                stage.close();
            } catch (IOException e) {
                message.setText(e.getMessage());
                e.printStackTrace(); // Print the error to the console
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
