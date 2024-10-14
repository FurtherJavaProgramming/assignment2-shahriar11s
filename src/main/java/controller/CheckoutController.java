package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PaymentPopup.fxml"));
                PaymentPopupController paymentPopupController = new PaymentPopupController();
                loader.setController(paymentPopupController);

                VBox paymentRoot = loader.load();

                Stage paymentStage = new Stage();
                paymentStage.initModality(Modality.APPLICATION_MODAL);
                paymentStage.initOwner(stage);
                paymentStage.setTitle("Payment Details");
                paymentStage.setScene(new Scene(paymentRoot, 350, 300));

                paymentPopupController.setPaymentStage(paymentStage);
                paymentPopupController.setCheckoutController(this);
                paymentPopupController.setCurrentUser(currentUser);

                paymentStage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while processing the payment.");
            }
        });
    }

    public void processOrder(User user) {
        Platform.runLater(() -> {
            String orderId = generateOrderId();

            double subtotal = cart.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
            double tax = subtotal * 0.10;
            double deliveryCharge = (getTotalQuantity() < 5) ? 9.99 : 0.0;
            double finalTotalPrice = subtotal + tax + deliveryCharge;

            Order order = new Order(orderId, cart, finalTotalPrice, null);
            OrderDao.saveOrder(order, user);

            for (Book book : cart.keySet()) {
                int quantity = cart.get(book);
                BookDao.updateBookStockFromTemp(book.getId());
                book.setSold(book.getSold() + quantity);
                BookDao.updateBookSold(book.getId(), book.getSold());
                BookDao.updateTempStock(book.getId(), book.getStock());
            }

            showAlert(Alert.AlertType.INFORMATION, "Payment Successful", "Order placed successfully!\nOrder ID: " + orderId);

            cart.clear();

            // Navigate back to Home view
            navigateToHome();
        });
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
            HomeController homeController = new HomeController(parentStage, this.model, this.cart, currentUser);
            loader.setController(homeController);

            Pane root = loader.load();
            homeController.showStage(root);
            
            // Close the Checkout stage
            stage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error loading HomeView: " + e.getMessage());
        }
    }

    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    public void goBack() {
        parentStage.show();
        stage.close();
    }
    
    public void showStage(Pane root) {
        VBox vboxRoot = (VBox) root;
        Scene scene = new Scene(vboxRoot, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Checkout");
        WindowManager.addWindow(stage);
        stage.setOnCloseRequest(event -> WindowManager.removeWindow(stage));
        stage.show();
    }

}