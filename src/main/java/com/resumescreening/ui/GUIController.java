package com.resumescreening.ui;

import com.resumescreening.model.Candidate;
import com.resumescreening.model.JobDescription;
import com.resumescreening.service.ResumeParser;
import com.resumescreening.service.ScoringEngine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for the GUI. Handles event handling and business logic
 * integration.
 * Applied Concept: Objects & Classes.
 */
public class GUIController {

    private Stage primaryStage;
    private final ResumeParser resumeParser = new ResumeParser();
    private ScoringEngine scoringEngine;

    // UI Components
    @FXML
    private BorderPane root;
    @FXML
    private TextArea jdArea;
    @FXML
    private TextField apiKeyField;
    @FXML
    private TableView<Candidate> resultsTable;
    @FXML
    private TableColumn<Candidate, String> nameCol;
    @FXML
    private TableColumn<Candidate, Double> scoreCol;
    @FXML
    private TableColumn<Candidate, String> skillsCol;
    @FXML
    private TableColumn<Candidate, String> roleCol;

    @FXML
    private Label statusLabel;
    @FXML
    private Label resumeCountLabel;
    @FXML
    private ImageView logoView;

    @FXML
    private Button uploadButton;
    @FXML
    private Button analyzeButton;
    @FXML
    private Button clearButton;

    // Details Components
    @FXML
    private ScrollPane detailsScrollPane;
    @FXML
    private VBox detailsContent;

    // Chart Components
    @FXML
    private PieChart skillsChart;

    private List<File> selectedFiles = new ArrayList<>();

    public GUIController() {
        // Default constructor for FXML
        this.scoringEngine = new ScoringEngine("");
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        setupLogo();
        setupTable();
        setupSelectionListener();

        // Initialize simple state
        resumeCountLabel.setText("No resumes uploaded");
        statusLabel.setText("Ready.");
    }

