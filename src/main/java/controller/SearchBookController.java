package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;
import dao.BookDao;

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

        // Add to cart button action
        addToCartBtn.setOnAction(event -> {
            Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                System.out.println("Added to cart: " + selectedBook.getTitle());
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
            // Logic for showing all books
        });

        viewCartBtn.setOnAction(event -> {
            // Logic for viewing the cart
        });
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 450);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Add Books to Cart");
        stage.show();
    }
}
