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
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Model;
import model.User;
import model.Book; // Import Book model

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
    	                System.out.println("User authenticated successfully.");
    	                model.setCurrentUser(user);

    	                // Check if the user is an admin
    	                if (user.getUsername().equals("admin")) {
    	                    // Load Admin Dashboard for admin user
    	                    try {
    	                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminDashboardView.fxml"));
    	                        AdminDashboardController adminController = new AdminDashboardController(stage, model, user);
    	                        loader.setController(adminController);

    	                        Pane root = loader.load();
    	                        adminController.showStage(root);

    	                    } catch (IOException e) {
    	                        message.setText(e.getMessage());
    	                        e.printStackTrace();
    	                    }
    	                } else {
    	                    // Regular user home screen
    	                    try {
    	                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
    	                        HomeController homeController = new HomeController(stage, model, cart, user);
    	                        loader.setController(homeController);

    	                        BorderPane root = loader.load();
    	                        homeController.showStage(root);
    	                    } catch (IOException e) {
    	                        message.setText(e.getMessage());
    	                        e.printStackTrace();
    	                    }
    	                }
    	            } else {
    	                message.setText("Wrong username or password");
    	                System.out.println("Authentication failed.");
    	            }
    	        } catch (SQLException e) {
    	            message.setText(e.getMessage());
    	            e.printStackTrace();
    	        }
    	    } else {
    	        message.setText("Empty username or password");
    	        System.out.println("Empty username or password.");
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
                e.printStackTrace(); 
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

    // Method to reset the login screen after logout
    public void resetLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            loader.setController(this); 
            Pane root = loader.load();
            showStage(root); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