    private void setupLogo() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/images/logo.png");
            if (is != null) {
                Image logo = new Image(is);
                logoView.setImage(logo);

                // --- Logo Animation ---
                ScaleTransition pulse = new ScaleTransition(Duration.millis(1200), logoView);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.05);
                pulse.setToY(1.05);
                pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }
    }

    private void setupTable() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        scoreCol.setCellValueFactory(new PropertyValueFactory<>("currentScore"));
        scoreCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setStyle(""); // Reset
                if (!empty && item != null) {
                    setText(String.format("%.1f%%", item));
                    String baseStyle = "-fx-font-weight: bold; -fx-alignment: CENTER; -fx-background-radius: 10; -fx-padding: 2 8; ";
                    if (item >= 70)
                        setStyle(baseStyle + "-fx-text-fill: #00b894; -fx-background-color: rgba(0, 184, 148, 0.1);");
                    else if (item >= 40)
                        setStyle(baseStyle + "-fx-text-fill: #fdcb6e; -fx-background-color: rgba(253, 203, 110, 0.1);");
                    else
                        setStyle(baseStyle + "-fx-text-fill: #ff7675; -fx-background-color: rgba(255, 118, 117, 0.1);");
                }
            }
        });

        skillsCol.setCellValueFactory(cellData -> {
            List<String> skills = cellData.getValue().getMatchedSkills();
            String summary = skills.isEmpty() ? "-" : String.join(", ", skills);
            return new javafx.beans.property.SimpleStringProperty(summary);
        });

        roleCol.setCellValueFactory(new PropertyValueFactory<>("recommendedRole"));
    }

    private void setupSelectionListener() {
        resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateDeepDive(newVal);
        });
    }

    // SOFT DARK HOVER HANDLERS
    @FXML
    private void handleButtonHover(MouseEvent event) {
        uploadButton.setStyle(
                "-fx-background-color: #00a8ff; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 25; -fx-border-color: #00a8ff; -fx-border-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 168, 255, 0.4), 10, 0, 0, 0);");
    }

    @FXML
    private void handleButtonExit(MouseEvent event) {
        uploadButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #00a8ff; -fx-font-weight: bold; -fx-background-radius: 25; -fx-border-color: #00a8ff; -fx-border-radius: 25; -fx-cursor: hand;");
    }

    @FXML
    private void handleAnalyzeHover(MouseEvent event) {
        analyzeButton.setStyle(
                "-fx-background-color: #9c88ff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 25; -fx-border-color: #9c88ff; -fx-border-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(156, 136, 255, 0.4), 10, 0, 0, 0);");
    }

    @FXML
    private void handleAnalyzeExit(MouseEvent event) {
        analyzeButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #9c88ff; -fx-font-weight: bold; -fx-background-radius: 25; -fx-border-color: #9c88ff; -fx-border-radius: 25; -fx-cursor: hand;");
    }

    private void updateDeepDive(Candidate c) {
        if (c != null) {
            // --- Update Cards ---
            detailsContent.getChildren().clear();

            // 1. Recommendation Card
            String recColor = c.getCurrentScore() >= 70 ? "#00b894"
                    : (c.getCurrentScore() >= 40 ? "#fdcb6e" : "#ff7675");
            detailsContent.getChildren().add(createCard("Recommendation",
                    "Role: " + c.getRecommendedRole() + "\n" +
                            (c.getCurrentScore() >= 70 ? "Highly Recommended for Interview." : "Review with Caution."),
                    recColor));

            // 2. Technical Strengths
            List<String> matched = c.getMatchedSkills();
            String matchedText = matched.isEmpty() ? "No specific matches found." : String.join(", ", matched);
            detailsContent.getChildren().add(createCard("Technical Strengths", matchedText, "#0984e3"));

            // 3. Skill Gaps
            List<String> missing = c.getMissingSkills();
            String missingText = missing.isEmpty() ? "None! Perfect Match." : String.join(", ", missing);
            detailsContent.getChildren().add(createCard("Skill Gaps", missingText, "#e17055"));

            // 4. Soft Skills
            List<String> soft = c.getMatchedSoftSkills();
            String softText = soft.isEmpty() ? "Professional qualities implied." : String.join(", ", soft);
            detailsContent.getChildren().add(createCard("Soft Skills", softText, "#6c5ce7"));

            // 5. Education & Exp
            String eduText = "Experience: " + c.getExperienceYears() + " Years\n" +
                    "Education: " + c.getEducationSummary();
            detailsContent.getChildren().add(createCard("Education & Experience", eduText, "#8e44ad"));

            // --- Update Chart ---
            int matchedCount = c.getMatchedSkills().size();
            int missingCount = c.getMissingSkills().size();

            if (matchedCount == 0 && missingCount == 0) {
                skillsChart.setData(FXCollections.observableArrayList(new PieChart.Data("No Data", 1)));
            } else {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                        new PieChart.Data("Matched", matchedCount),
                        new PieChart.Data("Missing", missingCount));
                skillsChart.setData(pieData);
            }
        } else {
            detailsContent.getChildren().clear();
            skillsChart.setData(FXCollections.observableArrayList());
        }
    }

    private VBox createCard(String title, String content, String colorHex) {
        VBox card = new VBox(5);
        // DARK CARD STYLE
        card.setStyle(
                "-fx-background-color: #161b22; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0); -fx-padding: 15; -fx-border-color: "
                        + colorHex + "; -fx-border-width: 0 0 0 2;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + colorHex + ";");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        // LIGHT TEXT FOR DARK BG
        contentLabel.setStyle("-fx-text-fill: #c9d1d9; -fx-font-size: 13px;");

        card.getChildren().addAll(titleLabel, contentLabel);
        return card;
    }

    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Resumes");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Resume Files", "*.txt", "*.pdf"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
        if (files != null) {
            selectedFiles.addAll(files);
            statusLabel.setText("Uploaded " + files.size() + " files. Total: " + selectedFiles.size());
            resumeCountLabel.setText("Uploaded: " + selectedFiles.size() + " resumes");
        }
    }

    @FXML
    private void handleClear() {
        selectedFiles.clear();
        resultsTable.getItems().clear();
        detailsContent.getChildren().clear();
        skillsChart.setData(FXCollections.observableArrayList());
        statusLabel.setText("Cleared workspace.");
        resumeCountLabel.setText("No resumes uploaded");
    }

    @FXML
    private void handleAnalysis() {
        String jdText = jdArea.getText();
        if (jdText.isEmpty()) {
            showAlert("Error", "Please enter a Job Description.");
            return;
        }
        if (selectedFiles.isEmpty()) {
            showAlert("Error", "Please upload at least one resume.");
            return;
        }

        // Update API Key
        scoringEngine.setApiKey(apiKeyField.getText().trim());
        JobDescription jd = new JobDescription("Current Role", jdText);

        ObservableList<Candidate> candidateList = FXCollections.observableArrayList();

        // Process in a rudimentary loop (Blocking UI for simplicity in this demo,
        // in real app use a background thread/Task)
        for (File file : selectedFiles) {
            try {
                Candidate c = resumeParser.parseResume(file);
                double score = scoringEngine.calculateScore(c, jd);
                c.setScore(score);

                // Perform detailed skill analysis
                scoringEngine.performSkillAnalysis(c, jd);

                candidateList.add(c);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to parse " + file.getName() + ": " + e.getMessage());
            }
        }

        // Sort by score descending
        candidateList.sort(Comparator.comparingDouble(Candidate::calculateScore).reversed());
        resultsTable.setItems(candidateList);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
