package dao;

import model.Book;
import model.Order;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDao {

    // Updated saveOrder method in OrderDao.java
    public static void saveOrder(Order order, User user) {
        String sql = "INSERT OR REPLACE INTO orders (username, order_number, book_id, quantity, total_price, date) VALUES (?, ?, ?, ?, ?, ?)";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
        String formattedDateTime = LocalDateTime.now().format(formatter);
        
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (Book book : order.getCart().keySet()) {
                int quantity = order.getCart().get(book);
                stmt.setString(1, user.getUsername());
                stmt.setString(2, order.getOrderId());
                stmt.setInt(3, book.getId());
                stmt.setInt(4, quantity);
                stmt.setDouble(5, order.getTotalPrice());
                stmt.setString(6, formattedDateTime);  // Add this line to set the date
                stmt.executeUpdate();
            }

            System.out.println("Order saved for user: " + user.getUsername() + " | Order ID: " + order.getOrderId() + " | Date: " + formattedDateTime);

        } catch (SQLException e) {
            System.err.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to retrieve all orders in reverse chronological order
    public static List<Order> getOrders(User user) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT DISTINCT order_number, total_price, date FROM orders WHERE username = ? ORDER BY date DESC";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String orderId = rs.getString("order_number");
                    double totalPrice = rs.getDouble("total_price");
                    String orderDate = rs.getString("date");

                    System.out.println("Fetched order: Order ID: " + orderId + ", Total Price: " + totalPrice + ", Date: " + orderDate);

                    Map<Book, Integer> cart = getCartForOrder(orderId, connection);
                    orders.add(new Order(orderId, cart, totalPrice, orderDate));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Total orders fetched for user " + user.getUsername() + ": " + orders.size());
        return orders;
    }

    private static Map<Book, Integer> getCartForOrder(String orderId, Connection connection) throws SQLException {
        Map<Book, Integer> cart = new HashMap<>();
        String sql = "SELECT b.id, b.title, b.author, b.price, o.quantity FROM orders o JOIN books b ON o.book_id = b.id WHERE o.order_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int bookId = rs.getInt("id");
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity");

                    Book book = new Book(bookId, title, author, price, 0, 0);
                    cart.put(book, quantity);
                }
            }
        }

        System.out.println("Cart for order " + orderId + ": " + cart.size() + " items");
        return cart;
    }
}