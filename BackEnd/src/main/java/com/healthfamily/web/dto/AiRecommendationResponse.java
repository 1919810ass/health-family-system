package com.healthfamily.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * AI推荐Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record AiRecommendationResponse(
        Long id,
        LocalDate forDate,
        String category,
        String title,
        String content,
        String reasoning,
        String priority,
        List<String> dataSources,
        Boolean isAccepted,
        Integer feedback,
        LocalDateTime createdAt
) {
}

