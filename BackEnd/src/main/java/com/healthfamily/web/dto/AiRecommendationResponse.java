package com.healthfamily.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

