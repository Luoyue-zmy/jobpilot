package com.jobpilot.dto;

import java.util.List;

public class JdParseResult {
    private String companyName;
    private String jobTitle;
    private String city;
    private String jobDirection;
    private String companyType;
    private List<String> skillTags;
    private List<String> responsibilities;
    private List<String> requirements;
    private List<String> reviewSuggestions;

    public JdParseResult(String companyName, String jobTitle, String city, String jobDirection, String companyType, List<String> skillTags, List<String> responsibilities, List<String> requirements, List<String> reviewSuggestions) {
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.city = city;
        this.jobDirection = jobDirection;
        this.companyType = companyType;
        this.skillTags = skillTags;
        this.responsibilities = responsibilities;
        this.requirements = requirements;
        this.reviewSuggestions = reviewSuggestions;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCity() {
        return city;
    }

    public String getJobDirection() {
        return jobDirection;
    }

    public String getCompanyType() {
        return companyType;
    }

    public List<String> getSkillTags() {
        return skillTags;
    }

    public List<String> getResponsibilities() {
        return responsibilities;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public List<String> getReviewSuggestions() {
        return reviewSuggestions;
    }
}

