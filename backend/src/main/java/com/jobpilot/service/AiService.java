package com.jobpilot.service;

import com.jobpilot.dto.JdParseResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AiService {
    private static final List<String> COMMON_SKILLS = Arrays.asList(
            "Java", "Spring Boot", "MySQL", "Redis", "REST API"
    );

    public JdParseResult parseJd(String jdText) {
        return new JdParseResult(
                "待确认公司",
                inferTitle(jdText),
                "待确认城市",
                "Java Backend",
                "待确认类型",
                COMMON_SKILLS,
                Arrays.asList("参与业务接口开发", "维护后端服务稳定性", "配合前端完成联调"),
                Arrays.asList("掌握 Java 基础", "了解 Spring Boot", "熟悉 SQL 和常见缓存场景"),
                Arrays.asList("复习 Java 集合与并发", "准备 Spring Boot 项目表达", "整理 MySQL 索引和事务问题")
        );
    }

    private String inferTitle(String jdText) {
        if (jdText == null || jdText.trim().isEmpty()) {
            return "待确认岗位";
        }
        if (jdText.toLowerCase().contains("ai")) {
            return "AI Application Engineer";
        }
        if (jdText.toLowerCase().contains("test")) {
            return "Test Development Engineer";
        }
        return "Java Backend Engineer";
    }
}

