package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    // URL pattern for database
    private static final String DB_URL = "jdbc:sqlite:application.db";
    
    // Cache the connection for reuse
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Initialize the connection
                connection = DriverManager.getConnection(DB_URL);
            } catch (SQLException e) {
                // Print an error message if the connection fails
                System.err.println("Failed to connect to the database: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
}