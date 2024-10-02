package model;

import java.util.Map;

public class Order {
    private String orderId;
    private Map<Book, Integer> cart;
    private double totalPrice;

    // Constructor
    public Order(String orderId, Map<Book, Integer> cart, double totalPrice) {
        this.orderId = orderId;
        this.cart = cart;
        this.totalPrice = totalPrice;
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
        this.cart = cart;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Optional: To display order details in a readable format
    @Override
    public String toString() {
        return "Order ID: " + orderId + "\nTotal Price: " + totalPrice + " AUD\n";
    }
}
