package com.jobpilot.dto;

import com.jobpilot.model.JobStatus;

public class UpdateStatusRequest {
    private JobStatus status;
    private String note;

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

