package com.healthfamily.web.dto;

/**
 * 推荐GenerateResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.List;

public record RecommendationGenerateResponse(
        List<RecommendationResponse> recommendations
) {
}


