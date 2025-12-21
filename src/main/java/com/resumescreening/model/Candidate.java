package com.resumescreening.model;

import java.util.ArrayList;
import java.util.List;

// represents a job candidate
//Applied concept: Inheritance (extends person )

public class Candidate extends Person {
    private List<String> skills;
    private String education;
    private int experienceYear;
    private String rawText;  // full content of the resume
    private double currentScore; // to store the latest analysis score

    public Candidate (String name, String phone,String email, String rawText){
        super(name, phone , email);
        this.rawText = rawText;
        this.skills =  new ArrayList<>();
    }

    @Override
    public double calculateScore(){
        // in a real scenario, this might have internal scoring logic based on profile
        //but, for this project year is not only parameter we are comparing, there are other factors
        // so the scoring is primarily driven by the api comparison
        return currentScore;
    }

    //getters and setters

    public void setScore(double score){
        this.currentScore = score;
    }

    public List<String> getSkills(){
        return skills;
    }

    public void addSkill(String skill){
        this.skills.add(skill);
    }

    public String getEducation(){
        return education;
    }

    public  void setEducation(String education){
        this.education = education;
    }

    public int getExperienceYears(){
        return experienceYear;
    }

    public void setExperienceYears(int experienceYear){
        this.experienceYear = experienceYear;
    }

    public String getRawText(){
        return rawText;
    } // but for this here there is no a setter method , cause the original text form file is a permanent fact.

    // Analytics felids
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private String recommendedRole = "Generalist"; // Default value , until the system analysize the person, just assume they are a generalist
                                                   // someone who does a bit of everything
    public  List<String> getMatchedSkills(){
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills){
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills(){
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills){
        this.missingSkills = missingSkills;
    }

    public String getRecommendedRole() {
        return recommendedRole;
    }

    public void setRecommendedRole(String recommendedRole){
        this.recommendedRole = recommendedRole;
    }

    @Override
    public String toString(){
        return String.format("%s (EXP: %d years) - Score: %.2f", getName(), experienceYear, currentScore);
    }



}
