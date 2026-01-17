package com.healthfamily.service.recommendation;

import com.healthfamily.domain.constant.RecommendationCategory;

public record AiRecommendationRequest(
        RecommendationCategory category,
        RecommendationContext context,
        int maxItems,
        boolean strictMode,
        String modelOverride
) {
}


