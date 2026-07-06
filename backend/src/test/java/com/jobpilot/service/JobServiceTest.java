package com.jobpilot.service;

import com.jobpilot.common.BusinessException;
import com.jobpilot.dto.CreateJobRequest;
import com.jobpilot.dto.DuplicateCheckResult;
import com.jobpilot.dto.JobQuery;
import com.jobpilot.model.JobPosting;
import com.jobpilot.model.JobStatus;
import com.jobpilot.model.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobServiceTest {
    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService();
    }

    @Test
    void createUsesDefaultsAndCreatesInitialEvent() {
        JobPosting created = jobService.create(request("Acme", "Java Engineer", "Shanghai"));

        assertEquals(JobStatus.TO_APPLY, created.getStatus());
        assertEquals(Priority.MEDIUM, created.getPriority());
        assertEquals(1, jobService.events(created.getId()).size());
        assertEquals(JobStatus.TO_APPLY, jobService.events(created.getId()).get(0).getToStatus());
    }

    @Test
    void updateCannotBypassStatusTimeline() {
        JobPosting created = jobService.create(request("Acme", "Java Engineer", "Shanghai"));
        CreateJobRequest update = request("Acme", "Senior Java Engineer", "Shanghai");
        update.setStatus(JobStatus.OFFER);

        JobPosting updated = jobService.update(created.getId(), update);

        assertEquals(JobStatus.TO_APPLY, updated.getStatus());
        assertEquals(1, jobService.events(created.getId()).size());
    }

    @Test
    void listSupportsKeywordAndStatusFilters() {
        JobPosting javaJob = jobService.create(request("Acme", "Java Engineer", "Shanghai"));
        jobService.create(request("Beta", "Frontend Engineer", "Beijing"));
        jobService.updateStatus(javaJob.getId(), JobStatus.APPLIED, "Submitted");

        JobQuery query = new JobQuery();
        query.setKeyword("java");
        query.setStatus(JobStatus.APPLIED);

        List<JobPosting> result = jobService.list(query);

        assertEquals(1, result.size());
        assertEquals(javaJob.getId(), result.get(0).getId());
    }

    @Test
    void duplicateCheckIgnoresCaseAndAllowsDifferentPlatforms() {
        CreateJobRequest existing = request("Acme Tech", "Java Engineer", "Shanghai");
        existing.setSourcePlatform("Campus");
        jobService.create(existing);

        CreateJobRequest candidate = request(" acme tech ", "JAVA ENGINEER", "Shanghai");
        candidate.setSourcePlatform("BOSS");
        DuplicateCheckResult result = jobService.checkDuplicate(candidate);

        assertTrue(result.isDuplicate());
        assertEquals(1, result.getCandidates().size());
    }

    @Test
    void duplicateCheckReturnsFalseForDifferentCity() {
        jobService.create(request("Acme", "Java Engineer", "Shanghai"));

        DuplicateCheckResult result = jobService.checkDuplicate(
                request("Acme", "Java Engineer", "Beijing")
        );

        assertFalse(result.isDuplicate());
    }

    @Test
    void missingJobThrowsBusinessException() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> jobService.get(999L)
        );

        assertEquals("JOB_NOT_FOUND", exception.getErrorCode().getCode());
    }

    private CreateJobRequest request(String companyName, String jobTitle, String city) {
        CreateJobRequest request = new CreateJobRequest();
        request.setCompanyName(companyName);
        request.setJobTitle(jobTitle);
        request.setCity(city);
        return request;
    }
}
