package dao;

import model.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookDao {
    private static List<Book> books = new ArrayList<>();

    static {
        books.add(new Book(1, "Absolute Java", "Savitch", 50.00, 10, 142));
        books.add(new Book(2, "JAVA: How to Program", "Deitel and Deitel", 70.00, 100, 475));
        books.add(new Book(3, "Computing Concepts with JAVA 8 Essentials", "Horstman", 89.00, 500, 60));
        books.add(new Book(4, "Java Software Solutions", "Lewis and Loftus", 99.00, 500, 12));
        books.add(new Book(5, "Java Program Design", "Cohoon and Davidson", 29.00, 2, 86));
        books.add(new Book(6, "Clean Code", "Robert Martin", 45.00, 100, 300));
        books.add(new Book(7, "Gray Hat C#", "Brandon Perry", 68.00, 300, 178));
        books.add(new Book(8, "Python Basics", "David Amos", 49.00, 1000, 79));
        books.add(new Book(9, "Bayesian Statistics The Fun Way", "Will Kurt", 42.00, 600, 155));
    }

    public static List<Book> getAllBooks() {
        return books;
    }

    // Method to search for books by keyword (matches title or author)
    public static List<Book> searchBooks(String keyword) {
        return books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                book.getAuthor().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Method to get the top 5 books based on the number of sold copies
    public static List<Book> getTopBooks(int limit) {
        return books.stream()
                .sorted((b1, b2) -> Integer.compare(b2.getSold(), b1.getSold()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
