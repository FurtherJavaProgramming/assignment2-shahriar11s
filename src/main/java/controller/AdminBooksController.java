package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import model.Book;
import model.Model;
import dao.BookDao;
import util.WindowManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminBooksController {

    @FXML
    private TableView<Book> booksTable;
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
    private TableColumn<Book, Void> actionCol;

    @FXML
    private Button goBackBtn;
    @FXML
    private Button addNewBookBtn;
    @FXML
    private Button updateBooksBtn;
    @FXML
    private Button cancelUpdateBtn;
    @FXML
    private Button confirmUpdateBtn;
    
    private ObservableList<Book> originalBooks;
    private ObservableList<Book> modifiedBooks;
    private Set<Integer> booksToDelete;

    private Stage stage;
    private Stage parentStage;
    private Model model;
    private Timeline refreshTimeline;
    private boolean isUpdating = false;

    public AdminBooksController(Stage parentStage, Model model) {
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        loadBooks();
        setupRefreshTimeline();

        updateBooksBtn.setOnAction(event -> handleUpdateBooks());
        cancelUpdateBtn.setOnAction(event -> handleCancelUpdate());
        confirmUpdateBtn.setOnAction(event -> handleConfirmUpdate());
        goBackBtn.setOnAction(event -> handleGoBack());
        addNewBookBtn.setOnAction(event -> handleAddNewBook());

        booksToDelete = new HashSet<>();

        // Set alignment for columns in normal view
        titleCol.setStyle("-fx-alignment: CENTER-LEFT;");
        authorCol.setStyle("-fx-alignment: CENTER-LEFT;");
        priceCol.setStyle("-fx-alignment: CENTER;");
        stockCol.setStyle("-fx-alignment: CENTER;");
        soldCol.setStyle("-fx-alignment: CENTER;");
    }

    private void setupRefreshTimeline() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (!isUpdating) {
                loadBooks();
            }
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void loadBooks() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        soldCol.setCellValueFactory(new PropertyValueFactory<>("sold"));

        // Custom cell factories for price and stock to ensure center alignment
        priceCol.setCellFactory(tc -> new TableCell<Book, Double>() {
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

        stockCol.setCellFactory(tc -> new TableCell<Book, Integer>() {
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

        List<Book> books = BookDao.getAdminBookList();
        originalBooks = FXCollections.observableArrayList(books);
        modifiedBooks = FXCollections.observableArrayList(books);
        booksTable.setItems(modifiedBooks);
    }

    private void handleUpdateBooks() {
        isUpdating = true;
        updateBooksBtn.setVisible(false);
        cancelUpdateBtn.setVisible(true);
        confirmUpdateBtn.setVisible(true);
        actionCol.setVisible(true);

        stockCol.setCellFactory(tc -> new TableCell<>() {
            final HBox hbox = new HBox(5);
            final Button decreaseBtn = new Button("-");
            final Label stockLabel = new Label();
            final Button increaseBtn = new Button("+");

            {
                hbox.setAlignment(javafx.geometry.Pos.CENTER);
                decreaseBtn.setStyle("-fx-min-width: 30px;");
                increaseBtn.setStyle("-fx-min-width: 30px;");
                stockLabel.setStyle("-fx-alignment: CENTER; -fx-min-width: 30px;");
                hbox.getChildren().addAll(decreaseBtn, stockLabel, increaseBtn);
            }

            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setGraphic(null);
                } else {
                    stockLabel.setText(stock.toString());
                    setGraphic(hbox);

                    increaseBtn.setOnAction(e -> {
                        Book book = getTableView().getItems().get(getIndex());
                        book.setStock(book.getStock() + 1);
                        stockLabel.setText(String.valueOf(book.getStock()));
                    });

                    decreaseBtn.setOnAction(e -> {
                        Book book = getTableView().getItems().get(getIndex());
                        if (book.getStock() > 0) {
                            book.setStock(book.getStock() - 1);
                            stockLabel.setText(String.valueOf(book.getStock()));
                        }
                    });
                }
            }
        });

        actionCol.setCellFactory(tc -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-min-width: 60px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(deleteButton);
                }

                deleteButton.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    modifiedBooks.remove(book);
                    booksToDelete.add(book.getId());
                    getTableView().refresh();
                });
            }
        });

        // Set alignment for other columns
        titleCol.setStyle("-fx-alignment: CENTER-LEFT;");
        authorCol.setStyle("-fx-alignment: CENTER-LEFT;");
        priceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        soldCol.setStyle("-fx-alignment: CENTER;");
    }

    private void handleCancelUpdate() {
        isUpdating = false;
        updateBooksBtn.setVisible(true);
        cancelUpdateBtn.setVisible(false);
        confirmUpdateBtn.setVisible(false);
        actionCol.setVisible(false);

        modifiedBooks.setAll(originalBooks);
        booksToDelete.clear();
        booksTable.setItems(modifiedBooks);
        booksTable.refresh();

        // Reset stock column to display-only
        stockCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                } else {
                    setText(stock.toString());
                }
            }
        });
    }

    private void handleConfirmUpdate() {
        for (Book book : modifiedBooks) {
            BookDao.updateBookStock(book);
        }
        for (Integer bookId : booksToDelete) {
            BookDao.deleteBook(bookId);
        }
        isUpdating = false;
        loadBooks();
        handleCancelUpdate();
    }

    private void handleGoBack() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        parentStage.show();
        stage.close();
    }

    private void handleAddNewBook() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddNewBookView.fxml"));
            AddNewBookController addNewBookController = new AddNewBookController(stage, model);
            loader.setController(addNewBookController);
            Pane root = loader.load();

            Stage addBookStage = new Stage();
            addNewBookController.setStage(addBookStage);
            addBookStage.setScene(new Scene(root, 400, 300));
            addBookStage.setResizable(false);
            addBookStage.setTitle("Add New Book");

            addBookStage.showAndWait();
            loadBooks();  // Reload books after adding a new book
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showStage(Pane root) {
        stage = new Stage();
        stage.setScene(new Scene(root, 870, 473));
        stage.setResizable(false);
        stage.setTitle("Manage Books");
        WindowManager.addWindow(stage);
        stage.show();

        stage.setOnCloseRequest(event -> {
            if (refreshTimeline != null) {
                refreshTimeline.stop();
            }
            WindowManager.removeWindow(stage);
            parentStage.show();
        });
    }
}