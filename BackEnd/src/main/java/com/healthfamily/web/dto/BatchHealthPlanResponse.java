package com.healthfamily.web.dto;

public record BatchHealthPlanResponse(
    Long userId,
    String userName,
    HealthPlanGenerationResponse plan
) {}
