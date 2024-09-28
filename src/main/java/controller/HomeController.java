package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;
import model.Model;
import dao.BookDao;

public class HomeController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private MenuItem viewProfile; 
    @FXML
    private MenuItem updateProfile;

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

    public HomeController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        soldCol.setCellValueFactory(new PropertyValueFactory<>("sold"));

        ObservableList<Book> bookList = FXCollections.observableArrayList(BookDao.getAllBooks());
        bookTable.setItems(bookList);
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 450);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Home");
        stage.show();
    }
}
