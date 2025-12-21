package com.resumescreening.ui;

import com.resumescreening.model.Candidate;
import com.resumescreening.model.JobDescription;
import com.resumescreening.service.ResumeParser;
import com.resumescreening.service.ScoringEngine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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
    private TextArea jdArea;
    private TextField apiKeyField;
    private TableView<Candidate> resultsTable;
    private Label statusLabel;

    private List<File> selectedFiles = new ArrayList<>();

    public GUIController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.scoringEngine = new ScoringEngine(""); // Initialize with empty key
    }

    public VBox createLayout() {
        VBox root = new VBox(20);
        root.setStyle(
                "-fx-padding: 30; -fx-alignment: top-left; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: linear-gradient(to bottom right, #f4f6f8, #e0eafc);");

        // Header
        Label header = new Label("Intelligent Resume Screener");
        header.setStyle(
                "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        // API Key Section
        VBox apiBox = new VBox(5);
        Label apiKeyLabel = new Label("Hugging Face API Key (Optional for AI scoring):");
        apiKeyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-font-size: 14px;");
        apiKeyField = new TextField();
        apiKeyField.setPromptText("Enter API Key here...");
        apiKeyField.setStyle(
                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: white; -fx-border-color: #dcdcdc;");
        apiBox.getChildren().addAll(apiKeyLabel, apiKeyField);

        // Job Description Section
        VBox jdBox = new VBox(5);
        Label jdLabel = new Label("Job Description:");
        jdLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-font-size: 14px;");
        jdArea = new TextArea();
        jdArea.setPromptText("Paste Job Description here...");
        jdArea.setPrefRowCount(8); // Increased Height
        jdArea.setWrapText(true);
        jdArea.setStyle(
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dcdcdc; -fx-control-inner-background: white; -fx-font-size: 13px;");
        jdBox.getChildren().addAll(jdLabel, jdArea);

        // Controls
        HBox controls = new HBox(15);
        controls.setStyle("-fx-padding: 10 0;");

        Button uploadButton = new Button("Upload Resumes");
        uploadButton.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 12 25; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 1);");
        uploadButton.setOnAction(e -> handleFileUpload());

        Button clearButton = new Button("Clear");
        clearButton.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 12 25; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        clearButton.setOnAction(e -> handleClear());

        Button analyzeButton = new Button("Analyze Candidates");
        analyzeButton.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 1);");
        analyzeButton.setOnAction(e -> handleAnalysis());

        statusLabel = new Label("No resumes selected.");
        statusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-padding: 12 0; -fx-font-size: 13px;");

        controls.getChildren().addAll(uploadButton, clearButton, analyzeButton, statusLabel);

        // Results Table
        Label tableLabel = new Label("Analysis Results:");
        tableLabel
                .setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10 0 5 0;");

        resultsTable = new TableView<>();
        resultsTable.setStyle(
                "-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 0); -fx-background-radius: 5; -fx-border-radius: 5;");
        resultsTable.setPrefHeight(250); // Fixed initial height, but allows resizing

        TableColumn<Candidate, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold; -fx-font-size: 13px;");

        TableColumn<Candidate, Double> scoreCol = new TableColumn<>("Match Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("currentScore"));
        scoreCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", item));
                    if (item >= 70) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if (item >= 40) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Analytical Columns
        TableColumn<Candidate, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            double s = cellData.getValue().calculateScore();
            if (s >= 70)
                return new javafx.beans.property.SimpleStringProperty("High Match");
            if (s >= 40)
                return new javafx.beans.property.SimpleStringProperty("Potential");
            return new javafx.beans.property.SimpleStringProperty("Low Match");
        });
        statusCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Candidate, String> skillsCol = new TableColumn<>("Top Skills Found");
        skillsCol.setCellValueFactory(cellData -> {
            List<String> skills = cellData.getValue().getMatchedSkills();
            String summary = skills.isEmpty() ? "-" : String.join(", ", skills);
            return new javafx.beans.property.SimpleStringProperty(summary);
        });

        TableColumn<Candidate, String> roleCol = new TableColumn<>("Recommended Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("recommendedRole"));

        // Add columns
        resultsTable.getColumns().add(nameCol);
        resultsTable.getColumns().add(scoreCol);
        resultsTable.getColumns().add(statusCol);
        resultsTable.getColumns().add(skillsCol);
        resultsTable.getColumns().add(roleCol);

        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Detail View
        Label detailsLabel = new Label("Deep Dive Analysis:");
        detailsLabel
                .setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15 0 5 0; -fx-text-fill: #2c3e50;");

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPromptText("Select a candidate above to see full skill gap analysis...");
        detailsArea.setStyle(
                "-fx-control-inner-background: #fff; -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-border-color: #dcdcdc; -fx-border-radius: 5;");
        detailsArea.setPrefRowCount(10); // Increased Height significantly

        // Let lists expand
        VBox.setVgrow(resultsTable, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(detailsArea, javafx.scene.layout.Priority.ALWAYS);

        resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("CANDIDATE: ").append(newVal.getName()).append("\n");
                sb.append("EMAIL:     ").append(newVal.getEmail()).append("\n");
                sb.append("EXPERIENCE: ").append(newVal.getExperienceYears()).append(" Years\n");
                sb.append("--------------------------------------------------\n");
                sb.append("ROLE FIT:   ").append(newVal.getRecommendedRole()).append("\n");
                sb.append("MATCH SKILLS: ").append(newVal.getMatchedSkills()).append("\n");
                sb.append("MISSING SKILLS: ").append(newVal.getMissingSkills()).append("\n");
                detailsArea.setText(sb.toString());
            }
        });

        root.getChildren().addAll(
                header,
                apiBox,
                jdBox,
                controls,
                tableLabel, resultsTable,
                detailsLabel, detailsArea);

        return root;
    }

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
            statusLabel.setText(selectedFiles.size() + " resume(s) ready to analyze.");
        }
    }

    private void handleClear() {
        selectedFiles.clear();
        resultsTable.getItems().clear();
        statusLabel.setText("Selection cleared.");
    }

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
