package com.resumescreening.model;

/**
 * Represents a Job Description.
 * Applied Concept: Encapsulation.
 */
public class JobDescription {
    private String title;
    private String rawText;

    public JobDescription(String title, String rawText) {
        this.title = title;
        this.rawText = rawText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}
