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
import model.User;
import dao.BookDao;

import java.util.Map;

public class CartController {

    @FXML
    private TableView<Book> cartTable;
    @FXML
    private TableColumn<Book, String> titleCol;
    @FXML
    private TableColumn<Book, String> authorCol;
    @FXML
    private TableColumn<Book, Void> quantityCol;
    @FXML
    private TableColumn<Book, Double> priceCol;
    @FXML
    private TableColumn<Book, Void> actionCol;
    @FXML
    private Button goBackBtn;
    @FXML
    private Button listBooksBtn;
    @FXML
private Button addToCartBtn;
    @FXML
    private Button checkoutBtn;
    @FXML
    private Button homeBtn;
    @FXML
    private Label totalPriceLabel;

    private Stage stage;
    private Stage parentStage;
    private Model model;
    private Map<Book, Integer> cart;
    private User currentUser;

    public CartController(Stage stage, Stage parentStage, Model model, Map<Book, Integer> cart, User currentUser) {
        this.stage = stage;
        this.parentStage = parentStage;
        this.model = model;
        this.cart = cart;
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        ObservableList<Book> cartItems = FXCollections.observableArrayList(cart.keySet());
        cartTable.setItems(cartItems);

        addDeleteButtonToTable();
        addQuantityButtonsToTable();

        double totalPrice = calculateTotalPrice();
        totalPriceLabel.setText(totalPrice + " AUD");

        goBackBtn.setOnAction(event -> {
            parentStage.show();
            stage.close();
        });

        listBooksBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookListView.fxml"));
                BookListController bookListController = new BookListController(new Stage(), stage, model, cart, currentUser);
                loader.setController(bookListController);
                Pane root = loader.load();
                bookListController.showStage(root);
                stage.hide();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        addToCartBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SearchBookView.fxml"));
                SearchBookController searchBookController = new SearchBookController(new Stage(), stage, model, cart, currentUser);
                loader.setController(searchBookController);
                Pane root = loader.load();
                searchBookController.showStage(root);
                stage.hide();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        checkoutBtn.setOnAction(event -> {
            boolean hasSufficientStock = true;
            for (Book book : cart.keySet()) {
                int cartQuantity = cart.get(book);
                if (book.getStock() < cartQuantity) {
                    hasSufficientStock = false;
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Insufficient Stock");
                    alert.setHeaderText(null);
                    alert.setContentText("There are only " + book.getStock() + " copies of " + book.getTitle() + " available.");
                    alert.showAndWait();
                    break;
                }
            }

            if (hasSufficientStock) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CheckoutView.fxml"));
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



        homeBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
                HomeController homeController = new HomeController(stage, this.model, this.cart, currentUser);
                loader.setController(homeController);
                Pane root = loader.load();
                homeController.showStage(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private double calculateTotalPrice() {
        double total = 0.0;
        for (Map.Entry<Book, Integer> entry : cart.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    private void addDeleteButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    int quantityInCart = cart.get(book);
                    book.setStock(book.getStock() + quantityInCart);
                    BookDao.updateBookStock(book.getId(), book.getStock());
                    cart.remove(book);
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

    private void addQuantityButtonsToTable() {
        quantityCol.setCellFactory(param -> new TableCell<>() {
            private final Button increaseButton = new Button("+");
            private final Button decreaseButton = new Button("-");
            private final Label quantityLabel = new Label();
            private final HBox quantityBox = new HBox(5, decreaseButton, quantityLabel, increaseButton);

            {
                quantityBox.setStyle("-fx-alignment: center;");

                increaseButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    int currentQuantity = cart.get(book);
                    int realTimeStock = BookDao.getAllBooks().stream()
                            .filter(b -> b.getId() == book.getId())
                            .findFirst().map(Book::getStock).orElse(0);

                    if (realTimeStock > 0 && currentQuantity < realTimeStock) {
                        cart.put(book, currentQuantity + 1);
                        book.setStock(book.getStock() - 1);
                        BookDao.updateBookStock(book.getId(), book.getStock());
                        updateQuantityLabel(book);
                        cartTable.refresh();
                        totalPriceLabel.setText(calculateTotalPrice() + " AUD");
                    } else {
                        showAlert("Stock Warning", "No more stock available for " + book.getTitle());
                    }
                });

                decreaseButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    int currentQuantity = cart.get(book);
                    if (currentQuantity > 1) {
                        cart.put(book, currentQuantity - 1);
                        book.setStock(book.getStock() + 1);
                        BookDao.updateBookStock(book.getId(), book.getStock());
                        updateQuantityLabel(book);
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
                    updateQuantityLabel(book);
                    setGraphic(quantityBox);
                }
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void clearCart() {
        cart.clear();
        cartTable.getItems().clear();
        cartTable.refresh();
        totalPriceLabel.setText("0.00 AUD");
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Your Cart");
        stage.show();
    }
}
