package model;

import java.sql.SQLException;
import dao.BookDao;
import dao.UserDao;
import dao.UserDaoImpl;

public class Model {
    private UserDao userDao;
    private User currentUser; 

    public Model() {
        userDao = new UserDaoImpl();
        initializeAdminAccount(); // Ensure admin account exists
    }

    // Method to ensure the admin account exists
    private void initializeAdminAccount() {
        try {
            User admin = userDao.getUser("admin", "reading_admin");
            if (admin == null) {
                // Create and add the admin account if not present
                admin = userDao.createUser("admin", "reading_admin", "Admin", "Admin");
                System.out.println("Admin account initialized.");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing admin account: " + e.getMessage());
        }
    }


    public void setup() throws SQLException {
        userDao.setup();
        BookDao.populateTempStockIfRequired(); // Replace the old method with the new one
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }
}
