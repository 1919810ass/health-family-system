package com.healthfamily.service.recommendation;

import com.healthfamily.domain.entity.HealthLog;

import java.util.List;

public record RecommendationContext(
        String profile,
        String assessment,
        String logsSummary,
        String preferences,
        String logsStructured,
        List<HealthLog> recentLogs
) {
    public static RecommendationContext empty() {
        return new RecommendationContext("无", "无", "无", "无", "[]", List.of());
    }
}

