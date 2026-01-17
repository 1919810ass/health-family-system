package com.healthfamily.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record HealthProfileResponse(
        String sex,
        LocalDate birthday,
        BigDecimal heightCm,
        BigDecimal weightKg,
        List<String> allergies,
        List<String> healthTags,
        Map<String, Object> lifestyle,
        Map<String, Object> goals
) {
}

