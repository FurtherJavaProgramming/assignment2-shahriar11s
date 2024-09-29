package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;

import java.util.Map;

public class CartController {

    @FXML
    private TableView<Book> cartTable;  // Table to show cart items
    @FXML
    private TableColumn<Book, String> titleCol;
    @FXML
    private TableColumn<Book, Integer> quantityCol;
    @FXML
    private TableColumn<Book, Double> priceCol;
    @FXML
    private Button goBackBtn;
    @FXML
    private Button listBooksBtn;
    @FXML
    private Button addToCartBtn;
    @FXML
    private Button checkoutBtn;
    @FXML
    private Label totalPriceLabel;

    private Stage stage;
    private Stage parentStage;
    private Map<Book, Integer> cart;  // Cart to store books and quantities

    public CartController(Stage stage, Stage parentStage, Map<Book, Integer> cart) {
        this.stage = stage;
        this.parentStage = parentStage;
        this.cart = cart;
    }

    @FXML
    public void initialize() {
        // Set up table columns
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        quantityCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>(cart.get(book));
        });
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Populate the cart table
        ObservableList<Book> cartItems = FXCollections.observableArrayList(cart.keySet());
        cartTable.setItems(cartItems);

        // Calculate and display total price
        double totalPrice = calculateTotalPrice();
        totalPriceLabel.setText(totalPrice + " AUD");

        // Navigation buttons
        goBackBtn.setOnAction(event -> {
            parentStage.show();
            stage.close();
        });

        listBooksBtn.setOnAction(event -> {
            // Logic to go back to the list of books
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookListView.fxml"));
                BookListController bookListController = new BookListController(new Stage(), stage, cart); // Add cart parameter here
                loader.setController(bookListController);

                Pane root = loader.load();
                bookListController.showStage(root);
                stage.hide();
            } catch (Exception e) {
                System.out.println("Error loading BookListView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        addToCartBtn.setOnAction(event -> {
            // Logic to go back to Add to Cart page
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SearchBookView.fxml"));
                SearchBookController searchBookController = new SearchBookController(new Stage(), stage, cart); // Add cart parameter here
                loader.setController(searchBookController);

                Pane root = loader.load();
                searchBookController.showStage(root);
                stage.hide();
            } catch (Exception e) {
                System.out.println("Error loading SearchBookView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        checkoutBtn.setOnAction(event -> {
            // Placeholder for checkout functionality
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Checkout");
            alert.setHeaderText(null);
            alert.setContentText("Proceeding to checkout!");
            alert.showAndWait();
        });
    }

    private double calculateTotalPrice() {
        double total = 0.0;
        for (Map.Entry<Book, Integer> entry : cart.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 450);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Your Cart");
        stage.show();
    }
}
