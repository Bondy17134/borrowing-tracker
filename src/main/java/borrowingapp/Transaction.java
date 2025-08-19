package borrowingapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.text.DateFormatter;

public class Transaction {
	private int id; // added id field following AWS RDS
	private double amount;
	private String description;
	private LocalDateTime timestamp;
	
	// constructor for transactions
	public Transaction(double amount, String description) {
		// without ID, as DB generates it
		this.amount = amount;
		this.description = description;
		this.timestamp = LocalDateTime.now(); // local time for new entries 
	}
	
	// constructor for loading from database (with ID)
	public Transaction(int id, double amount, String description, LocalDateTime timestamp) {
		this.id = id;
		this.amount = amount;
		this.description = description;
		this.timestamp = timestamp;
	}
	
	// getters
	
	public int getId() {
		return id;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public String getDescription() {
		return description;
	}
	
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	
	@Override
	public String toString() {
		// define a formatter or both date and time
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedTimestamp = (timestamp != null) ? timestamp.format(formatter) : "N/A";
		// format the amount for display
		String formattedAmount = String.format("%.2f", amount);
		return id + "," + formattedAmount + "," + description + "," + formattedTimestamp;
	}
}
