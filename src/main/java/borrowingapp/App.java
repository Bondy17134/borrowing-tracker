package borrowingapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
	// AWS RDS database related variable details 
	private static final String JDBC_URL = "jdbc:mysql://borrowing-tracker-db.cba8m8wwshs9.ap-southeast-2.rds.amazonaws.com/borrowing-tracker-db";
	private static final String DB_USER = "admin";
	private static final String DB_PASSWORD = "Bond20100";
	
	private static List<Transaction> transaction = new ArrayList<>();
	private static double balance = 0.0;
	
	public static void main(String[] args) {
		// step 1: load transactions from the database when the program starts
		loadTransactionFromDatabase();
		
		Scanner scanner = new Scanner(System.in);
		
		boolean running = true;
		while(running) {
			System.out.println("--- Borrowing Tracker ---");
			System.out.println("1. Add a transaction");
			System.out.println("2. Show current balance");
			System.out.println("3. Display all transactions");
			System.out.println("4. Exit");
			
			// read the user's choice
			int choice = -1;
			try {
				choice = scanner.nextInt();
			} catch(java.util.InputMismatchException e) {
				System.err.println("Invalid input. Please enter a number");
				scanner.next(); // clear the invalid input from the scanner
				continue;
			}
			
			switch(choice) {
				case 1: 
					System.out.print("Enter amount (+ for you, - for sister): ");
					double amount = scanner.nextDouble();
					scanner.nextLine(); // consume the rest of the line
					
					System.out.print("Enter a description: ");
					String description = scanner.nextLine();
					
					// step 2: create a new transaction and add it to the list
					Transaction newTransaction = new Transaction(amount, description);
					
					// step 3: save the new transaction to the database
					if(saveTransactionToDatabase(newTransaction)) {
						transaction.add(newTransaction);
						balance += amount;
						System.out.println("Transaction recorded. Balance updated.");
					} else {
						System.out.println("Failed to save transaction.");
					}
					break;
				case 2:
					System.out.printf("Current balance: $%.2f%n", balance);
					if(balance > 0) {
						System.out.println("Your sister owes you.");
					} else if(balance < 0) {
						System.out.println("You owe your sister.");
					} else {
						System.out.println("You are all square.");
					}
					break;
				case 3:
					displayAllTransactions();
					break;
				case 4:
					running = false;
					System.out.println("Exiting application");
					break;
				default:
					System.out.println("Invalid choice. Please enter 1, 2, or 3.");
			}
		}
		scanner.close();
	}

	private static void loadTransactionFromDatabase() {
		
		try(Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)){
			String sql = "SELECT id, amount, description, timestamp FROM transactions";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				double amount = rs.getDouble("amount");
				String description = rs.getString("description");
				transaction.add(new Transaction(amount, description));
				balance += amount;
				}
				System.out.println("Loaded " + transaction.size() + " past transactions.");
			} catch(SQLException e) {
			System.err.println("Database connection failed: " + e.getMessage());
		}
	}
	
	private static boolean saveTransactionToDatabase(Transaction transaction) {
		try(Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)){
			String sql = "INSERT INTO transactions (amount, description) VALUES (?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setDouble(1, transaction.getAmount());
			stmt.setString(2, transaction.getDescription());
			stmt.executeUpdate();
			return true;
		}catch(SQLException e) {
			System.err.println("Failed to save transaction to database: " + e.getMessage());
			return false;
		}
	}
	
	private static void displayAllTransactions() {
		if(transaction.isEmpty()) {
			System.out.println("No transaction recorded yet.");
			return;
		}
		System.out.println("\n--- All Transactions ---");
		for(Transaction t: transaction) {
			System.out.println(t); // uses the overriden toString() method in Transaction class
		}
	}
}
