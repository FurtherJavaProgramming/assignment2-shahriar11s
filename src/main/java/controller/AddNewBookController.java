package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Book;
import model.Model;
import dao.BookDao;

public class AddNewBookController {

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private Button addBookBtn;
    @FXML private Button cancelBtn;

    private Stage stage;
    private Model model;

    public AddNewBookController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        addBookBtn.setOnAction(event -> addBook());
        cancelBtn.setOnAction(event -> stage.close());

        // Add listeners to validate input as user types
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                priceField.setText(oldValue);
            }
        });

        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockField.setText(oldValue);
            }
        });
    }

    private void addBook() {
        if (isValidInput()) {
            int newBookId = BookDao.getHighestBookId() + 1;
            Book newBook = new Book(newBookId, 
                                    titleField.getText().trim(), 
                                    authorField.getText().trim(),
                                    Double.parseDouble(priceField.getText()), 
                                    Integer.parseInt(stockField.getText()), 
                                    0);

            BookDao.addNewBook(newBook);
            BookDao.updateBookStock(newBook);

            showAlert("Success", "Book added successfully!", Alert.AlertType.INFORMATION);
            clearForm();
            stage.close();
        } else {
            showAlert("Invalid Input", "Please fill in all fields with valid data.", Alert.AlertType.ERROR);
        }
    }

    private boolean isValidInput() {
        return !titleField.getText().trim().isEmpty() &&
               !authorField.getText().trim().isEmpty() &&
               isValidPrice(priceField.getText()) &&
               isValidStock(stockField.getText());
    }

    private boolean isValidPrice(String price) {
        try {
            double value = Double.parseDouble(price);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidStock(String stock) {
        try {
            int value = Integer.parseInt(stock);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void clearForm() {
        titleField.clear();
        authorField.clear();
        priceField.clear();
        stockField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Add New Book");
        stage.show();
    }
}