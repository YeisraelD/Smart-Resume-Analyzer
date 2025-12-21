package com.resumescreening.service;

import com.resumescreening.model.Candidate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to parse resume files and extract candidate information.
 * Supports .txt and .pdf.
 */
public class ResumeParser {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+\\d{1,3}[- ]?)?\\d{10}");

    public Candidate parseResume(File file) throws IOException {
        String content;

        if (file.getName().toLowerCase().endsWith(".pdf")) {
            content = parsePdf(file);
        } else {
            content = Files.readString(file.toPath());
        }

        String name = file.getName().replace(".txt", "").replace(".pdf", ""); // Simple heuristic: filename is name
        String email = extractEmail(content);
        String phone = extractPhone(content);

        Candidate candidate = new Candidate(name, email, phone, content);
        candidate.setExperienceYears(estimateExperience(content));

        return candidate;
    }

    private String parsePdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Not Found";
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Not Found";
    }

    private int estimateExperience(String text) {
        Pattern expPattern = Pattern.compile("(\\d{1,2})\\+?\\s+years?");
        Matcher matcher = expPattern.matcher(text.toLowerCase());
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
