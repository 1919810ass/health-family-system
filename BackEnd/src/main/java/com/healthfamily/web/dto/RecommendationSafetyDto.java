package com.healthfamily.web.dto;

public record RecommendationSafetyDto(
        boolean refuse,
        String message
) {
    public static RecommendationSafetyDto safe() {
        return new RecommendationSafetyDto(false, null);
    }
}


