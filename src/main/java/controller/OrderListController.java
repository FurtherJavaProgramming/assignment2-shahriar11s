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
import javafx.stage.Stage;
import model.Order;
import model.User;
import util.WindowManager;

import java.util.List;

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

    private Stage stage;
    private User currentUser;

    public OrderListController(Stage stage, User currentUser) {
        this.stage = stage;
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

    @FXML
    private void handleClose() {
        stage.close();
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Order History");
        WindowManager.addWindow(stage);
        stage.setOnCloseRequest(event -> WindowManager.removeWindow(stage));
        stage.show();
    }
}