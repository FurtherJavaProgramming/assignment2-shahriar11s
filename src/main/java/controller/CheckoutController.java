package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Book;
import model.Order;
import model.User;
import util.WindowManager;
import dao.BookDao;
import dao.OrderDao;
import model.Model;  // Add this import

import java.io.IOException;
import java.util.Map;

public class CheckoutController {

    @FXML
    private Label subtotalLabel;
    @FXML
    private Label taxLabel;
    @FXML
    private Label deliveryChargeLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Button goBackBtn;
    @FXML
    private Button proceedPaymentBtn;
    @FXML
    private javafx.scene.text.Text bookSummaryText;

    private Stage stage;
    private Stage parentStage;
    private Map<Book, Integer> cart;
    private double totalPrice;
    private User currentUser;  // Add field to store the current user
    private Model model;  // Add the model field

    // Constructor
    public CheckoutController(Stage stage, Stage parentStage, Map<Book, Integer> cart, double totalPrice, User currentUser, Model model) {
        this.stage = stage;
        this.parentStage = parentStage;
        this.cart = cart;
        this.totalPrice = totalPrice;
        this.currentUser = currentUser;  // Initialize currentUser
        this.model = model;  // Initialize model
    }

    @FXML
    public void initialize() {
        // Show summary of books
        StringBuilder bookSummary = new StringBuilder();
        double subtotal = 0.0;
        for (Book book : cart.keySet()) {
            int quantity = cart.get(book);
            double price = book.getPrice() * quantity;
            bookSummary.append(book.getTitle())
                    .append(" | Qty: ").append(quantity)
                    .append(" | ").append(String.format("%.2f AUD", price)).append("\n");
            subtotal += price;
        }
        bookSummaryText.setText(bookSummary.toString());

        // Set subtotal, tax, and delivery charge
        subtotalLabel.setText(String.format("%.2f AUD", subtotal));
        double tax = subtotal * 0.10;
        taxLabel.setText(String.format("%.2f AUD", tax));

        // Determine delivery charge
        double deliveryCharge = (getTotalQuantity() < 5) ? 9.99 : 0.0;
        deliveryChargeLabel.setText(String.format("%.2f AUD", deliveryCharge));

        // Calculate total price
        double finalTotal = subtotal + tax + deliveryCharge;
        totalLabel.setText(String.format("%.2f AUD", finalTotal));

        // Set button actions
        goBackBtn.setOnAction(event -> goBack());
        proceedPaymentBtn.setOnAction(event -> onProceedPayment());
    }

    private int getTotalQuantity() {
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    @FXML
    public void onProceedPayment() {
        try {
            // Load the PaymentPopup.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PaymentPopup.fxml"));

            // Set the controller explicitly (PaymentPopupController)
            PaymentPopupController paymentPopupController = new PaymentPopupController();
            loader.setController(paymentPopupController);

            // Since PaymentPopup.fxml uses VBox, cast to VBox instead of GridPane
            VBox paymentRoot = loader.load();

            // Create a new payment window
            Stage paymentStage = new Stage();
            paymentStage.setTitle("Payment Details");
            paymentStage.setScene(new Scene(paymentRoot, 400, 250));
            paymentStage.show();

            // Pass the necessary data to the controller
            paymentPopupController.setPaymentStage(paymentStage);
            paymentPopupController.setCheckoutController(this);
            paymentPopupController.setCurrentUser(currentUser);  // Pass currentUser to PaymentPopupController

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Finalize payment and save order
    public void processOrder(User user) {
        String orderId = generateOrderId();
        
        // Calculate total price including tax and delivery
        double subtotal = cart.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
        double tax = subtotal * 0.10;  // 10% GST
        double deliveryCharge = (getTotalQuantity() < 5) ? 9.99 : 0.0;
        double finalTotalPrice = subtotal + tax + deliveryCharge;

        // Create the order object with the final total price
        Order order = new Order(orderId, cart, finalTotalPrice, null);
        
        // Save the order with the correct total price
        OrderDao.saveOrder(order, user);  // Pass both Order and User

        // *** DO NOT update stock again here, as it was already updated when adding to the cart ***
        
        // Show payment success message
        showAlert(Alert.AlertType.INFORMATION, "Payment Successful", "Order placed successfully!\nOrder ID: " + orderId);

        // Clear the cart after placing the order
        cart.clear();

        // Load and show the homepage after the order is placed
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
            HomeController homeController = new HomeController(stage, this.model, this.cart, currentUser);  // Pass model, cart, and user
            loader.setController(homeController);

            Pane root = loader.load();
            Scene homeScene = new Scene(root);
            stage.setScene(homeScene);  // Set the new scene to the stage
            stage.show();  // Show the homepage
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis();
    }

    // Utility function to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBack() {
        parentStage.show();
        stage.close();
    }
    
    // Show the stage
    public void showStage(Pane root) {
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setTitle("Checkout");
        WindowManager.addWindow(stage);
        stage.setOnCloseRequest(event -> WindowManager.removeWindow(stage));
        stage.show();
    }

}
