package com.healthfamily.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record TcmPersonalizedPlanResponse(
    Long id,
    Long userId,
    String primaryConstitution,
    LocalDate planDate,
    Map<String, List<PlanItemDto>> planItems,
    Map<String, String> seasonalRecommendations,
    List<String> priorityRecommendations
) {
    
    public record PlanItemDto(
        String title,
        String content,
        String difficulty,
        List<String> tags,
        List<String> contraindications
    ) {}
}