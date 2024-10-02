package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User; // Import the User class
import model.Book;
import model.Order;

import java.time.YearMonth;
import java.util.Map;

public class PaymentPopupController {

    @FXML
    private TextField cardNumberField;
    @FXML
    private TextField expiryDateField;
    @FXML
    private TextField cvvField;

    private Stage paymentStage;
    private CheckoutController checkoutController;
    private User currentUser; // Add a field to store the current user

    // Set the Payment stage
    public void setPaymentStage(Stage paymentStage) {
        this.paymentStage = paymentStage;
    }

    // Set the CheckoutController to handle order processing
    public void setCheckoutController(CheckoutController checkoutController) {
        this.checkoutController = checkoutController;
    }

    // Set the current user
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void onSubmitPayment() {
        String cardNumber = cardNumberField.getText();
        String expiryDate = expiryDateField.getText();
        String cvv = cvvField.getText();

        // Validate card details
        if (!isValidCardNumber(cardNumber)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Card Number", "Card number must be 16 digits.");
        } else if (!isValidExpiryDate(expiryDate)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Expiry Date", "Expiry date must be in the future.");
        } else if (!isValidCVV(cvv)) {
            showAlert(Alert.AlertType.ERROR, "Invalid CVV", "CVV must be 3 digits.");
        } else {
            // Process the payment (assume it's successful) and place the order
            checkoutController.processOrder(currentUser); // Use the current user

            paymentStage.close(); // Close the payment popup
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber.matches("\\d{16}"); // Must be exactly 16 digits
    }

    private boolean isValidExpiryDate(String expiryDate) {
        try {
            String[] parts = expiryDate.split("/");
            if (parts.length != 2) return false;
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt("20" + parts[1]);
            if (month < 1 || month > 12) return false;

            YearMonth expiry = YearMonth.of(year, month);
            YearMonth now = YearMonth.now();
            return expiry.isAfter(now);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidCVV(String cvv) {
        return cvv.matches("\\d{3}"); // Must be exactly 3 digits
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
