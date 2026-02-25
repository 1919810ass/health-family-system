package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.RecommendationCategory;

/**
 * AI推荐Request
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.time.LocalDate;

public record AiRecommendationRequest(
        LocalDate forDate,
        RecommendationCategory category
) {
}

