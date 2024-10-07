package model;

import java.util.Map;
import java.util.HashMap;

public class Order {
    private String orderId;
    private Map<Book, Integer> cart;
    private double totalPrice;
    private String orderDate;
    private String bookSummary;

    public Order(String orderId, Map<Book, Integer> cart, double totalPrice, String orderDate) {
        this.orderId = orderId;
        setCart(cart); // Use setter to ensure null check and summary generation
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Map<Book, Integer> getCart() {
        return cart;
    }

    public void setCart(Map<Book, Integer> cart) {
        if (cart == null) {
            this.cart = new HashMap<>();
        } else {
            this.cart = cart;
        }
        updateBookSummary();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getBookSummary() {
        return bookSummary;
    }

    // Method to update book summary
    public void updateBookSummary() {
        this.bookSummary = generateBookSummary();
    }

    // Helper method to generate book summary from the cart
    private String generateBookSummary() {
        if (cart == null || cart.isEmpty()) {
            return "No books";
        }
        
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<Book, Integer> entry : cart.entrySet()) {
            Book book = entry.getKey();
            int quantity = entry.getValue();
            summary.append(book.getTitle()).append(" (").append(quantity).append("), ");
        }
        return summary.substring(0, summary.length() - 2);  // Remove last comma and space
    }

    @Override
    public String toString() {
        return "Order ID: " + orderId + "\nTotal Price: " + totalPrice + " AUD\nOrder Date: " + orderDate + "\nBooks: " + bookSummary;
    }
}