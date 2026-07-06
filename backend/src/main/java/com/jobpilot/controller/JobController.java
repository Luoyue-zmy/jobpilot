package com.jobpilot.controller;

import com.jobpilot.common.ApiResponse;
import com.jobpilot.dto.CreateJobRequest;
import com.jobpilot.dto.DuplicateCheckResult;
import com.jobpilot.dto.JobQuery;
import com.jobpilot.dto.UpdateStatusRequest;
import com.jobpilot.model.ApplicationEvent;
import com.jobpilot.model.JobPosting;
import com.jobpilot.service.JobService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ApiResponse<List<JobPosting>> list(JobQuery query) {
        return ApiResponse.ok(jobService.list(query));
    }

    @PostMapping
    public ApiResponse<JobPosting> create(@RequestBody CreateJobRequest request) {
        return ApiResponse.ok(jobService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<JobPosting> get(@PathVariable Long id) {
        return ApiResponse.ok(jobService.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<JobPosting> update(@PathVariable Long id, @RequestBody CreateJobRequest request) {
        return ApiResponse.ok(jobService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<JobPosting> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        return ApiResponse.ok(jobService.updateStatus(id, request.getStatus(), request.getNote()));
    }

    @GetMapping("/{id}/events")
    public ApiResponse<List<ApplicationEvent>> events(@PathVariable Long id) {
        return ApiResponse.ok(jobService.events(id));
    }

    @PostMapping("/check-duplicate")
    public ApiResponse<DuplicateCheckResult> checkDuplicate(@RequestBody CreateJobRequest request) {
        return ApiResponse.ok(jobService.checkDuplicate(request));
    }
}
