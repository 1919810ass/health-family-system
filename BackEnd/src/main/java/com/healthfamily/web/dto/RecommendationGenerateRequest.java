package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.healthfamily.domain.constant.RecommendationCategory;

import java.time.LocalDate;
/**
 * 推荐GenerateRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.List;

public record RecommendationGenerateRequest(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        List<RecommendationCategory> categories,
        Integer maxItems,
        Boolean strictMode,
        String model
) {
    public RecommendationGenerateRequest {
        if (maxItems != null && maxItems < 1) {
            throw new IllegalArgumentException("maxItems must be greater than 0");
        }
    }
}


