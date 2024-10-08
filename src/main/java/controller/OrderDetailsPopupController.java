package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Book;
import model.Order;
import util.WindowManager;

import java.util.Map;

public class OrderDetailsPopupController {

    private Stage stage;
    private Order order;

    public OrderDetailsPopupController(Order order) {
        this.order = order;
        this.stage = new Stage();
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.setTitle("Order Details");
    }

    @FXML
    public void initialize() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label orderIdLabel = new Label("Order ID: " + order.getOrderId());
        Label dateLabel = new Label("Date: " + order.getOrderDate());
        Label totalPriceLabel = new Label("Total Price: $" + String.format("%.2f", order.getTotalPrice()));

        TextArea booksArea = new TextArea();
        booksArea.setEditable(false);
        booksArea.setPrefHeight(200);

        StringBuilder booksText = new StringBuilder();
        for (Map.Entry<Book, Integer> entry : order.getCart().entrySet()) {
            Book book = entry.getKey();
            int quantity = entry.getValue();
            booksText.append(book.getTitle())
                     .append(" by ")
                     .append(book.getAuthor())
                     .append(" (Quantity: ")
                     .append(quantity)
                     .append(")\n");
        }
        booksArea.setText(booksText.toString());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());

        layout.getChildren().addAll(orderIdLabel, dateLabel, totalPriceLabel, new Label("Books:"), booksArea, closeButton);

        Scene scene = new Scene(layout, 400, 400);
        WindowManager.addWindow(stage);
        stage.setOnCloseRequest(event -> WindowManager.removeWindow(stage));
        stage.setScene(scene);
    }

    public void showAndWait() {
        stage.showAndWait();
    }
}