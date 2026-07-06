package com.jobpilot.dto;

import com.jobpilot.model.JobStatus;

public class JobQuery {
    private String keyword;
    private JobStatus status;
    private String companyType;
    private String jobDirection;
    private String city;
    private String sourcePlatform;
    private String deadlineOrder;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getJobDirection() {
        return jobDirection;
    }

    public void setJobDirection(String jobDirection) {
        this.jobDirection = jobDirection;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSourcePlatform() {
        return sourcePlatform;
    }

    public void setSourcePlatform(String sourcePlatform) {
        this.sourcePlatform = sourcePlatform;
    }

    public String getDeadlineOrder() {
        return deadlineOrder;
    }

    public void setDeadlineOrder(String deadlineOrder) {
        this.deadlineOrder = deadlineOrder;
    }
}
