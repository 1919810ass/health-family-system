package com.healthfamily.web.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record AssessmentHistoryResponse(
        Long id,
        String type,
        String primaryType,
        Map<String, Double> scores,
        LocalDateTime createdAt
) {
}

