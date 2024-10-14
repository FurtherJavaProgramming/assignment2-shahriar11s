package dao;

import model.Book;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    // Method to retrieve all books from the database with stock from temp_stock
    public static List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.id, b.title, b.author, b.price, ts.stock, b.sold " +
                     "FROM books b LEFT JOIN temp_stock ts ON b.id = ts.book_id";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),  // Use temp_stock value
                    rs.getInt("sold")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }
    
    public static List<Book> getAdminBookList() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getInt("sold")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }


    // Method to search for books by keyword (matches title or author)
    public static List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.id, b.title, b.author, b.price, ts.stock, b.sold " +
                     "FROM books b LEFT JOIN temp_stock ts ON b.id = ts.book_id " +
                     "WHERE LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword.toLowerCase() + "%");
            stmt.setString(2, "%" + keyword.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),  // temp_stock
                        rs.getInt("sold")
                    );
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Method to get the top 5 books based on the number of sold copies
    public static List<Book> getTopBooks(int limit) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY sold DESC LIMIT ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getInt("sold")
                    );
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Method to update the sold copies of a book in the database
    public static void updateBookSold(int bookId, int newSold) {
        String sql = "UPDATE books SET sold = ? WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newSold);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to populate temp_stock if required
    public static void populateTempStockIfRequired() {
        if (getIsRunningFlag() == 1) {
            System.out.println("Program already running, skipping temp stock population.");
            return; // Skip populating temp_stock if the program is running
        }

        Timestamp lastUpdate = getLastTempStockUpdate();

        // Check if more than 1 minute has passed since the last update
        if (lastUpdate == null || (System.currentTimeMillis() - lastUpdate.getTime()) > 60 * 1000) {
            populateTempStock();  // Populate the temp_stock table
            updateLastTempStockUpdate(); // Update the last updated timestamp
            setIsRunningFlag(1);  // Set the is_running flag to 1 to indicate the program is running
        } else {
            System.out.println("Temp stock was updated less than a minute ago. Skipping update.");
        }
    }


    // Method to get the is_running flag from the database
    public static int getIsRunningFlag() {
        String sql = "SELECT is_running FROM temp_stock_meta LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("is_running");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if no record is found
    }

    // Method to set the is_running flag in the database
    public synchronized static void setIsRunningFlag(int flag) {
        String sql = "UPDATE temp_stock_meta SET is_running = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, flag);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Populate temp_stock
    public static void populateTempStock() {
        String sql = "INSERT OR REPLACE INTO temp_stock (book_id, stock) " +
                     "SELECT id, stock FROM books";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("Temp stock table populated from books table.");
        } catch (SQLException e) {
            System.err.println("Error populating temp_stock: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update stock in temp_stock
    public static void updateTempStock(int bookId, int newStock) {
        String sql = "UPDATE temp_stock SET stock = ? WHERE book_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update stock in books table from temp_stock when order is placed
    public static void updateBookStockFromTemp(int bookId) {
        String sql = "UPDATE books SET stock = (SELECT stock FROM temp_stock WHERE book_id = ?) WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Clear the temp_stock table
    public static void clearTempStock() {
        String sql = "DELETE FROM temp_stock";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch a book by its ID
    public static Book getBookById(int bookId) {
        String sql = "SELECT * FROM books WHERE id = ?";
        Book book = null;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                book = new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getInt("sold")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return book;
    }

    // Retrieve the last update time of temp_stock
    public static Timestamp getLastTempStockUpdate() {
        String sql = "SELECT last_updated FROM temp_stock_meta LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getTimestamp("last_updated");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no record found
    }

    // Update the last update time in temp_stock_meta
    public static void updateLastTempStockUpdate() {
        String sql = "INSERT OR REPLACE INTO temp_stock_meta (last_updated) VALUES (CURRENT_TIMESTAMP)";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Add a new book to the database
    public static void addNewBook(Book book) {
        String sql = "INSERT INTO books (id, title, author, price, stock, sold) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, book.getId());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setDouble(4, book.getPrice());
            stmt.setInt(5, book.getStock());
            stmt.setInt(6, book.getSold());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // New method to update the stock of a book
    public static void updateBookStock(Book book) {
        String sql = "UPDATE books SET stock = ? WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, book.getStock());
            stmt.setInt(2, book.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Method to get the highest book ID from the books table
    public static int getHighestBookId() {
        String sql = "SELECT MAX(id) AS max_id FROM books";
        int highestId = 0;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                highestId = rs.getInt("max_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return highestId;
    }
    
    // deleteBook method
    public static void deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            System.out.println("Book with ID " + bookId + " has been deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
