package model;

public class Book {
	private int id;
	private String title;
	private String author;
	private double price;
	private int stock;
	
	public Book(int id, String title, String author, double price, int stock) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.price = price;
		this.stock = stock;
	}
	
	public int getID() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public double getPrice() {
		return price;
	}
	
	public int getStock() {
		return stock;
	}
	
	public void setStock(int stock) {
		this.stock = stock;
	}
}