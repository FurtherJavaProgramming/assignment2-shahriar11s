package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
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

    private Stage stage;

    public BookListController(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        // Set up the table columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        soldCol.setCellValueFactory(new PropertyValueFactory<>("sold"));

        // Fetch the books and display them in the table
        ObservableList<Book> bookList = FXCollections.observableArrayList(BookDao.getAllBooks());
        bookTable.setItems(bookList);
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 450);  // Keep the size consistent
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("List of All Books");
        stage.show();
    }

}
