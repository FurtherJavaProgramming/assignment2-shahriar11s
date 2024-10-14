package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.User;
import model.Order;
import dao.UserDao;
import dao.OrderDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;

import java.sql.SQLException;
import java.util.List;

public class ViewUsersController {

    @FXML private TableView<UserViewModel> usersTable;
    @FXML private TableColumn<UserViewModel, String> nameColumn;
    @FXML private TableColumn<UserViewModel, Double> totalPurchaseColumn;
    @FXML private TableColumn<UserViewModel, Integer> totalOrdersColumn;
    @FXML private TableColumn<UserViewModel, Button> orderDetailsColumn;
    @FXML private Button closeButton;

    private Stage stage;
    private UserDao userDao;
    private OrderDao orderDao;
    private Stage parentStage;


    public ViewUsersController(Stage stage, Stage parentStage, UserDao userDao, OrderDao orderDao) {
        this.stage = stage;
        this.parentStage = parentStage;
        this.userDao = userDao;
        this.orderDao = orderDao;
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        totalPurchaseColumn.setCellValueFactory(new PropertyValueFactory<>("totalPurchase"));
        totalOrdersColumn.setCellValueFactory(new PropertyValueFactory<>("totalOrders"));
        orderDetailsColumn.setCellValueFactory(new PropertyValueFactory<>("orderDetailsButton"));

        totalPurchaseColumn.setStyle("-fx-alignment: CENTER;");
        totalOrdersColumn.setStyle("-fx-alignment: CENTER;");
        orderDetailsColumn.setStyle("-fx-alignment: CENTER;");

        loadUsers();

        closeButton.setOnAction(event -> {
            stage.close();
            parentStage.show();  // Show the Admin Dashboard when closing
        });
    }

    private void loadUsers() {
        ObservableList<UserViewModel> userViewModels = FXCollections.observableArrayList();

        try {
            List<User> users = userDao.getAllUsers();
            for (User user : users) {
                if (!"admin".equals(user.getUsername())) {
                    List<Order> orders = orderDao.getOrders(user);
                    double totalPurchase = orders.stream().mapToDouble(Order::getTotalPrice).sum();
                    int totalOrders = orders.size();

                    Button orderDetailsButton = new Button("View Orders");
                    orderDetailsButton.setOnAction(event -> showOrderDetails(user, orders));

                    userViewModels.add(new UserViewModel(
                        user.getFirstName() + " " + user.getLastName(),
                        totalPurchase,
                        totalOrders,
                        orderDetailsButton
                    ));
                }
            }

            usersTable.setItems(userViewModels);
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally, show an error message to the user
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load users: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showOrderDetails(User user, List<Order> orders) {
        Stage detailsStage = new Stage();
        VBox detailsBox = new VBox(10);
        detailsBox.setAlignment(Pos.TOP_LEFT);
        detailsBox.setPadding(new Insets(20));
        detailsBox.setStyle("-fx-background-color: #f4f4f4;");

        Label titleLabel = new Label("Order Details for " + user.getFirstName() + " " + user.getLastName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        detailsBox.getChildren().add(titleLabel);

        ScrollPane scrollPane = new ScrollPane();
        VBox ordersBox = new VBox(15);
        ordersBox.setPadding(new Insets(10, 0, 0, 0));

        for (Order order : orders) {
            VBox orderBox = new VBox(5);
            orderBox.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #ddd; -fx-border-width: 1px;");

            Label orderIdLabel = new Label("Order ID: " + order.getOrderId());
            orderIdLabel.setStyle("-fx-font-weight: bold;");
            Label totalPriceLabel = new Label(String.format("Total Price: %.2f AUD", order.getTotalPrice()));
            Label orderDateLabel = new Label("Order Date: " + order.getOrderDate());
            Label booksLabel = new Label("Books: " + order.getBookSummary());
            booksLabel.setWrapText(true);

            orderBox.getChildren().addAll(orderIdLabel, totalPriceLabel, orderDateLabel, booksLabel);
            ordersBox.getChildren().add(orderBox);
        }

        scrollPane.setContent(ordersBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(300);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        closeButton.setOnAction(e -> detailsStage.close());

        detailsBox.getChildren().addAll(scrollPane, closeButton);

        Scene detailsScene = new Scene(detailsBox, 400, 400);
        detailsStage.setScene(detailsScene);
        detailsStage.setTitle("Order Details for " + user.getFirstName() + " " + user.getLastName());
        detailsStage.show();
    }

    // ViewModel for the TableView
    public static class UserViewModel {
        private String name;
        private double totalPurchase;
        private int totalOrders;
        private Button orderDetailsButton;

        public UserViewModel(String name, double totalPurchase, int totalOrders, Button orderDetailsButton) {
            this.name = name;
            this.totalPurchase = totalPurchase;
            this.totalOrders = totalOrders;
            this.orderDetailsButton = orderDetailsButton;
        }

        // Getters
        public String getName() { return name; }
        public double getTotalPurchase() { return totalPurchase; }
        public int getTotalOrders() { return totalOrders; }
        public Button getOrderDetailsButton() { return orderDetailsButton; }
    }
    
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 870, 473);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("View Users");
        stage.show();
        parentStage.hide();  // Hide the Admin Dashboard when showing this stage
    }
}