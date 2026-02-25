package com.healthfamily.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
/**
 * 健康画像Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
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

