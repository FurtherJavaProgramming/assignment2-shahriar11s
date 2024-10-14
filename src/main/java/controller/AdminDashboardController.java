package controller;

import java.io.IOException;
import java.util.List;

import dao.BookDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;
import model.Model;
import model.User;
import util.WindowManager;

public class AdminDashboardController {

    @FXML
    private Label adminWelcomeLabel;
    @FXML
    private Button manageBooksBtn;
    @FXML
    private Button addNewBookBtn;
    @FXML
    private Button viewUsersBtn;
    @FXML
    private Button quitBtn;
    @FXML
    private TableView<Book> topBooksTable;
    @FXML
    private TableColumn<Book, String> titleCol;
    @FXML
    private TableColumn<Book, String> authorCol;
    @FXML
    private TableColumn<Book, Integer> soldCol;

    private Stage stage;
    private Model model;
    private User currentUser;

    public AdminDashboardController(Stage stage, Model model, User currentUser) {
        this.stage = stage;
        this.model = model;
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        // Set welcome message
        if (currentUser != null) {
            String firstName = currentUser.getFirstName();
            adminWelcomeLabel.setText("Welcome, Admin " + firstName + "!");
        } else {
            adminWelcomeLabel.setText("Welcome, Admin!");
        }

        // Bind table columns
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        soldCol.setCellValueFactory(new PropertyValueFactory<>("sold"));

        // Load books into the table
        loadTopSellingBooks();

        // Add button handlers
        manageBooksBtn.setOnAction(e -> handleManageBooksAction());
        addNewBookBtn.setOnAction(e -> handleAddNewBookAction());
        viewUsersBtn.setOnAction(e -> handleViewUsersAction());
        quitBtn.setOnAction(e -> handleQuitAction());
    }

    // Load top-selling books into the table
    private void loadTopSellingBooks() {
        List<Book> topBooks = BookDao.getTopBooks(5);
        ObservableList<Book> topBooksList = FXCollections.observableArrayList(topBooks);
        topBooksTable.setItems(topBooksList);
    }

    // Handle the Manage Books button click
    private void handleManageBooksAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminBooksView.fxml"));
            AdminBooksController adminBooksController = new AdminBooksController(stage, model);
            loader.setController(adminBooksController);
            Pane root = loader.load();
            adminBooksController.showStage(root);
            stage.hide(); // Hide the current stage
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handle the Add New Book button click
    private void handleAddNewBookAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddNewBookView.fxml"));
            AddNewBookController addNewBookController = new AddNewBookController(new Stage(), model);
            loader.setController(addNewBookController);
            Pane root = loader.load();
            addNewBookController.showStage(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Handle the View Users button click (Placeholder functionality)
    private void handleViewUsersAction() {
        System.out.println("View Users button clicked! Functionality to be implemented.");
    }

    // Handle the Quit button
    private void handleQuitAction() {
        WindowManager.closeAllWindows();
        stage.close();
    }

    // Reload dashboard when returning
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Admin Dashboard");
        WindowManager.addWindow(stage);
        stage.show();
    }
}