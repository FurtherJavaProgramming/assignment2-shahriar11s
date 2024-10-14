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

    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;

    @FXML
    private Button addBookBtn;
    @FXML
    private Button cancelBtn;

    private Stage stage;  // To hold the new stage instance
    private Model model;

    public AddNewBookController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    public void setStage(Stage stage) {
        this.stage = stage;  // Setter to set the stage from outside
    }

    @FXML
    public void initialize() {
        // Handle the Add Book button action
    	addBookBtn.setOnAction(event -> {
            if (isValidInput()) {
                int newBookId = BookDao.getHighestBookId() + 1;
                Book newBook = new Book(newBookId, 
                                        titleField.getText(), 
                                        authorField.getText(),
                                        Double.parseDouble(priceField.getText()), 
                                        Integer.parseInt(stockField.getText()), 
                                        0);

                BookDao.addNewBook(newBook);
                
                // Update the stock in the main books table
                BookDao.updateBookStock(newBook);

                clearForm();
                stage.close();
            } else {
                showAlert("Invalid Input", "Please fill in all fields with valid data.");
            }
        });

        // Handle the Cancel button action
        cancelBtn.setOnAction(event -> stage.close());
    }

    // Method to check if the user input is valid
    private boolean isValidInput() {
        try {
            // Parse price and stock fields to ensure they are valid numbers
            Double.parseDouble(priceField.getText());
            Integer.parseInt(stockField.getText());

            // Check if title, author, price, and stock fields are not empty
            return !titleField.getText().isEmpty() &&
                   !authorField.getText().isEmpty() &&
                   !priceField.getText().isEmpty() &&
                   !stockField.getText().isEmpty();
        } catch (NumberFormatException e) {
            return false;  // Return false if parsing fails
        }
    }

    // Method to clear the form fields after adding a book
    private void clearForm() {
        titleField.clear();
        authorField.clear();
        priceField.clear();
        stockField.clear();
    }

    // Method to show an information alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to show the Add New Book window (stage)
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Add New Book");
        stage.show();
    }
}
