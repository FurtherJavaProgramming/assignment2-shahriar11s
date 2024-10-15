package controller;

import dao.OrderDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Order;
import model.User;
import util.WindowManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class OrderListController {
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, String> orderNumberCol;
    @FXML
    private TableColumn<Order, String> dateCol;
    @FXML
    private TableColumn<Order, Double> totalPriceCol;
    @FXML
    private TableColumn<Order, Void> detailsCol;
    @FXML
    private Button closeButton;
    @FXML
    private Button exportButton; // New export button

    private Stage stage;
    private Stage parentStage;
    private User currentUser;

    public OrderListController(Stage stage, Stage parentStage, User currentUser) {
        this.stage = stage;
        this.parentStage = parentStage;
        this.currentUser = currentUser;
        System.out.println("OrderListController created with user: " + (currentUser != null ? currentUser.getUsername() : "null"));
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing OrderListController");
        
        orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        
        setupDetailsColumn();

        loadOrders();

        // Set action for export button
        exportButton.setOnAction(event -> handleExport());
        
        // Enable multiple selection
        orderTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupDetailsColumn() {
        detailsCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = new Button("Details");

            {
                detailsButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDetails(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });
    }

    private void showOrderDetails(Order order) {
        OrderDetailsPopupController popupController = new OrderDetailsPopupController(order);
        popupController.initialize(); // We need to call initialize manually as we're not using FXML
        popupController.showAndWait();
    }

    private void loadOrders() {
        System.out.println("Loading orders for user: " + currentUser.getUsername());
        
        Task<List<Order>> task = new Task<>() {
            @Override
            protected List<Order> call() throws Exception {
                return OrderDao.getOrders(currentUser);
            }
        };

        task.setOnSucceeded(event -> {
            List<Order> orders = task.getValue();
            ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
            orderTable.setItems(orderList);
            
            System.out.println("Order table populated with " + orderList.size() + " orders.");
            
            if (orderList.isEmpty()) {
                System.out.println("No orders found for user: " + currentUser.getUsername());
            } else {
                for (Order order : orderList) {
                    System.out.println("Order in list: " + order.getOrderId() + ", Date: " + order.getOrderDate());
                }
            }
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            System.err.println("Error loading orders: " + exception.getMessage());
            exception.printStackTrace();
        });

        new Thread(task).start();
    }

    // New method to handle exporting orders
    @FXML
    private void handleExport() {
        // Check if any orders are selected
        ObservableList<Order> selectedOrders = orderTable.getSelectionModel().getSelectedItems();
        if (selectedOrders.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Orders Selected", "Please select one or more orders to export.");
            return;
        }

        // Generate default file name using current date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String defaultFileName = "Orders_" + LocalDateTime.now().format(formatter) + ".csv";

        // Open FileChooser for the user to select the destination file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Orders");
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            exportOrdersToCSV(file, selectedOrders);
        }
    }

    // Method to generate the list of books for a specific order
    private String getBooksList(Order order) {
        return order.getCart().entrySet().stream()
            .map(entry -> entry.getKey().getTitle() + " (" + entry.getValue() + ")")
            .collect(Collectors.joining("; "));  // Separate each book with a semicolon
    }

    // Method to export selected orders to a CSV file
    private void exportOrdersToCSV(File file, List<Order> selectedOrders) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header
            writer.write("Order ID,Date,Total Price (AUD),Books\n");

            // Write order details for each selected order
            for (Order order : selectedOrders) {
                // Use the helper method to generate the book list
                String booksList = getBooksList(order);

                writer.write(order.getOrderId() + "," + order.getOrderDate() + "," + order.getTotalPrice() + "," + booksList + "\n");
            }

            writer.flush();
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Orders exported successfully!");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "An error occurred while exporting orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        stage.close();
        // Show the Home view
        if (parentStage != null) {
            parentStage.show();
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Order History");
        WindowManager.addWindow(stage);
        stage.setOnCloseRequest(event -> {
            WindowManager.removeWindow(stage);
            if (parentStage != null) {
                parentStage.show();
            }
        });
        stage.show();
        if (parentStage != null) {
            parentStage.hide();
        }
    }

    // Utility function to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
