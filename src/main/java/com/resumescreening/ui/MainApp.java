package com.resumescreening.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main Entry Point for the JavaFX Application.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Intelligent Resume Screening System");

        try {
            java.io.InputStream is = getClass().getResourceAsStream("/images/logo.png");
            if (is != null) {
                // Add multiple sizes so the OS can pick the best one for the title bar (small)
                // vs taskbar (large)
                primaryStage.getIcons().addAll(
                        new Image(getClass().getResourceAsStream("/images/logo.png"), 16, 16, true, true),
                        new Image(getClass().getResourceAsStream("/images/logo.png"), 32, 32, true, true),
                        new Image(getClass().getResourceAsStream("/images/logo.png"), 64, 64, true, true),
                        new Image(getClass().getResourceAsStream("/images/logo.png"), 128, 128, true, true),
                        new Image(getClass().getResourceAsStream("/images/logo.png"), 256, 256, true, true));
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }

        try {
            // Screen Responsiveness: Detect screen size for initial window
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();

            double width = bounds.getWidth() * 0.85; // 85% of screen width
            double height = bounds.getHeight() * 0.85; // 85% of screen height

            // Load Welcome FXML (Introduction Page)
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/resumescreening/ui/WelcomeView.fxml"));
            javafx.scene.Parent root = loader.load();

            Scene scene = new Scene(root, width, height); // Dynamic Size
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900); // Prevent breaking
            primaryStage.setMinHeight(650);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
