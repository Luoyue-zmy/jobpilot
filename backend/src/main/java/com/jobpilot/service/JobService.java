package com.jobpilot.service;

import com.jobpilot.common.BusinessException;
import com.jobpilot.common.ErrorCode;
import com.jobpilot.dto.CreateJobRequest;
import com.jobpilot.dto.DuplicateCheckResult;
import com.jobpilot.dto.JobQuery;
import com.jobpilot.model.ApplicationEvent;
import com.jobpilot.model.JobPosting;
import com.jobpilot.model.JobStatus;
import com.jobpilot.model.Priority;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final Map<Long, JobPosting> jobs = new ConcurrentHashMap<>();
    private final Map<Long, List<ApplicationEvent>> events = new ConcurrentHashMap<>();
    private final AtomicLong jobIds = new AtomicLong(1);
    private final AtomicLong eventIds = new AtomicLong(1);

    @PostConstruct
    public void seed() {
        CreateJobRequest request = new CreateJobRequest();
        request.setCompanyName("Demo Tech");
        request.setJobTitle("Java Backend Intern");
        request.setCity("Shanghai");
        request.setSourcePlatform("Campus");
        request.setCompanyType("Private");
        request.setJobDirection("Java Backend");
        request.setExtractedSkills(Arrays.asList("Java", "Spring Boot", "MySQL", "Redis"));
        request.setDeadline(LocalDateTime.now().plusDays(3));
        request.setPriority(Priority.HIGH);
        request.setNotes("Demo record for the first MVP loop.");
        create(request);
    }

    public List<JobPosting> list() {
        return list(new JobQuery());
    }

    public List<JobPosting> list(JobQuery query) {
        JobQuery effectiveQuery = query == null ? new JobQuery() : query;
        Comparator<JobPosting> comparator = buildComparator(effectiveQuery.getDeadlineOrder());

        return jobs.values().stream()
                .filter(job -> matches(job, effectiveQuery))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public Optional<JobPosting> find(Long id) {
        return Optional.ofNullable(jobs.get(id));
    }

    public JobPosting get(Long id) {
        return find(id).orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
    }

    public JobPosting create(CreateJobRequest request) {
        validateJobRequest(request);
        LocalDateTime now = LocalDateTime.now();
        JobPosting job = new JobPosting();
        job.setId(jobIds.getAndIncrement());
        apply(job, request, true);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        jobs.put(job.getId(), job);
        appendEvent(job.getId(), null, job.getStatus(), "Created job posting");
        return job;
    }

    public JobPosting update(Long id, CreateJobRequest request) {
        validateJobRequest(request);
        JobPosting job = jobs.get(id);
        if (job == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        apply(job, request, false);
        job.setUpdatedAt(LocalDateTime.now());
        return job;
    }

    public void delete(Long id) {
        if (!jobs.containsKey(id)) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        events.remove(id);
        jobs.remove(id);
    }

    public JobPosting updateStatus(Long id, JobStatus status, String note) {
        if (status == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status is required");
        }
        JobPosting job = jobs.get(id);
        if (job == null) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        JobStatus fromStatus = job.getStatus();
        if (fromStatus == status) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status is already " + status);
        }
        job.setStatus(status);
        job.setUpdatedAt(LocalDateTime.now());
        appendEvent(id, fromStatus, status, note);
        return job;
    }

    public List<ApplicationEvent> events(Long jobId) {
        if (!jobs.containsKey(jobId)) {
            throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
        }
        return events.getOrDefault(jobId, new ArrayList<>());
    }

    public Map<JobStatus, Long> statusCounts() {
        return jobs.values().stream()
                .collect(Collectors.groupingBy(JobPosting::getStatus, Collectors.counting()));
    }

    public DuplicateCheckResult checkDuplicate(CreateJobRequest request) {
        validateJobRequest(request);
        List<JobPosting> candidates = jobs.values().stream()
                .filter(job -> sameText(job.getCompanyName(), request.getCompanyName()))
                .filter(job -> sameText(job.getJobTitle(), request.getJobTitle()))
                .filter(job -> sameCityOrMissing(job.getCity(), request.getCity()))
                .sorted(Comparator.comparing(JobPosting::getUpdatedAt).reversed())
                .collect(Collectors.toList());
        return new DuplicateCheckResult(candidates);
    }

    private void validateJobRequest(CreateJobRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "request body is required");
        }
        if (isBlank(request.getCompanyName())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "companyName is required");
        }
        if (isBlank(request.getJobTitle())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "jobTitle is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void apply(JobPosting job, CreateJobRequest request, boolean includeStatus) {
        job.setCompanyName(request.getCompanyName().trim());
        job.setJobTitle(request.getJobTitle().trim());
        job.setCity(trimToNull(request.getCity()));
        job.setJobUrl(request.getJobUrl());
        job.setSourcePlatform(trimToNull(request.getSourcePlatform()));
        job.setCompanyType(trimToNull(request.getCompanyType()));
        job.setJobDirection(trimToNull(request.getJobDirection()));
        job.setJdText(request.getJdText());
        job.setExtractedSkills(request.getExtractedSkills() == null
                ? new ArrayList<>()
                : new ArrayList<>(request.getExtractedSkills()));
        job.setDeadline(request.getDeadline());
        if (includeStatus && request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            job.setPriority(request.getPriority());
        }
        job.setNotes(request.getNotes());
    }

    private boolean matches(JobPosting job, JobQuery query) {
        return matchesKeyword(job, query.getKeyword())
                && (query.getStatus() == null || job.getStatus() == query.getStatus())
                && matchesText(job.getCompanyType(), query.getCompanyType())
                && matchesText(job.getJobDirection(), query.getJobDirection())
                && matchesText(job.getCity(), query.getCity())
                && matchesText(job.getSourcePlatform(), query.getSourcePlatform());
    }

    private boolean matchesKeyword(JobPosting job, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        String normalizedKeyword = normalize(keyword);
        return normalize(job.getCompanyName()).contains(normalizedKeyword)
                || normalize(job.getJobTitle()).contains(normalizedKeyword)
                || normalize(job.getNotes()).contains(normalizedKeyword);
    }

    private boolean matchesText(String actual, String expected) {
        return isBlank(expected) || sameText(actual, expected);
    }

    private boolean sameText(String first, String second) {
        return normalize(first).equals(normalize(second));
    }

    private boolean sameCityOrMissing(String first, String second) {
        return isBlank(first) || isBlank(second) || sameText(first, second);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private Comparator<JobPosting> buildComparator(String deadlineOrder) {
        if (isBlank(deadlineOrder)) {
            return Comparator.comparing(JobPosting::getUpdatedAt).reversed();
        }
        if ("asc".equalsIgnoreCase(deadlineOrder)) {
            return Comparator.comparing(
                    JobPosting::getDeadline,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
        }
        if ("desc".equalsIgnoreCase(deadlineOrder)) {
            return Comparator.comparing(
                    JobPosting::getDeadline,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "deadlineOrder must be asc or desc");
    }

    private void appendEvent(Long jobId, JobStatus fromStatus, JobStatus toStatus, String note) {
        ApplicationEvent event = new ApplicationEvent(
                eventIds.getAndIncrement(),
                jobId,
                fromStatus,
                toStatus,
                note,
                LocalDateTime.now()
        );
        events.computeIfAbsent(jobId, ignored -> new CopyOnWriteArrayList<>()).add(event);
    }
}
