package com.jobpilot.dto;

import com.jobpilot.model.JobPosting;

import java.util.List;

public class DuplicateCheckResult {
    private final boolean duplicate;
    private final List<JobPosting> candidates;

    public DuplicateCheckResult(List<JobPosting> candidates) {
        this.candidates = candidates;
        this.duplicate = !candidates.isEmpty();
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public List<JobPosting> getCandidates() {
        return candidates;
    }
}
