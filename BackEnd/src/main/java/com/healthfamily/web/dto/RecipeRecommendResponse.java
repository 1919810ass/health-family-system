package com.healthfamily.web.dto;

/**
 * RecipeRecommendResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.List;

public record RecipeRecommendResponse(
        String title,
        List<String> items,
        String note
) {}

