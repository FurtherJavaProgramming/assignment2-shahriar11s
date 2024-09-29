package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import model.Model;
import model.Book;

import java.util.Map;

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
    private Map<Book, Integer> cart;  // Cart map

    public HomeController(Stage stage, Model model, Map<Book, Integer> cart) {
        this.stage = stage;
        this.model = model;
        this.cart = cart;  // Initialize the cart map
    }

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to The Reading Room Bookstore!");

        // List All Books button action
        listBooksBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookListView.fxml"));
                BookListController bookListController = new BookListController(new Stage(), stage, cart);  // Pass both stages and the cart
                loader.setController(bookListController);

                Pane root = loader.load();
                bookListController.showStage(root);
                stage.hide();  // Hide home stage
                System.out.println("Book list page shown.");
            } catch (Exception e) {
                System.out.println("Error loading BookListView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Add to Cart button action (loading SearchBookView.fxml)
        addBookBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SearchBookView.fxml"));
                SearchBookController searchBookController = new SearchBookController(new Stage(), stage, cart);  // Pass cart here as well
                loader.setController(searchBookController);
                
                Pane root = loader.load();
                searchBookController.showStage(root);
                stage.hide();  // Hide home stage
            } catch (Exception e) {
                System.out.println("Error loading SearchBookView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // View Cart button action
        viewCartBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CartView.fxml"));
                CartController cartController = new CartController(new Stage(), stage, cart);  // Pass cart
                loader.setController(cartController);

                Pane root = loader.load();
                cartController.showStage(root);
                stage.hide();
            } catch (Exception e) {
                System.out.println("Error loading CartView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        quitBtn.setOnAction(event -> stage.close());  // Close only when the Quit button is pressed
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 450);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Home");
        stage.show(); 
    }
}
