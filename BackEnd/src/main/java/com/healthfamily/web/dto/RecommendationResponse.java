package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.RecommendationCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RecommendationResponse(
        Long id,
        LocalDate forDate,
        RecommendationCategory category,
        String categoryLabel,
        String categoryTagType,
        List<RecommendationItemDto> items,
        List<String> evidence,
        RecommendationSafetyDto safety,
        String aiModel,
        String promptVersion,
        String version,
        String status,
        Boolean accepted,
        LocalDateTime createdAt
) {
}

