package com.jobpilot.controller;

import com.jobpilot.common.ApiResponse;
import com.jobpilot.dto.JdParseResult;
import com.jobpilot.dto.ParseJdRequest;
import com.jobpilot.service.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/parse-jd")
    public ApiResponse<JdParseResult> parseJd(@RequestBody ParseJdRequest request) {
        return ApiResponse.ok(aiService.parseJd(request.getJdText()));
    }
}
