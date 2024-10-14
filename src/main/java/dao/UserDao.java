package dao;

import java.sql.SQLException;
import java.util.List;

import model.User;

/**
 * A data access object (DAO) is a pattern that provides an abstract interface 
 * to a database or other persistence mechanism. 
 * the DAO maps application calls to the persistence layer and provides some specific data operations 
 * without exposing details of the database. 
 */
public interface UserDao {
    void setup() throws SQLException;
    
    User getUser(String username, String password) throws SQLException;
    
    // This method is used to create a user in the system. The admin user is added manually and not through this.
    User createUser(String username, String password, String firstName, String lastName) throws SQLException;

    // New method for updating user details
    void updateUser(User user) throws SQLException;

    // Method to initialize the fixed admin account directly
    default void initializeAdminAccount() throws SQLException {
        // Check if the admin user exists
        User admin = getUser("admin", "reading_admin");
        if (admin == null) {
            // Admin user does not exist, create the admin account directly in the database
            createUser("admin", "reading_admin", "Admin", "Admin");
            System.out.println("Admin account initialized in the database.");
        }
    }
    
    List<User> getAllUsers() throws SQLException;
}
