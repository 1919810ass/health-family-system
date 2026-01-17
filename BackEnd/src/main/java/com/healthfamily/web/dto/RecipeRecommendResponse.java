package com.healthfamily.web.dto;

import java.util.List;

public record RecipeRecommendResponse(
        String title,
        List<String> items,
        String note
) {}

