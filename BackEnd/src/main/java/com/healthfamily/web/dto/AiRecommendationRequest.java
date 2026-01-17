package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.RecommendationCategory;

import java.time.LocalDate;

public record AiRecommendationRequest(
        LocalDate forDate,
        RecommendationCategory category
) {
}

