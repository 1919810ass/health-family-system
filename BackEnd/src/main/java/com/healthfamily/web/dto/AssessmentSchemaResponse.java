package com.healthfamily.web.dto;

import java.util.List;
import java.util.Map;

public record AssessmentSchemaResponse(
        String type,
        String title,
        String description,
        List<Map<String, Object>> dimensions
) {
}

