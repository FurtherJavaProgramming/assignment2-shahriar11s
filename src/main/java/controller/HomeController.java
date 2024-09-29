package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;

public class HomeController {
    @FXML
    private Label welcomeLabel;  
    @FXML
    private MenuItem viewProfile;
    @FXML
    private MenuItem updateProfile;
    @FXML
    private Button addBookBtn;
    @FXML
    private Button viewCartBtn;
    @FXML
    private Button removeBookBtn;
    @FXML
    private Button checkoutBtn;
    @FXML
    private Button listBooksBtn;
    @FXML
    private Button quitBtn;

    private Stage stage;
    private Model model;

    public HomeController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to The Reading Room Bookstore!");

        addBookBtn.setOnAction(event -> System.out.println("Add Book to Cart clicked!"));
        viewCartBtn.setOnAction(event -> System.out.println("View Shopping Cart clicked!"));
        removeBookBtn.setOnAction(event -> System.out.println("Remove Book from Cart clicked!"));
        checkoutBtn.setOnAction(event -> System.out.println("Checkout clicked!"));
        listBooksBtn.setOnAction(event -> System.out.println("List All Books clicked!"));
        quitBtn.setOnAction(event -> stage.close());  // Close only when the Quit button is pressed
    }

    public void showStage(Pane root) {
        try {
            Scene scene = new Scene(root, 870, 450);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Home");
            stage.show();
            System.out.println("Home stage is shown successfully.");  // Debugging print
        } catch (Exception e) {
            System.out.println("Error showing the home stage: " + e.getMessage());
            e.printStackTrace();  // Print any errors to the console
        }
    }
}
