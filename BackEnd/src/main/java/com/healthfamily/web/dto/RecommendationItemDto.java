package com.healthfamily.web.dto;

import java.util.List;

public record RecommendationItemDto(
        String title,
        String content,
        String priority,
        List<String> sourceTags,
        Double confidence
) {
}


