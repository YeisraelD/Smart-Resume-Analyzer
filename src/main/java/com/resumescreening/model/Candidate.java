package com.resumescreening.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a job candidate.
 * Applied Concept: Inheritance (extends Person).
 */
public class Candidate extends Person {
    private List<String> skills;
    private String education;
    private int experienceYears;
    private String rawText; // The full text content of the resume
    private double currentScore; // To store the latest analysis score

    public Candidate(String name, String email, String phone, String rawText) {
        super(name, email, phone);
        this.rawText = rawText;
        this.skills = new ArrayList<>();
    }

    @Override
    public double calculateScore() {
        // In a real scenario, this might have internal scoring logic based on profile
        // completeness.
        // For this project, the scoring is primarily driven by the API comparison,
        // so we might return the externally set score or a default.
        return currentScore;
    }

    public void setScore(double score) {
        this.currentScore = score;
    }

    public double getCurrentScore() {
        return currentScore;
    }

    // Getters and Setters
    public List<String> getSkills() {
        return skills;
    }

    public void addSkill(String skill) {
        this.skills.add(skill);
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getRawText() {
        return rawText;
    }

    // Analytics Fields
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private String recommendedRole = "Generalist";
    private String analysisDetails = "";

    public String getAnalysisDetails() {
        return analysisDetails;
    }

    public void setAnalysisDetails(String analysisDetails) {
        this.analysisDetails = analysisDetails;
    }

    // New Structured Data for Cards
    private List<String> matchedSoftSkills = new ArrayList<>();
    private String educationSummary = "Not specified";

    public List<String> getMatchedSoftSkills() {
        return matchedSoftSkills;
    }

    public void setMatchedSoftSkills(List<String> matchedSoftSkills) {
        this.matchedSoftSkills = matchedSoftSkills;
    }

    public String getEducationSummary() {
        return educationSummary;
    }

    public void setEducationSummary(String educationSummary) {
        this.educationSummary = educationSummary;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getRecommendedRole() {
        return recommendedRole;
    }

    public void setRecommendedRole(String recommendedRole) {
        this.recommendedRole = recommendedRole;
    }

    @Override
    public String toString() {
        return String.format("%s (Exp: %d years) - Score: %.2f", getName(), experienceYears, currentScore);
    }
}
