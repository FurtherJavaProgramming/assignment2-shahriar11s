package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;
import dao.BookDao;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SearchBookController {
    @FXML
    private TextField searchField; // Search input field
    @FXML
    private Button searchButton; // Button for searching
    @FXML
    private TableView<Book> bookTable; // Table to show the search results
    @FXML
    private TableColumn<Book, Integer> idCol;
    @FXML
    private TableColumn<Book, String> titleCol;
    @FXML
    private TableColumn<Book, String> authorCol;
    @FXML
    private TableColumn<Book, Double> priceCol;
    @FXML
    private TableColumn<Book, Integer> stockCol;
    @FXML
    private Button addToCartBtn; // Button to add to cart
    @FXML
    private Button goBackBtn; // Button to go back
    @FXML
    private Button listBooksBtn; // Button to list all books
    @FXML
    private Button viewCartBtn; // Button to view cart
    @FXML
    private Label messageLabel; // Label to show messages

    private Stage stage;
    private Stage parentStage;
    private static Map<Book, Integer> cart = new HashMap<>(); // Cart to store books and quantities

    public SearchBookController(Stage stage, Stage parentStage) {
        this.stage = stage;
        this.parentStage = parentStage;
    }

    @FXML
    public void initialize() {
        // Set up table columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Search button action
        searchButton.setOnAction(event -> {
            String keyword = searchField.getText().toLowerCase();
            if (!keyword.isEmpty()) {
                ObservableList<Book> filteredBooks = FXCollections.observableArrayList();
                for (Book book : BookDao.getAllBooks()) {
                    if (book.getTitle().toLowerCase().contains(keyword) || book.getAuthor().toLowerCase().contains(keyword)) {
                        filteredBooks.add(book);
                    }
                }
                bookTable.setItems(filteredBooks);

                // Show a message if no books were found
                if (filteredBooks.isEmpty()) {
                    messageLabel.setText("No books found with the name '" + keyword + "'");
                } else {
                    messageLabel.setText("");  // Clear the message if books are found
                }
            }
        });

        // Add to cart button action with custom quantity dialog
        addToCartBtn.setOnAction(event -> {
            Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                // Create a custom dialog for quantity input
                Dialog<Integer> dialog = new Dialog<>();
                dialog.setTitle("Enter Quantity");
                dialog.setGraphic(null);  // Remove the question mark icon
                
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setAlignment(Pos.CENTER);

                Label bookTitle = new Label("Add " + selectedBook.getTitle() + " to Cart");
                bookTitle.setStyle("-fx-font-weight: bold;");
                grid.add(bookTitle, 0, 0, 2, 1);  // span 2 columns

                // Create quantity input with increase/decrease buttons
                TextField quantityField = new TextField("1");
                Button increaseBtn = new Button("+");
                Button decreaseBtn = new Button("-");

                HBox quantityBox = new HBox(5, decreaseBtn, quantityField, increaseBtn);
                quantityBox.setAlignment(Pos.CENTER);
                grid.add(quantityBox, 0, 1, 2, 1);  // span 2 columns

                // Increment and decrement buttons logic
                increaseBtn.setOnAction(e -> {
                    int currentQuantity = Integer.parseInt(quantityField.getText());
                    quantityField.setText(String.valueOf(currentQuantity + 1));
                });

                decreaseBtn.setOnAction(e -> {
                    int currentQuantity = Integer.parseInt(quantityField.getText());
                    if (currentQuantity > 1) {
                        quantityField.setText(String.valueOf(currentQuantity - 1));
                    }
                });

                dialog.getDialogPane().setContent(grid);
                ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == okButtonType) {
                        try {
                            return Integer.parseInt(quantityField.getText());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input: Please enter a valid number.");
                        }
                    }
                    return null;
                });

                Optional<Integer> result = dialog.showAndWait();
                result.ifPresent(quantity -> {
                    if (quantity > 0) {
                        // Check if enough stock is available
                        if (selectedBook.getStock() >= quantity) {
                            // Reduce the stock of the book
                            selectedBook.setStock(selectedBook.getStock() - quantity);
                            bookTable.refresh(); // Refresh the table to show updated stock

                            // Add book and quantity to the cart
                            cart.put(selectedBook, cart.getOrDefault(selectedBook, 0) + quantity);

                            // Show success message
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText(quantity + " copies of " + selectedBook.getTitle() + " have been added to your cart.");
                            alert.showAndWait();
                        } else {
                            // Not enough stock
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Insufficient Stock");
                            alert.setHeaderText(null);
                            alert.setContentText("There are only " + selectedBook.getStock() + " copies of " + selectedBook.getTitle() + " available.");
                            alert.showAndWait();
                        }
                    } else {
                        System.out.println("Invalid quantity entered.");
                    }
                });
            } else {
                System.out.println("No book selected.");
            }
        });

        // Navigation buttons
        goBackBtn.setOnAction(event -> {
            parentStage.show();
            stage.close();
        });

        listBooksBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookListView.fxml"));
                BookListController bookListController = new BookListController(new Stage(), stage);  // Pass both stages
                loader.setController(bookListController);

                Pane root = loader.load();
                bookListController.showStage(root);
                stage.hide();  // Hide current stage
            } catch (Exception e) {
                System.out.println("Error loading BookListView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        viewCartBtn.setOnAction(event -> {
            // Call method to show cart view
            showCartView();
        });
    }

    private void showCartView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CartView.fxml"));
            CartController cartController = new CartController(new Stage(), stage, cart);  // Pass cart details
            loader.setController(cartController);

            Pane root = loader.load();
            cartController.showStage(root);
            stage.hide();  // Hide current stage
        } catch (Exception e) {
            System.out.println("Error loading CartView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 450);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Add Books to Cart");
        stage.show();
    }
}
