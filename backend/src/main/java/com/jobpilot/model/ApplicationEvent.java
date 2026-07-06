package com.jobpilot.model;

import java.time.LocalDateTime;

public class ApplicationEvent {
    private Long id;
    private Long jobId;
    private JobStatus fromStatus;
    private JobStatus toStatus;
    private String note;
    private LocalDateTime eventTime;

    public ApplicationEvent() {
    }

    public ApplicationEvent(Long id, Long jobId, JobStatus fromStatus, JobStatus toStatus, String note, LocalDateTime eventTime) {
        this.id = id;
        this.jobId = jobId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.note = note;
        this.eventTime = eventTime;
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public JobStatus getFromStatus() {
        return fromStatus;
    }

    public JobStatus getToStatus() {
        return toStatus;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }
}

