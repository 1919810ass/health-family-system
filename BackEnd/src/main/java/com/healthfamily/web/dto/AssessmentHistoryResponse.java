package com.healthfamily.web.dto;

import java.time.LocalDateTime;
/**
 * 测评HistoryResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record AssessmentHistoryResponse(
        Long id,
        String type,
        String primaryType,
        Map<String, Double> scores,
        LocalDateTime createdAt
) {
}

