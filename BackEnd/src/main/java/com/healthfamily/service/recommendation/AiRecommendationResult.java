package com.healthfamily.service.recommendation;

import java.util.List;

public record AiRecommendationResult(
        String title,
        String summary,
        List<Item> items,
        List<String> evidence,
        Safety safety,
        String raw
) {
    public record Item(
            String title,
            String content,
            String priority,
            List<String> sourceTags,
            Double confidence
    ) {
    }

    public record Safety(
            boolean refuse,
            String message
    ) {
    }
}


