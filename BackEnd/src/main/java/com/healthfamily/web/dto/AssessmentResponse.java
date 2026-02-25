package com.healthfamily.web.dto;

import java.time.LocalDateTime;
/**
 * 测评Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
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

