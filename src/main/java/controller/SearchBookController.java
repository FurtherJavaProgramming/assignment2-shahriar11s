package controller;

import javafx.application.Platform;
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
import util.WindowManager;
import dao.BookDao;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.concurrent.Task;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
    private Book selectedBook;

    public SearchBookController(Stage stage, Stage parentStage, Model model, Map<Book, Integer> cart, User currentUser) {
        this.stage = stage;
        this.parentStage = parentStage;
        this.model = model;
        this.cart = cart;
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedBook = newSelection;
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshBookList()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        searchButton.setOnAction(event -> performSearch());
        addToCartBtn.setOnAction(event -> addToCart());
        goBackBtn.setOnAction(event -> goBack());
        homeBtn.setOnAction(event -> goToHome());
        listBooksBtn.setOnAction(event -> showBookList());
        viewCartBtn.setOnAction(event -> showCartView());

        refreshBookList();
    }

    private void refreshBookList() {
        Task<ObservableList<Book>> task = new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                return FXCollections.observableArrayList(BookDao.getAllBooks());
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                ObservableList<Book> updatedBooks = task.getValue();
                int selectedIndex = bookTable.getSelectionModel().getSelectedIndex();
                bookTable.setItems(updatedBooks);
                if (selectedIndex >= 0 && selectedIndex < updatedBooks.size()) {
                    bookTable.getSelectionModel().select(selectedIndex);
                    selectedBook = bookTable.getItems().get(selectedIndex);
                } else if (selectedBook != null) {
                    int newIndex = updatedBooks.indexOf(selectedBook);
                    if (newIndex >= 0) {
                        bookTable.getSelectionModel().select(newIndex);
                    } else {
                        selectedBook = null;
                    }
                }
                bookTable.refresh(); // Ensure the table is refreshed to show updated stock
            });
        });

        new Thread(task).start();
    }

    private void performSearch() {
        String keyword = searchField.getText().toLowerCase();
        if (!keyword.isEmpty()) {
            Task<ObservableList<Book>> searchTask = new Task<>() {
                @Override
                protected ObservableList<Book> call() throws Exception {
                    return FXCollections.observableArrayList(BookDao.searchBooks(keyword));
                }
            };

            searchTask.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    ObservableList<Book> searchResults = searchTask.getValue();
                    bookTable.setItems(searchResults);
                    if (searchResults.isEmpty()) {
                        messageLabel.setText("No books found with the name '" + keyword + "'");
                    } else {
                        messageLabel.setText("");
                    }
                });
            });

            new Thread(searchTask).start();
        }
    }

    private void addToCart() {
        if (selectedBook != null) {
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Enter Quantity");
            dialog.setHeaderText("Add " + selectedBook.getTitle() + " to Cart");

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            TextField quantityField = new TextField("1");
            Button increaseBtn = new Button("+");
            Button decreaseBtn = new Button("-");

            increaseBtn.setOnAction(e -> incrementQuantity(quantityField));
            decreaseBtn.setOnAction(e -> decrementQuantity(quantityField));

            HBox quantityBox = new HBox(5, decreaseBtn, quantityField, increaseBtn);
            quantityBox.setAlignment(Pos.CENTER);

            grid.add(new Label("Quantity:"), 0, 0);
            grid.add(quantityBox, 1, 0);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    try {
                        return Integer.parseInt(quantityField.getText());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            });

            Optional<Integer> result = dialog.showAndWait();
            result.ifPresent(quantity -> {
                if (quantity > 0 && quantity <= selectedBook.getStock()) {
                    cart.put(selectedBook, cart.getOrDefault(selectedBook, 0) + quantity);
                    selectedBook.setStock(selectedBook.getStock() - quantity);
                    BookDao.updateTempStock(selectedBook.getId(), selectedBook.getStock());
                    showAlert(Alert.AlertType.INFORMATION, "Success", quantity + " copies of " + selectedBook.getTitle() + " have been added to your cart.");
                    refreshBookList();
                } else if (quantity > selectedBook.getStock()) {
                    showAlert(Alert.AlertType.ERROR, "Insufficient Stock", "There are only " + selectedBook.getStock() + " copies of " + selectedBook.getTitle() + " available.");
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book to add to the cart.");
        }
    }

    private void incrementQuantity(TextField quantityField) {
        int quantity = Integer.parseInt(quantityField.getText());
        quantityField.setText(String.valueOf(quantity + 1));
    }

    private void decrementQuantity(TextField quantityField) {
        int quantity = Integer.parseInt(quantityField.getText());
        if (quantity > 1) {
            quantityField.setText(String.valueOf(quantity - 1));
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void goBack() {
        parentStage.show();
        stage.close();
    }

    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
            HomeController homeController = new HomeController(stage, this.model, this.cart, currentUser);
            loader.setController(homeController);
            Pane root = loader.load();
            homeController.showStage(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error loading HomeView: " + e.getMessage());
        }
    }

    private void showBookList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookListView.fxml"));
            BookListController bookListController = new BookListController(new Stage(), stage, model, cart, currentUser);
            loader.setController(bookListController);
            Pane root = loader.load();
            bookListController.showStage(root);
            stage.hide();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error loading BookListView: " + e.getMessage());
        }
    }

    private void showCartView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CartView.fxml"));
            CartController cartController = new CartController(new Stage(), stage, model, cart, currentUser);
            loader.setController(cartController);
            Pane root = loader.load();
            cartController.showStage(root);
            stage.hide();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error loading CartView: " + e.getMessage());
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Purchase Books");
        WindowManager.addWindow(stage);
        stage.setOnCloseRequest(event -> WindowManager.removeWindow(stage));
        stage.show();
    }
}