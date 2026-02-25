package com.healthfamily.web.dto;

/**
 * 健康计划GenerationResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
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
