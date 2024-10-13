package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;
import model.Model;
import dao.BookDao;
import model.User;
import util.WindowManager;

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

    // MenuItem in the MenuBar
    @FXML
    private MenuItem updateProfile;
    @FXML
    private MenuItem viewOrders;

    private Stage stage;
    private Model model;
    private Map<Book, Integer> cart;
    private User currentUser;

    public HomeController(Stage stage, Model model, Map<Book, Integer> cart, User currentUser) {
        this.stage = stage;
        this.model = model;
        this.cart = cart;
        this.currentUser = currentUser;
    }


    @FXML
    public void initialize() {
        if (currentUser != null) {
            String firstName = currentUser.getFirstName();
            welcomeLabel.setText("Welcome, " + firstName + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }

        //table for top books
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
                CartController cartController = new CartController(new Stage(), stage, model, cart, currentUser); 
                loader.setController(cartController);

                Pane root = loader.load();
                cartController.showStage(root);
                stage.hide();
            } catch (Exception e) {
                System.out.println("Error loading CartView: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // Checkout button action
        checkoutBtn.setOnAction(event -> {
            if (cart.isEmpty()) {
                // Show alert if the cart is empty
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Empty Cart");
                alert.setHeaderText(null);
                alert.setContentText("Your cart is empty. Please add books to the cart before proceeding to checkout.");
                alert.showAndWait();
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CheckoutView.fxml"));
                    //CheckoutController with the necessary data
                    CheckoutController checkoutController = new CheckoutController(new Stage(), stage, cart, calculateTotalPrice(), currentUser, model);
                    loader.setController(checkoutController);

                    Pane root = loader.load();
                    checkoutController.showStage(root);
                    stage.hide();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        // Update Profile MenuItem action
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
        
        viewOrders.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderListView.fxml"));
                
                // Create the controller and set it before loading the FXML
                OrderListController orderListController = new OrderListController(new Stage(), currentUser);
                loader.setController(orderListController);
                
                Pane root = loader.load();
                
                // Now we can call methods on the controller
                orderListController.showStage(root);
            } catch (Exception e) {
                System.err.println("Error loading OrderListView: " + e.getMessage());
                e.printStackTrace();
            }
        });


        // Quit button action
        quitBtn.setOnAction(event -> {
            WindowManager.closeAllWindows();
            stage.close();
        });
    }

    private double calculateTotalPrice() {
        return cart.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
    }



	public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Home");
        WindowManager.addWindow(stage);
        stage.show();
    }
}