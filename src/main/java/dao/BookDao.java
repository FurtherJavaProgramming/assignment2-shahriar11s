package dao;

import model.Book;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
	private static List<Book> books = new ArrayList<>();
	
	static {
		books.add(new Book(1, "Absolute Java", "Savitch", 50.00, 10));
        books.add(new Book(2, "JAVA: How to Program", "Deitel and Deitel", 70.00, 100));
        books.add(new Book(3, "Computing Concepts with JAVA 8 Essentials", "Horstman", 89.00, 500));
        books.add(new Book(4, "Java Software Solutions", "Lewis and Loftus", 99.00, 500));
        books.add(new Book(5, "Java Program Design", "Cohoon and Davidson", 29.00, 2));
        books.add(new Book(6, "Clean Code", "Robert Martin", 45.00, 100));
        books.add(new Book(7, "Gray Hat C#", "Brandon Perry", 68.00, 300));
        books.add(new Book(8, "Python Basics", "David Amos", 49.00, 1000));
        books.add(new Book(9, "Bayesian Statistics The Fun Way", "Will Kurt", 42.00, 600));
	}
	
	public static List<Book> getAllBooks() {
		return books;
	}
}