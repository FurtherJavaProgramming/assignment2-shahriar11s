package controller;

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
import model.Book;
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
    private Button addToCartBtn;
    @FXML
    private Button viewCartBtn;

    private Stage stage;
    private Stage parentStage;
    private Map<Book, Integer> cart;  // Cart

    public BookListController(Stage stage, Stage parentStage, Map<Book, Integer> cart) {  // Constructor with cart
        this.stage = stage;
        this.parentStage = parentStage;
        this.cart = cart;
    }

    @FXML
    public void initialize() {
        // Set up table columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        soldCol.setCellValueFactory(new PropertyValueFactory<>("sold"));

        // Fetch the books and display them in the table
        ObservableList<Book> bookList = FXCollections.observableArrayList(BookDao.getAllBooks());
        bookTable.setItems(bookList);

        // Button actions
        goBackBtn.setOnAction(event -> {
            parentStage.show();  // Show the parent (home) stage
            stage.close();  // Close the current book list page
        });

        addToCartBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SearchBookView.fxml"));
                SearchBookController searchBookController = new SearchBookController(new Stage(), stage, cart);  // Pass cart
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
                CartController cartController = new CartController(new Stage(), stage, cart);  // Pass cart
                loader.setController(cartController);

                Pane root = loader.load();
                cartController.showStage(root);
                stage.hide();  // Hide current stage
            } catch (Exception e) {
                System.out.println("Error loading CartView: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 450);  // Consistent window size
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("List of Books");
        stage.show();
    }
}
