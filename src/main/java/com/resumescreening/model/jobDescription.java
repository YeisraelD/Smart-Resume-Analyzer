package com.resumescreening.model;

//Represents a job description
//Applied oop concept: encapsulation

public class JobDescription {
    private String title;
    private String rawText;

    public JobDescription(String title, String rawText){
        this.title = title;
        this.rawText = rawText;
    }

    // setters and getters

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getRawText(){
        return rawText;
    }

    public void setRawTitle(String rawText){
        this.rawText = rawText;
    }
}
