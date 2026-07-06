package com.jobpilot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listJobsReturnsUnifiedResponse() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void createJobRequiresCompanyName() throws Exception {
        String body = "{"
                + "\"jobTitle\":\"Java Backend Intern\","
                + "\"city\":\"Shanghai\""
                + "}";

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("companyName is required"));
    }

    @Test
    void getMissingJobReturnsUnifiedNotFound() throws Exception {
        mockMvc.perform(get("/api/jobs/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"));
    }

    @Test
    void updateStatusCreatesTimelineEvent() throws Exception {
        String createBody = "{"
                + "\"companyName\":\"Status Test Co\","
                + "\"jobTitle\":\"Java Developer\","
                + "\"city\":\"Beijing\""
                + "}";

        String responseBody = mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(responseBody);
        long jobId = response.path("data").path("id").asLong();

        mockMvc.perform(put("/api/jobs/" + jobId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\",\"note\":\"Submitted resume\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPLIED"));

        mockMvc.perform(get("/api/jobs/" + jobId + "/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data[1].toStatus").value("APPLIED"));
    }

    @Test
    void listJobsSupportsSearchAndFilter() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"Filter Co\",\"jobTitle\":\"Platform Engineer\","
                                + "\"city\":\"Hangzhou\",\"status\":\"APPLIED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "platform")
                        .param("status", "APPLIED")
                        .param("city", "Hangzhou"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[0].companyName").value("Filter Co"));
    }

    @Test
    void duplicateCheckReturnsCandidates() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"Duplicate Co\",\"jobTitle\":\"Java Engineer\","
                                + "\"city\":\"Shenzhen\",\"sourcePlatform\":\"Campus\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/jobs/check-duplicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"duplicate co\",\"jobTitle\":\"JAVA ENGINEER\","
                                + "\"city\":\"Shenzhen\",\"sourcePlatform\":\"BOSS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.duplicate").value(true))
                .andExpect(jsonPath("$.data.candidates.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void deleteJobRemovesIt() throws Exception {
        String responseBody = mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"Delete Co\",\"jobTitle\":\"QA Engineer\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long jobId = objectMapper.readTree(responseBody).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/jobs/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/jobs/" + jobId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"));
    }

    @Test
    void invalidDeadlineOrderReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/jobs").param("deadlineOrder", "latest"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("deadlineOrder must be asc or desc"));
    }

    @Test
    void localFrontendOriginCanCallJobApi() throws Exception {
        mockMvc.perform(options("/api/jobs")
                        .header("Origin", "http://localhost:5500")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertEquals(
                        "http://localhost:5500",
                        result.getResponse().getHeader("Access-Control-Allow-Origin")
                ));
    }
}
