package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Book;
import model.Model;
import model.User;
import util.WindowManager;
import dao.BookDao;

import java.util.Map;

public class BookListController {
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
    private TableColumn<Book, Integer> soldCol;

    @FXML
    private Button goBackBtn;
    @FXML
    private Button homeBtn;
    @FXML
    private Button addToCartBtn;
    @FXML
    private Button viewCartBtn;

    private Stage stage;
    private Stage parentStage;
    private Model model;
    private Map<Book, Integer> cart;
    private User currentUser;  // Add currentUser


 // Constructor update
    public BookListController(Stage stage, Stage parentStage, Model model, Map<Book, Integer> cart, User currentUser) {
        this.stage = stage;
        this.parentStage = parentStage;
        this.model = model;
        this.cart = cart;
        this.currentUser = currentUser;  // Assign currentUser
    }


    @FXML
    public void initialize() {
        // Set up table columns
    	idCol.setVisible(false);
    	titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        soldCol.setCellValueFactory(new PropertyValueFactory<>("sold"));

        // Center-align Price, Physical Copies, and Sold Copies columns
        priceCol.setStyle("-fx-alignment: CENTER;");
        stockCol.setStyle("-fx-alignment: CENTER;");
        soldCol.setStyle("-fx-alignment: CENTER;");

        // Custom cell factories for center alignment and formatting
        priceCol.setCellFactory(column -> new javafx.scene.control.TableCell<Book, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", price));
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        stockCol.setCellFactory(column -> new javafx.scene.control.TableCell<Book, Integer>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                } else {
                    setText(stock.toString());
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        soldCol.setCellFactory(column -> new javafx.scene.control.TableCell<Book, Integer>() {
            @Override
            protected void updateItem(Integer sold, boolean empty) {
                super.updateItem(sold, empty);
                if (empty || sold == null) {
                    setText(null);
                } else {
                    setText(sold.toString());
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        // Fetch the books and display them in the table
        ObservableList<Book> bookList = FXCollections.observableArrayList(BookDao.getAllBooks());
        bookTable.setItems(bookList);

        // Create a timeline to refresh every second
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            ObservableList<Book> updatedBooks = FXCollections.observableArrayList(BookDao.getAllBooks());
            bookTable.setItems(updatedBooks);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Button actions
        goBackBtn.setOnAction(event -> {
            parentStage.show();
            stage.close();
        });

        homeBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
                HomeController homeController = new HomeController(stage, this.model, this.cart, currentUser);
                loader.setController(homeController);
                
                Pane root = loader.load();
                homeController.showStage(root);
            } catch (Exception e) {
                System.out.println("Error loading HomeView: " + e.getMessage());
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
                System.out.println("Error loading SearchBookView: " + e.getMessage());
                e.printStackTrace();
            }
        });

        viewCartBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CartView.fxml"));
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
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("List of Books");
        WindowManager.addWindow(stage);
        stage.setOnCloseRequest(event -> WindowManager.removeWindow(stage));
        stage.show();
    }
}