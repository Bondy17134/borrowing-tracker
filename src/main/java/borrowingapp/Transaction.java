package borrowingapp;

public class Transaction {
	private double amount;
	private String description;
	
	public Transaction(double amount, String description) {
		this.amount = amount;
		this.description = description;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public String getDescription() {
		return description;
	}
	
	// a method to format the transaction for saving to a file
	public String toFileString() {
		return String.format("%.2f,%s", amount, description);
	}
}
