package com.healthfamily.web.dto;

import java.time.LocalDate;

public record HealthPlanGenerationResponse(
    String title,
    String description,
    String type, // BLOOD_PRESSURE_FOLLOWUP, DIET_MANAGEMENT, etc.
    String frequencyType, // DAILY, WEEKLY
    Integer frequencyValue,
    String targetIndicators, // JSON string
    String reminderStrategy, // JSON string
    String reasoning // Why this plan was generated
) {}
