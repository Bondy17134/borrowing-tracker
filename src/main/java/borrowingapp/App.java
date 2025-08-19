package borrowingapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App extends Application{
	
	public void start(Stage primaryStage) {
		// creare a lable control to desplay text
		Label helloLabel = new Label("Hello, Borrowing Tracker GUI!");
		
		// create a StackPane layout. It places nodes in a back-to-front stack
		StackPane root = new StackPane();
		root.getChildren().add(helloLabel);
		
		// create a scene. a scene holds all the content of a scene graph
		// the scene graph is the hierachy of nodes that make up a JavaFX application's UI
		// parameters: root node, width, height
		Scene scene = new Scene(root, 400, 200);
		
		// set the title of the primary window (Stage)
		primaryStage.setScene(scene);
		
		// show the stage (window) to the user
		primaryStage.show();
	}
	
    public static void main(String[] args) {
        // This launches the JavaFX application.
        // It calls the start() method internally.
        launch(args);
    }
	
	
}
