package com.healthfamily.web.dto;

import java.util.List;

public record RecipeRecommendRequest(
        List<String> tags,
        String mealType
) {}

