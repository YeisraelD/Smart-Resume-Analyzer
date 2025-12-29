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

    private static final String API_URL = "https://router.huggingface.co/hf-inference/models/sentence-transformers/all-MiniLM-L6-v2";
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
        String jdText = jd.toLowerCase();
        String resumeText = resume.toLowerCase();

        String[] technicalSkills = {
                "java", "python", "c++", "c#", "golang", "rust", "php", "ruby", "swift", "kotlin",
                "javascript", "typescript", "html", "css", "react", "angular", "vue", "next.js", "node.js",
                "spring", "hibernate", "django", "flask", "express", "asp.net", "laravel",
                "sql", "mysql", "postgresql", "mongodb", "redis", "oracle", "sql server",
                "docker", "kubernetes", "aws", "azure", "gcp", "terraform", "ansible",
                "git", "maven", "gradle", "jenkins", "ci/cd", "linux", "agile", "scrum", "devops",
                "machine learning", "data science", "nlp", "rest api", "graphql", "microservices"
        };

        int totalRequired = 0;
        int matched = 0;

        for (String skill : technicalSkills) {
            if (jdText.contains(skill)) {
                totalRequired++;
                if (resumeText.contains(skill)) {
                    matched++;
                }
            }
        }

        if (totalRequired == 0)
            return 50.0; // Neutral score if no specific keywords found
        return (double) matched / totalRequired * 100;
    }

    /**
     * Performs a detailed analysis of the candidate against the JD.
     * Updates the Candidate object with matched/missing skills and detailed
     * analysis.
     */
    public void performSkillAnalysis(Candidate candidate, JobDescription jd) {
        String jdText = jd.getRawText().toLowerCase();
        String resumeText = candidate.getRawText().toLowerCase();

        // Comprehensive tech skills list
        String[] technicalSkills = {
                "java", "python", "c++", "c#", "golang", "rust", "php", "ruby", "swift", "kotlin",
                "javascript", "typescript", "html", "css", "react", "angular", "vue", "next.js", "node.js",
                "spring", "hibernate", "django", "flask", "express", "asp.net", "laravel",
                "sql", "mysql", "postgresql", "mongodb", "redis", "oracle", "sql server",
                "docker", "kubernetes", "aws", "azure", "gcp", "terraform", "ansible",
                "git", "maven", "gradle", "jenkins", "ci/cd", "linux", "agile", "scrum", "devops",
                "machine learning", "data science", "nlp", "rest api", "graphql", "microservices"
        };

        // Soft skills list
        String[] softSkills = {
                "communication", "leadership", "teamwork", "problem solving", "critical thinking",
                "adaptability", "time management", "creativity", "collaboration", "management"
        };

        // Education keywords
        String[] educationKeywords = {
                "bachelor", "master", "phd", "degree", "computer science", "engineering", "bsc", "msc"
        };

        List<String> matchedTech = new ArrayList<>();
        List<String> missingTech = new ArrayList<>();
        List<String> matchedSoft = new ArrayList<>();
        List<String> allCandidateSkills = new ArrayList<>();

        // Analyze Technical Skills
        for (String skill : technicalSkills) {
            // Check if candidate has the skill regardless of JD
            if (resumeText.contains(skill)) {
                allCandidateSkills.add(skill);
            }

            // Check against JD for matching/missing analysis
            if (jdText.contains(skill)) {
                if (resumeText.contains(skill)) {
                    matchedTech.add(skill);
                } else {
                    missingTech.add(skill);
                }
            }
        }

        // Analyze Soft Skills
        for (String skill : softSkills) {
            if (resumeText.contains(skill)) {
                matchedSoft.add(skill);
            }
        }

        candidate.setMatchedSkills(matchedTech);
        candidate.setMissingSkills(missingTech);
        candidate.setMatchedSoftSkills(matchedSoft); // Store structured soft skills

        // Advanced Role Recommendation logic using ALL candidate skills
        String role = determineRole(allCandidateSkills);
        candidate.setRecommendedRole(role);

        // Build Detailed Analysis & Structured Education
        StringBuilder analysis = new StringBuilder();
        analysis.append("--- CANDIDATE ANALYSIS REPORT ---\n\n");

        analysis.append("[1. TECHNICAL STRENGTHS]\n");
        if (matchedTech.isEmpty()) {
            analysis.append("- No specific technical skills from the requirement were identified.\n");
        } else {
            for (String s : matchedTech) {
                analysis.append("- Found proficiency in: ").append(s.toUpperCase()).append("\n");
            }
        }
        analysis.append("\n");

        analysis.append("[2. SKILL GAPS]\n");
        if (missingTech.isEmpty()) {
            analysis.append("- Perfect match! No critical skill gaps identified.\n");
        } else {
            for (String s : missingTech) {
                analysis.append("- Missing requirement: ").append(s.toUpperCase()).append("\n");
            }
        }
        analysis.append("\n");

        analysis.append("[3. SOFT SKILLS & QUALITIES]\n");
        if (matchedSoft.isEmpty()) {
            analysis.append("- Standard professional qualities implied.\n");
        } else {
            analysis.append("- Demonstrated: ").append(String.join(", ", matchedSoft)).append("\n");
        }
        analysis.append("\n");

        analysis.append("[4. EXPERIENCE & EDUCATION]\n");
        analysis.append("- Estimated Experience: ").append(candidate.getExperienceYears()).append(" years.\n");
        boolean hasEdu = false;
        String detectedEdu = "Experience Based";
        for (String edu : educationKeywords) {
            if (resumeText.contains(edu)) {
                String eduNice = edu.substring(0, 1).toUpperCase() + edu.substring(1);
                analysis.append("- Education noted: ").append(eduNice).append(" detected.\n");
                detectedEdu = eduNice + " Level";
                hasEdu = true;
                break;
            }
        }
        if (!hasEdu)
            analysis.append("- Specific degree not explicitly parsed.\n");

        candidate.setEducationSummary(detectedEdu); // Store structured education
        analysis.append("\n");

        analysis.append("[5. SUMMARY RECOMMENDATION]\n");
        analysis.append(String.format("- Overall Match Score: %.1f%%\n", candidate.getCurrentScore()));

        if (candidate.getCurrentScore() >= 70) {
            analysis.append("EXCELLENT MATCH: Candidate is highly qualified for the ").append(role)
                    .append(" role. Strongly recommend for interview.");
        } else if (candidate.getCurrentScore() >= 40) {
            analysis.append(
                    "POTENTIAL MATCH: Candidate has solid foundations but some gaps. Recommended as a backup or for a junior ")
                    .append(role).append(" position.");
        } else {
            analysis.append(
                    "LOW MATCH: Candidate does not significantly meet the core requirements for this specific role.");
        }

        candidate.setAnalysisDetails(analysis.toString());
    }

    private String determineRole(List<String> matched) {
        if (matched.isEmpty())
            return "General Software Engineer";

        int backend = 0, frontend = 0, data = 0, devops = 0;

        if (matched.contains("java") || matched.contains("spring") || matched.contains("python")
                || matched.contains("node.js"))
            backend += 2;
        if (matched.contains("sql") || matched.contains("postgresql") || matched.contains("mongodb"))
            backend += 1;

        if (matched.contains("javascript") || matched.contains("react") || matched.contains("angular")
                || matched.contains("html") || matched.contains("css"))
            frontend += 2;
        if (matched.contains("typescript") || matched.contains("vue"))
            frontend += 1;

        if (matched.contains("machine learning") || matched.contains("data science") || matched.contains("nlp")
                || matched.contains("python"))
            data += 2;

        if (matched.contains("docker") || matched.contains("kubernetes") || matched.contains("aws")
                || matched.contains("jenkins") || matched.contains("devops"))
            devops += 2;

        if (data >= backend && data >= frontend && data >= devops && data > 0)
            return "Data Scientist / ML Engineer";
        if (devops >= backend && devops >= frontend && devops > 0)
            return "DevOps Engineer";
        if (backend > 0 && frontend > 0)
            return "Fullstack Developer";
        if (backend > frontend)
            return "Backend Developer";
        if (frontend > backend)
            return "Frontend Developer";

        return "Software Engineer";
    }
}
