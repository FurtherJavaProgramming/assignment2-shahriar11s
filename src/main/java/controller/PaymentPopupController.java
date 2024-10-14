package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PaymentPopupController {

    @FXML private TextField cardNumberField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;

    private Stage paymentStage;
    private CheckoutController checkoutController;
    private User currentUser;

    @FXML
    public void initialize() {
        expiryDateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 2 && oldValue.length() == 1) {
                expiryDateField.setText(newValue + "/");
            } else if (newValue.length() > 5) {
                expiryDateField.setText(oldValue);
            }
        });
    }

    public void setPaymentStage(Stage paymentStage) {
        this.paymentStage = paymentStage;
    }

    public void setCheckoutController(CheckoutController checkoutController) {
        this.checkoutController = checkoutController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void onSubmitPayment() {
        String cardNumber = cardNumberField.getText().replaceAll("\\s", "");
        String expiryDate = expiryDateField.getText();
        String cvv = cvvField.getText();

        if (!isValidCardNumber(cardNumber)) {
            showAlert("Invalid Card Number", "Please enter a valid 16-digit card number.");
        } else if (!isValidExpiryDate(expiryDate)) {
            showAlert("Invalid Expiry Date", "Please enter a valid future expiry date in MM/YY format.");
        } else if (!isValidCVV(cvv)) {
            showAlert("Invalid CVV", "Please enter a valid 3-digit CVV.");
        } else {
            Platform.runLater(() -> {
                checkoutController.processOrder(currentUser);
                paymentStage.close();
            });
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber.matches("\\d{16}");
    }

    private boolean isValidExpiryDate(String expiryDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiry = YearMonth.parse(expiryDate, formatter);
            return expiry.isAfter(YearMonth.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidCVV(String cvv) {
        return cvv.matches("\\d{3}");
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(paymentStage);
            alert.showAndWait();
        });
    }
}