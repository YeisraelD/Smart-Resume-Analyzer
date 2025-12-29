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

    // Feature Icons
    @FXML
    private ImageView iconAi;
    @FXML
    private ImageView iconFair;
    @FXML
    private ImageView iconQuick;
    @FXML
    private ImageView iconSecure;

    @FXML
    public void initialize() {
        // Load logo
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/images/logo.png");
            if (is != null)
                logoView.setImage(new Image(is));
        } catch (Exception e) {
            System.err.println("Could not load logo in Welcome: " + e.getMessage());
        }

        // Load Feature Icons
        loadIcon("/images/ai.png", iconAi);
        loadIcon("/images/fair.png", iconFair);
        loadIcon("/images/quike.png", iconQuick);
        loadIcon("/images/secure.png", iconSecure);

        // Entrance animation
        rootPane.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1500), rootPane);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void loadIcon(String path, ImageView view) {
        if (view == null)
            return;
        try {
            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                view.setImage(new Image(is));
            } else {
                System.err.println("Could not find icon: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error loading icon " + path + ": " + e.getMessage());
        }
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
                "-fx-background-color: linear-gradient(to right, #004080, #002d72); -fx-background-radius: 40; -fx-text-fill: white; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0, 45, 114, 0.6), 25, 0, 0, 10);");
    }

    @FXML
    private void handleExit() {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), startButton);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.play();

        startButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #002d72, #00a2a7); -fx-background-radius: 40; -fx-text-fill: white; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0, 45, 114, 0.3), 20, 0, 0, 10);");
    }
}
