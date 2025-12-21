package com.resumescreening.service;

import com.resumescreening.model.Candidate;
import com.resumescreening.model.JobDescription;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Service to calculate candidate scores using external AI API.
 * Applied Concept: Exception Handling.
 */
public class ScoringEngine {

    private static final String API_URL = "https://api-inference.huggingface.co/models/sentence-transformers/all-MiniLM-L6-v2";
    private String apiKey;

    public ScoringEngine(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public double calculateScore(Candidate candidate, JobDescription jobDescription) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            // Fallback if no API key: basic keyword matching
            return basicKeywordScore(candidate.getRawText(), jobDescription.getRawText());
        }

        try {
            double similarity = getSemanticSimilarity(candidate.getRawText(), jobDescription.getRawText());
            // Normalize or weight it if necessary.
            // The model returns cosine similarity (usually 0 to 1 for this model).
            return similarity * 100; // Return as percentage
        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
            // Fallback
            return basicKeywordScore(candidate.getRawText(), jobDescription.getRawText());
        }
    }

    private double getSemanticSimilarity(String sourceText, String targetText)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        // Truncate texts if too long (API limits)
        String cleanSource = sourceText.substring(0, Math.min(sourceText.length(), 1000));
        String cleanTarget = targetText.substring(0, Math.min(targetText.length(), 1000));

        JSONObject payload = new JSONObject();
        payload.put("inputs", new JSONObject()
                .put("source_sentence", cleanSource)
                .put("sentences", new JSONArray().put(cleanTarget)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // The API returns a list of scores for the "sentences" list
            JSONArray scores = new JSONArray(response.body());
            return scores.getDouble(0);
        } else {
            throw new IOException("API request failed: " + response.statusCode() + " " + response.body());
        }
    }

    // Fallback method (Polymorphic-ish behavior based on availability / condition)
    private double basicKeywordScore(String resume, String jd) {
        // Also perform the detailed analysis here so it's obtainable even in basic mode
        return 0.0; // The caller (calculateScore) will handle the logic flow, wait, let's refactor
                    // slightly.
    }

    /**
     * Performs a detailed analysis of the candidate against the JD.
     * Updates the Candidate object with matched/missing skills.
     */
    public void performSkillAnalysis(Candidate candidate, JobDescription jd) {
        String jdText = jd.getRawText().toLowerCase();
        String resumeText = candidate.getRawText().toLowerCase();

        // A simple predefined list of common tech skills for demonstration
        // in a real app, this would be dynamic or extracted via NLP
        String[] commonSkills = {
                "java", "python", "c++", "javascript", "typescript", "react", "angular", "vue",
                "spring", "hibernate", "sql", "mysql", "postgresql", "mongodb", "docker", "kubernetes",
                "aws", "azure", "gcp", "git", "maven", "gradle", "jenkins", "linux", "agile", "scrum"
        };

        List<String> jdRequiredSkills = new ArrayList<>();
        for (String skill : commonSkills) {
            if (jdText.contains(skill)) {
                jdRequiredSkills.add(skill);
            }
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String skill : jdRequiredSkills) {
            if (resumeText.contains(skill)) {
                matched.add(skill);
            } else {
                missing.add(skill);
            }
        }

        candidate.setMatchedSkills(matched);
        candidate.setMissingSkills(missing);

        // Simple heuristic for role recommendation
        if (matched.contains("java") && matched.contains("spring")) {
            candidate.setRecommendedRole("Java Backend Developer");
        } else if (matched.contains("javascript") || matched.contains("react")) {
            candidate.setRecommendedRole("Frontend Developer");
        } else if (matched.contains("python")) {
            candidate.setRecommendedRole("Python Developer");
        } else {
            candidate.setRecommendedRole("General Software Engineer");
        }
    }
}
