package dao;

import model.Book;
import model.Order;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderDao {

	// Updated saveOrder method in OrderDao.java
	public static void saveOrder(Order order, User user) {
	    String sql = "INSERT INTO orders (username, order_number, book_id, quantity, total_price) VALUES (?, ?, ?, ?, ?)"; // Added total_price
	    try (Connection connection = Database.getConnection();
	         PreparedStatement stmt = connection.prepareStatement(sql)) {

	        for (Book book : order.getCart().keySet()) {
	            int quantity = order.getCart().get(book);
	            stmt.setString(1, user.getUsername());  // Save username instead of user_id
	            stmt.setString(2, order.getOrderId());
	            stmt.setInt(3, book.getId());
	            stmt.setInt(4, quantity);
	            stmt.setDouble(5, order.getTotalPrice());  // Save total_price
	            stmt.executeUpdate();
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}





    // Method to retrieve user ID based on username
    private static int getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("User not found");
            }
        }
    }

    // Method to retrieve all orders
    public static List<Order> getOrders(User user) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ?";  // Assuming the table contains order details

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, getUserIdByUsername(user.getUsername()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderId = rs.getString("order_number");
                int bookId = rs.getInt("book_id");
                int quantity = rs.getInt("quantity");
                double totalPrice = rs.getDouble("total_price");

                Book book = BookDao.getBookById(bookId);  // Assuming BookDao has this method

                if (book != null) {
                    // Assuming you use some way to rebuild the cart from these details
                    orders.add(new Order(orderId, Map.of(book, quantity), totalPrice));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }
}
