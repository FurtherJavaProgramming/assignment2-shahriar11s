package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;
import model.Model;
import dao.BookDao;

import java.util.Map;

public class CartController {

    @FXML
    private TableView<Book> cartTable;  // Table to show cart items
    @FXML
    private TableColumn<Book, String> titleCol;
    @FXML
    private TableColumn<Book, String> authorCol;
    @FXML
    private TableColumn<Book, Void> quantityCol;  // Updated to handle buttons and quantity display
    @FXML
    private TableColumn<Book, Double> priceCol;
    @FXML
    private TableColumn<Book, Void> actionCol; // Column for delete action
    @FXML
    private Button goBackBtn;
    @FXML
    private Button listBooksBtn;
    @FXML
    private Button addToCartBtn;
    @FXML
    private Button checkoutBtn;
    @FXML
    private Button homeBtn;  // Home button
    @FXML
    private Label totalPriceLabel;

    private Stage stage;
    private Stage parentStage;
    private Model model;
    private Map<Book, Integer> cart;  // Cart to store books and quantities

    public CartController(Stage stage, Stage parentStage, Model model, Map<Book, Integer> cart) {
        this.stage = stage;
        this.parentStage = parentStage;
        this.cart = cart;
    }

    @FXML
    public void initialize() {
        // Set up table columns
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Populate the cart table
        ObservableList<Book> cartItems = FXCollections.observableArrayList(cart.keySet());
        cartTable.setItems(cartItems);

        // Add action columns
        addDeleteButtonToTable();
        addQuantityButtonsToTable();  // Add quantity buttons with quantity display

        // Calculate and display total price
        double totalPrice = calculateTotalPrice();
        totalPriceLabel.setText(totalPrice + " AUD");

        // Navigation buttons
        goBackBtn.setOnAction(event -> {
            parentStage.show();
            stage.close();
        });

        listBooksBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookListView.fxml"));
                BookListController bookListController = new BookListController(new Stage(), stage, model, cart); // Add cart parameter here
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
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SearchBookView.fxml"));
                SearchBookController searchBookController = new SearchBookController(new Stage(), stage, model, cart); // Add cart parameter here
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

        homeBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
                HomeController homeController = new HomeController(stage, this.model, this.cart);
                loader.setController(homeController);
                
                Pane root = loader.load();
                homeController.showStage(root);
            } catch (Exception e) {
                System.out.println("Error loading HomeView: " + e.getMessage());
                e.printStackTrace();
            }
        });


    }

    // Calculate total price of cart items
    private double calculateTotalPrice() {
        double total = 0.0;
        for (Map.Entry<Book, Integer> entry : cart.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    // Add a delete button to each row of the cart table
    private void addDeleteButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");  // Red button style
                deleteButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());

                    // Increase the stock of the book when removed from the cart
                    int quantityInCart = cart.get(book);
                    book.setStock(book.getStock() + quantityInCart);
                    BookDao.updateBookStock(book.getId(), book.getStock());

                    // Remove the book from the cart
                    cart.remove(book);

                    // Refresh the table and update the total price
                    cartTable.getItems().remove(book);
                    totalPriceLabel.setText(calculateTotalPrice() + " AUD");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }

    // Add increase/decrease buttons with quantity display
    private void addQuantityButtonsToTable() {
        quantityCol.setCellFactory(param -> new TableCell<>() {
            private final Button increaseButton = new Button("+");
            private final Button decreaseButton = new Button("-");
            private final Label quantityLabel = new Label();

            private final HBox quantityBox = new HBox(5, decreaseButton, quantityLabel, increaseButton);

            {
                quantityBox.setStyle("-fx-alignment: center;");  // Center the HBox

                increaseButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    int currentQuantity = cart.get(book);

                    // Check if stock is available for the increase
                    if (book.getStock() > 0) {
                        cart.put(book, currentQuantity + 1);
                        book.setStock(book.getStock() - 1);  // Decrease stock
                        BookDao.updateBookStock(book.getId(), book.getStock());

                        updateQuantityLabel(book);  // Update quantity label
                        cartTable.refresh();
                        totalPriceLabel.setText(calculateTotalPrice() + " AUD");
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Stock Warning");
                        alert.setHeaderText(null);
                        alert.setContentText("No more stock available for " + book.getTitle());
                        alert.showAndWait();
                    }
                });

                decreaseButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    int currentQuantity = cart.get(book);

                    // Ensure the quantity doesn't go below 1
                    if (currentQuantity > 1) {
                        cart.put(book, currentQuantity - 1);
                        book.setStock(book.getStock() + 1);  // Increase stock
                        BookDao.updateBookStock(book.getId(), book.getStock());

                        updateQuantityLabel(book);  // Update quantity label
                        cartTable.refresh();
                        totalPriceLabel.setText(calculateTotalPrice() + " AUD");
                    }
                });
            }

            private void updateQuantityLabel(Book book) {
                quantityLabel.setText(String.valueOf(cart.get(book)));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Book book = getTableView().getItems().get(getIndex());
                    updateQuantityLabel(book);  // Set the initial quantity value
                    setGraphic(quantityBox);
                }
            }
        });
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Your Cart");
        stage.show();
    }
}
