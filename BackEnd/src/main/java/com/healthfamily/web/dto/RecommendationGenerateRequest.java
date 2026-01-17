package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.healthfamily.domain.constant.RecommendationCategory;

import java.time.LocalDate;
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


