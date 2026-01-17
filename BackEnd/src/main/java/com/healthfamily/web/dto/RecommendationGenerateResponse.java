package com.healthfamily.web.dto;

import java.util.List;

public record RecommendationGenerateResponse(
        List<RecommendationResponse> recommendations
) {
}


