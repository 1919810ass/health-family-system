package com.healthfamily.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 患者详情响应DTO
 */
public record PatientDetailResponse(
        // 基础信息
        Long userId,
        String nickname,
        String phone,
        String avatar,
        Integer age,
        String sex,
        String familyName,
        String relation,
        String role,
        // 健康概览
        AssessmentSummary latestAssessment,  // 最近体质测评
        Map<String, Integer> logStatistics,  // 日志统计 { "diet": 10, "sleep": 7, ... }
        List<RecommendationSummary> recentRecommendations,  // 近期建议摘要
        List<String> healthTags,  // 健康标签（疾病/风险）
        String riskLevel  // 风险等级：LOW, MEDIUM, HIGH
) {
    public record AssessmentSummary(
            Long assessmentId,
            String primaryType,
            Double confidence,
            LocalDateTime createdAt
    ) {}

    public record RecommendationSummary(
            Long recommendationId,
            String title,
            String category,
            String priority,
            LocalDateTime createdAt
    ) {}
}

