package borrowingapp;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class App extends Application{
	
	// AWS RDS database related variable details 
	private static final String JDBC_URL = "jdbc:mysql://borrowing-tracker-db.cba8m8wwshs9.ap-southeast-2.rds.amazonaws.com/borrowing-tracker-db";
	private static final String DB_USER = "admin";
	private static final String DB_PASSWORD = "Bond20100";
	
	private static List<Transaction> transactions = new ArrayList<>();
    private static double balance = 0.0;
    
    // GUI components
    private Label balanceLabel;
    private TextField amountField;
    private TextField descriptionField;
    private Label statusLabel; // for user feedback

	public void start(Stage primaryStage) {
		// 1. load data from database on application start
		loadTransactionFromDatabase();
		
		// 2. set up the main layout (GridPane is good for forms)
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER); // center the grid in the scene
		grid.setHgap(10); // horizontal gap between columns
		grid.setVgap(10); // vertical gap between rows
		grid.setPadding(new Insets(25, 25, 25, 25)); // padding around the grid
		
		// 3. add GUI elements
		Label titleLabel = new Label("Borrowing Tracker");
		titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		grid.add(titleLabel, 0, 0, 2, 1); // col, row, ColSpan, RowSpan (Spans 2 columns)
		GridPane.setHalignment(titleLabel, HPos.CENTER); // center title in its allocated space
		
		// amount input
		Label amountLbl = new Label("Amount: ");
		grid.add(amountLbl, 0, 1);
		amountField = new TextField();
		amountField.setPromptText("e.g., 200 or -50");
		grid.add(amountField, 1, 1);
		
		// description input
		Label descriptionLbl = new Label("Description: ");
		grid.add(descriptionLbl, 0, 2);
		descriptionField = new TextField();
		descriptionField.setPromptText("e.g., Lunch, Rent, Coffee");
		grid.add(descriptionField, 1, 2);
		
		// add transaction button
		Button addTransactionButton = new Button("Add Transaction");
		addTransactionButton.setMaxWidth(Double.MAX_VALUE); // make button fill width
		grid.add(addTransactionButton, 1, 3);
		
		// current balance history
		balanceLabel = new Label(); // will be updated by updatedBalanceDisplay()
		balanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		grid.add(balanceLabel, 0, 4, 2, 1);
		GridPane.setHalignment(balanceLabel, HPos.CENTER);
		
		// status label for feedback
		statusLabel = new Label("Ready.");
		statusLabel.setFont(Font.font("Arial", 12));
		grid.add(statusLabel, 0, 5, 2, 1);
		GridPane.setHalignment(statusLabel, HPos.CENTER);
		
		// 4. event handling 
		addTransactionButton.setOnAction(event -> addTransaction());
		
		// 5. initial display update
		updateBalanceDisplay();
		
		// 6. set up the scene and stage
		Scene scene = new Scene(grid, 500, 400);
		primaryStage.setTitle("Borrowing Tracker - GUI");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	// --- Helper Methods ---
	
	// method to handle adding a transaction from GUI input
	private void addTransaction() {
		try {
			double amount = Double.parseDouble(amountField.getText());
			String description = descriptionField.getText().trim();
			
			if(description.isEmpty()) {
				statusLabel.setText("Error: Description cannot be empty.");
				return;
			}
			
			Transaction newTransaction = new Transaction(amount, description);
			
			// save the new transaction to the database
			if(saveTransactionToDatabase(newTransaction)) {
				// clear input field
				amountField.clear();
				descriptionField.clear();
				statusLabel.setText("Transaction added successfully.");
				// reload transaction and update display
				loadTransactionFromDatabase();
				updateBalanceDisplay();
			}else {
				statusLabel.setText("Error: Failed to save transaction");
			}
		} catch(NumberFormatException e){
			statusLabel.setText("Error: Please enter a valid number for amount");
		}
		catch(Exception e) {
			statusLabel.setText("An unexpected error occurred: " + e.getMessage());
		}
	}
	
	// method to update the balance label text
	private void updateBalanceDisplay() {
		balanceLabel.setText(String.format("Current Balance: $%.2f", balance));
		if(balance < 0) {
			balanceLabel.setText(balanceLabel.getText() + " (Your sister owes you.)");
		} else if (balance > 0) {
            balanceLabel.setText(balanceLabel.getText() + " (You owe your sister.)");
        } else {
            balanceLabel.setText(balanceLabel.getText() + " (You are all square.)");
        }
	}
	
	// --- Database Methods ---
	
	// loads all transactions from the database and recalculates the balance
	private static void loadTransactionFromDatabase() {
		transactions.clear(); // clear current list before loading
		balance = 0.0; // reset balance before recalculating from database
		try(Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)){
			String sql = "SELECT id, amount, description, timestamp FROM transactions ORDER BY timestamp ASC"; // order by timestamp for consistent display
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				int id = rs.getInt("id");
				double amount = rs.getDouble("amount");
				String description = rs.getString("description");
				Timestamp dbTimestamp = rs.getTimestamp("timestamp");
				// ternary operator (if-else shorthand)
				// condition ? value_if_true : value_if_false
				LocalDateTime timestamp = dbTimestamp != null ? dbTimestamp.toLocalDateTime() : null;
				
				transactions.add(new Transaction(id, amount, description, timestamp));
				balance += amount;
				}
				System.out.println("Loaded " + transactions.size() + " past transactions.");
			} catch(SQLException e) {
			System.err.println("Database connection failed during loading: " + e.getMessage());
		}
	}
	
	private static boolean saveTransactionToDatabase(Transaction transaction) {
		try(Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)){
			// user RETURN_GENERATE_KEYS to get the auto-incremented ID back 
			String sql = "INSERT INTO transactions (amount, description) VALUES (?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
			stmt.setDouble(1, transaction.getAmount());
			stmt.setString(2, transaction.getDescription());
			int affedtedRow = stmt.executeUpdate();
			
			if(affedtedRow == 0) {
				throw new SQLException("Creating transaction failed, no rows affected.");
			}
			
			// retrieve the generated ID and update the Transaction object if needed
			try(ResultSet generatedKeys = stmt.getGeneratedKeys()){
				if(generatedKeys.next()) {
					
				}
			}catch(SQLException e) {
				throw new SQLException("Creating transaction failed, no ID obtained.");
			}
			return true;
		}catch(SQLException e) {
			System.err.println("Failed to save transaction to database: " + e.getMessage());
			return false;
		}
	}
	
	private static void displayAllTransactions() {
		if(transactions.isEmpty()) {
			System.out.println("No transaction recorded yet.");
			return;
		}
		System.out.println("\n--- All Transactions ---");
		for(Transaction t: transactions) {
			System.out.println(t); // uses the overridden toString() method in Transaction class
		}
	}

    public static void main(String[] args) {
        // This launches the JavaFX application.
        // It calls the start() method internally.
        launch(args);
    }
	
}
