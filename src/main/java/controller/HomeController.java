package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem; // Import MenuItem for the Profile menu
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;
import model.Model;
import dao.BookDao;
import model.User;  // Make sure to import User

import java.util.List;
import java.util.Map;

public class HomeController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private TableView<Book> topBooksTable;
    @FXML
    private TableColumn<Book, String> titleCol;
    @FXML
    private TableColumn<Book, String> authorCol;
    @FXML
    private TableColumn<Book, Integer> soldCol;
    @FXML
    private Button addBookBtn;
    @FXML
    private Button viewCartBtn;
    @FXML
    private Button checkoutBtn;
    @FXML
    private Button listBooksBtn;
    @FXML
    private Button quitBtn;

    // MenuItem for Update Profile in the MenuBar
    @FXML
    private MenuItem updateProfile;  // UpdateProfile is linked to the MenuItem in HomeView.fxml

    private Stage stage;
    private Model model;
    private Map<Book, Integer> cart;
    private User currentUser;  // Add a User instance variable

 // Constructor update
    public HomeController(Stage stage, Model model, Map<Book, Integer> cart, User currentUser) {
        this.stage = stage;
        this.model = model;
        this.cart = cart;
        this.currentUser = currentUser;  // Assign currentUser
    }


    @FXML
    public void initialize() {
        if (currentUser != null) {
            String firstName = currentUser.getFirstName();
            welcomeLabel.setText("Welcome, " + firstName + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }

        // Set up table columns for top books
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        soldCol.setCellValueFactory(new PropertyValueFactory<>("sold"));

        // Load and display the top 5 popular books based on the number of sold copies
        List<Book> topBooks = BookDao.getTopBooks(5);
        ObservableList<Book> topBooksList = FXCollections.observableArrayList(topBooks);
        topBooksTable.setItems(topBooksList);

        // List All Books button action
        listBooksBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookListView.fxml"));
                BookListController bookListController = new BookListController(new Stage(), stage, model, cart, currentUser);
                loader.setController(bookListController);

                Pane root = loader.load();
                bookListController.showStage(root);
                stage.hide();
            } catch (Exception e) {
                System.out.println("Error loading BookListView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Add to Cart button action
        addBookBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SearchBookView.fxml"));
                SearchBookController searchBookController = new SearchBookController(new Stage(), stage, model, cart, currentUser);
                loader.setController(searchBookController);

                Pane root = loader.load();
                searchBookController.showStage(root);
                stage.hide();
            } catch (Exception e) {
                System.out.println("Error loading SearchBookView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // View Cart button action
        viewCartBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CartView.fxml"));
                // Pass the currentUser to the CartController
                CartController cartController = new CartController(new Stage(), stage, model, cart, currentUser);  // Pass currentUser
                loader.setController(cartController);

                Pane root = loader.load();
                cartController.showStage(root);
                stage.hide();
            } catch (Exception e) {
                System.out.println("Error loading CartView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Update Profile MenuItem action (not a button anymore)
        updateProfile.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditProfileView.fxml"));
                EditProfileController editProfileController = new EditProfileController(new Stage(), model);
                loader.setController(editProfileController);

                Pane root = loader.load();
                editProfileController.showStage(root);
            } catch (Exception e) {
                System.out.println("Error loading EditProfileView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Quit button action
        quitBtn.setOnAction(event -> stage.close());
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Home");
        stage.show();
    }
}
