package com.jobpilot.controller;

import com.jobpilot.common.ApiResponse;
import com.jobpilot.model.JobStatus;
import com.jobpilot.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final JobService jobService;

    public DashboardController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        Map<JobStatus, Long> statusCounts = jobService.statusCounts();
        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        return ApiResponse.ok(Map.of(
                "totalJobs", total,
                "statusCounts", statusCounts,
                "todayTodos", 1,
                "pendingReviews", 0
        ));
    }
}
