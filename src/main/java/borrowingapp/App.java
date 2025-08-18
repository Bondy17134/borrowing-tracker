package borrowingapp;

import java.util.Scanner;

public class App {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		// declare variables
		double balance = 0.0;
		
		boolean running = true;
		while(running) {
			System.out.println("--- Borrowing Tracker ---");
			System.out.println("1. Add a transaction");
			System.out.println("2. Show current balance");
			System.out.println("3. Exit");
			
			// read the user's choice
			int choice = -1;
			try {
				choice = scanner.nextInt();
			} catch(java.util.InputMismatchException e) {
				System.out.println("Invalid input. Please enter a number");
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
					
					// Update the balance and add the transaction to a list
					balance += amount;
					
					System.out.println("Transaction recorded. Balance updated.");
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
					running = false;
					System.out.println("Exiting application");
					break;
				default:
					System.out.println("Invalid choice. Please enter 1, 2, or 3.");
			}
		}
		scanner.close();
	}

}
