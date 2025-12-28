package com.resumescreening.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WelcomeController {

    @FXML
    private StackPane rootPane;
    @FXML
    private ImageView logoView;
    @FXML
    private Button startButton;

    @FXML
    public void initialize() {
        // Load logo
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/images/logo.png");
            if (is != null) {
                logoView.setImage(new Image(is));
            }
        } catch (Exception e) {
            System.err.println("Could not load logo in Welcome: " + e.getMessage());
        }

        // Entrance animation
        rootPane.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1500), rootPane);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    @FXML
    private void handleStart() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), rootPane);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> loadMainApp());
        fadeOut.play();
    }

    private void loadMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/resumescreening/ui/ResumeAnalyzerView.fxml"));
            Parent root = loader.load();

            // Pass stage to main controller
            GUIController controller = loader.getController();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            controller.setStage(stage);

            // Inherit dimensions for responsiveness
            double currentWidth = stage.getScene().getWidth();
            double currentHeight = stage.getScene().getHeight();

            Scene scene = new Scene(root, currentWidth, currentHeight);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHover() {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), startButton);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        scaleUp.play();

        startButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #764ba2, #667eea); -fx-background-radius: 40; -fx-text-fill: white; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(102, 126, 234, 0.6), 25, 0, 0, 10);");
    }

    @FXML
    private void handleExit() {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), startButton);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.play();

        startButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-background-radius: 40; -fx-text-fill: white; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(102, 126, 234, 0.4), 20, 0, 0, 10);");
    }
}
