package com.resumescreening.ui;

import com.resumescreening.model.Candidate;
import com.resumescreening.model.JobDescription;
import com.resumescreening.service.ResumeParser;
import com.resumescreening.service.ScoringEngine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import java.util.prefs.Preferences;

import com.resumescreening.service.PDFExportService;

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

    private final Preferences prefs = Preferences.userNodeForPackage(GUIController.class);
    private final String PREF_API_KEY = "hf_api_key";

    @FXML
    public void initialize() {
        setupLogo();
        setupTable();
        setupSelectionListener();

        // Load API Key
        String savedKey = prefs.get(PREF_API_KEY, "");
        if (!savedKey.isEmpty()) {
            apiKeyField.setText(savedKey);
            scoringEngine.setApiKey(savedKey);
        }

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

                // --- Logo Animation Removed ---
                // ScaleTransition pulse = new ScaleTransition(Duration.millis(1200), logoView);
                // pulse.setFromX(1.0);
                // pulse.setFromY(1.0);
                // pulse.setToX(1.05);
                // pulse.setToY(1.05);
                // pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
                // pulse.setAutoReverse(true);
                // pulse.play();
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
                    String baseStyle = "-fx-font-weight: bold; -fx-alignment: CENTER; -fx-background-radius: 15; -fx-padding: 5 12; -fx-font-size: 13px; -fx-border-radius: 15; -fx-border-width: 1; ";

                    if (item >= 70) {
                        // Success Green
                        setStyle(baseStyle
                                + "-fx-text-fill: #00695c; -fx-background-color: rgba(38, 166, 154, 0.2); -fx-border-color: #26a69a;");
                    } else if (item >= 40) {
                        // Warning Orange
                        setStyle(baseStyle
                                + "-fx-text-fill: #e65100; -fx-background-color: rgba(255, 167, 38, 0.2); -fx-border-color: #ffa726;");
                    } else {
                        // Danger Red
                        setStyle(baseStyle
                                + "-fx-text-fill: #c62828; -fx-background-color: rgba(239, 83, 80, 0.2); -fx-border-color: #ef5350;");
                    }
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
                "-fx-background-color: linear-gradient(to right, #00796b, #004d40); -fx-background-radius: 30; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 121, 107, 0.6), 15, 0, 0, 0);");
    }

    @FXML
    private void handleButtonExit(MouseEvent event) {
        uploadButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #4db6ac, #00796b); -fx-background-radius: 30; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void handleAnalyzeHover(MouseEvent event) {
        analyzeButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #004080, #002d72); -fx-background-radius: 30; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0, 45, 114, 0.6), 15, 0, 0, 0);");
    }

    @FXML
    private void handleAnalyzeExit(MouseEvent event) {
        analyzeButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #002d72, #00a2a7); -fx-background-radius: 30; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
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
        // BLUE-TEAL GLASS STYLE
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.85); -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0, 45, 114, 0.1), 8, 0, 0, 4); -fx-padding: 15; -fx-border-color: "
                        + colorHex + "; -fx-border-width: 0 0 0 5; -fx-border-radius: 2;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #002d72;");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        // BRAND NAVY TEXT
        contentLabel.setStyle("-fx-text-fill: #002d72; -fx-font-size: 13.5px; -fx-font-weight: 500;");

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
    private void handleRestart() {
        try {
            // Load Welcome View
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/resumescreening/ui/WelcomeView.fxml"));
            javafx.scene.Parent welcomeRoot = loader.load();

            // Get current stage
            Stage stage = (Stage) root.getScene().getWindow();

            // Set scene
            javafx.scene.Scene scene = new javafx.scene.Scene(welcomeRoot, stage.getScene().getWidth(),
                    stage.getScene().getHeight());
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not restart system: " + e.getMessage());
        }
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

        // Update API Key & Persist
        String key = apiKeyField.getText().trim();
        scoringEngine.setApiKey(key);
        if (!key.isEmpty()) {
            prefs.put(PREF_API_KEY, key);
        }

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

    @FXML
    private void handleLoadSampleData() {
        handleClear(); // Clear existing

        List<Candidate> sampleCandidates = new ArrayList<>();

        // 1. Star Candidate
        Candidate c1 = new Candidate(
                "Alice Sterling",
                "alice.sterling@example.com",
                "555-0101",
                "Experienced Senior Backend Developer with 8 years in Java, Spring Boot, Microservices, and AWS. Proven track record in high-scale systems.");
        c1.setExperienceYears(8);
        c1.setEducationSummary("Master Level");
        c1.setScore(92.5);
        // Analysis will populate skills lists based on text
        scoringEngine.performSkillAnalysis(c1,
                new JobDescription("Demo JD", "Java Spring AWS Microservices SQL Docker"));
        sampleCandidates.add(c1);

        // 2. Average Candidate
        Candidate c2 = new Candidate(
                "Bob Miller",
                "bob.miller@example.com",
                "555-0102",
                "Junior Developer with enthusiasm for Frontend technologies. Skilled in React, JavaScript, and HTML/CSS. 1 year experience.");
        c2.setExperienceYears(1);
        c2.setEducationSummary("Bachelor Level");
        c2.setScore(65.0);
        scoringEngine.performSkillAnalysis(c2, new JobDescription("Demo JD", "React TypeScript HTML CSS"));
        sampleCandidates.add(c2);

        // 3. Mismatch Candidate
        Candidate c3 = new Candidate(
                "Charlie Davis",
                "charlie.davis@example.com",
                "555-0103",
                "Marketing Specialist focusing on SEO, Content Strategy, and Social Media Management. Proficient in Google Analytics.");
        c3.setExperienceYears(4);
        c3.setEducationSummary("Bachelor Level");
        c3.setScore(15.0);
        scoringEngine.performSkillAnalysis(c3, new JobDescription("Demo JD", "Java Python SQL"));
        sampleCandidates.add(c3);

        resultsTable.setItems(FXCollections.observableArrayList(sampleCandidates));
        statusLabel.setText("Loaded 3 demo candidates.");
        resumeCountLabel.setText("Demo Mode Active");
    }

    @FXML
    private void handleExport() {
        if (resultsTable.getItems().isEmpty()) {
            showAlert("Export Error", "No data to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("SmartHire_Report.pdf");
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                new PDFExportService().exportResults(resultsTable.getItems(), file);
                showAlert("Success", "Report exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Export Failed", "Could not create PDF: " + e.getMessage());
            }
        }
    }
}
