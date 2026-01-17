package com.healthfamily.web.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record AssessmentResponse(
        Long id,
        String type,
        Map<String, Double> scores,
        String primaryType,
        Map<String, Object> report,
        LocalDateTime createdAt
) {
}

