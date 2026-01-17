package com.healthfamily.web.dto;

import java.util.List;
import java.util.Map;

public record DietIngestResponse(
        List<Map<String, Object>> items,
        Double totalCalories
) {}

