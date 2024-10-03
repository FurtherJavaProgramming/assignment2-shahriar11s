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
import model.Model;
import model.User;
import dao.BookDao;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.Map;
import java.util.Optional;

public class SearchBookController {
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private TableView<Book> bookTable;
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
    private Button addToCartBtn;
    @FXML
    private Button goBackBtn;
    @FXML
    private Button homeBtn;
    @FXML
    private Button listBooksBtn;
    @FXML
    private Button viewCartBtn;
    @FXML
    private Label messageLabel;

    private Stage stage;
    private Stage parentStage;
    private Model model;
    private Map<Book, Integer> cart;
    private User currentUser; 
// Cart to store books and quantities

    private Book selectedBook; 

 // Constructor update
    public SearchBookController(Stage stage, Stage parentStage, Model model, Map<Book, Integer> cart, User currentUser) {
        this.stage = stage;
        this.parentStage = parentStage;
        this.model = model;
        this.cart = cart;
        this.currentUser = currentUser;
    }


    @FXML
    public void initialize() {
        //table
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        //all books
        ObservableList<Book> allBooks = FXCollections.observableArrayList(BookDao.getAllBooks());
        bookTable.setItems(allBooks);

        // Handle selection and re-selection on refresh
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedBook = newSelection;
        });

        // ScheduledService to refresh the stock data every second
        ScheduledService<Void> refreshService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        // Preserve the selected book during refresh
                        if (selectedBook != null) {
                            int selectedIndex = bookTable.getSelectionModel().getSelectedIndex();
                            ObservableList<Book> updatedBooks = FXCollections.observableArrayList(BookDao.getAllBooks());
                            bookTable.setItems(updatedBooks);
                            bookTable.getSelectionModel().select(selectedIndex);
                        } else {
                            bookTable.setItems(FXCollections.observableArrayList(BookDao.getAllBooks()));
                        }
                        return null;
                    }
                };
            }
        };
        refreshService.setPeriod(Duration.seconds(1));
        refreshService.start();

        // Search button action
        searchButton.setOnAction(event -> {
            String keyword = searchField.getText().toLowerCase();
            if (!keyword.isEmpty()) {
                ObservableList<Book> filteredBooks = FXCollections.observableArrayList(BookDao.searchBooks(keyword));
                bookTable.setItems(filteredBooks);

                // Show a message if no books were found
                if (filteredBooks.isEmpty()) {
                    messageLabel.setText("No books found with the name '" + keyword + "'");
                } else {
                    messageLabel.setText("");  
                }
            }
        });

        // Add to cart button action
        addToCartBtn.setOnAction(event -> {
            if (selectedBook != null) {
                // Custom dialog for quantity input
                Dialog<Integer> dialog = new Dialog<>();
                dialog.setTitle("Enter Quantity");
                dialog.setGraphic(null); 

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setAlignment(Pos.CENTER);

                Label bookTitle = new Label("Add " + selectedBook.getTitle() + " to Cart");
                bookTitle.setStyle("-fx-font-weight: bold;");
                grid.add(bookTitle, 0, 0, 2, 1);

                //quantity input with increase/decrease buttons
                TextField quantityField = new TextField("1");
                Button increaseBtn = new Button("+");
                Button decreaseBtn = new Button("-");

                HBox quantityBox = new HBox(5, decreaseBtn, quantityField, increaseBtn);
                quantityBox.setAlignment(Pos.CENTER);
                grid.add(quantityBox, 0, 1, 2, 1); 

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
                            // Reduce the stock of the book and update in the database
                            selectedBook.setStock(selectedBook.getStock() - quantity);
                            BookDao.updateBookStock(selectedBook.getId(), selectedBook.getStock());
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

        homeBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
                
                // Pass currentUser along with stage, model, and cart
                HomeController homeController = new HomeController(stage, this.model, this.cart, model.getCurrentUser()); // Add model.getCurrentUser()

                loader.setController(homeController);

                Pane root = loader.load();
                homeController.showStage(root);
            } catch (Exception e) {
                System.out.println("Error loading HomeView: " + e.getMessage());
                e.printStackTrace();
            }
        });


        listBooksBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookListView.fxml"));
                BookListController bookListController = new BookListController(new Stage(), stage, model, cart, currentUser);  // Pass both stages and cart
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
            showCartView();
        });
    }

    private void showCartView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CartView.fxml"));
            
            // Update the constructor call to include currentUser
            CartController cartController = new CartController(new Stage(), stage, model, cart, model.getCurrentUser());  // Pass cart and currentUser
            
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
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Add Books to Cart");
        stage.show();
    }
}
