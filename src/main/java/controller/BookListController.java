package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;
import dao.BookDao;

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
    private Button goBackBtn;  // Change to Button
    @FXML
    private Button addToCartBtn;  // Change to Button
    @FXML
    private Button viewCartBtn;  // Change to Button

    private Stage stage;
    private Stage parentStage;

    public BookListController(Stage stage, Stage parentStage) {
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
        soldCol.setCellValueFactory(new PropertyValueFactory<>("sold"));

        // Fetch the books and display them in the table
        ObservableList<Book> bookList = FXCollections.observableArrayList(BookDao.getAllBooks());
        bookTable.setItems(bookList);

        // Set the height dynamically based on the number of items in the table
        bookTable.setFixedCellSize(25);
        bookTable.prefHeightProperty().bind(bookTable.fixedCellSizeProperty().multiply(bookTable.getItems().size() + 1.01));
        bookTable.minHeightProperty().bind(bookTable.prefHeightProperty());
        bookTable.maxHeightProperty().bind(bookTable.prefHeightProperty());

        // Button actions
        goBackBtn.setOnAction(event -> {
            parentStage.show();  // Show the parent (home) stage
            stage.close();  // Close the current book list page
            System.out.println("Navigated back to home.");
        });

        addToCartBtn.setOnAction(event -> {
            System.out.println("Add to Cart clicked! (Functionality to be added)");
        });

        viewCartBtn.setOnAction(event -> {
            System.out.println("View Cart clicked! (Functionality to be added)");
        });
    }


    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 450);  // Keep the consistent window size
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("List of Books");
        stage.show();
    }
}
