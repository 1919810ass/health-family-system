/**
 * 健康计划GenerationRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
package com.healthfamily.web.dto;

public record HealthPlanGenerationRequest(
    Long patientUserId,
    String focusArea, // e.g., "降血压", "减重", "日常保健"
    String additionalRequirements // e.g., "不吃辣", "膝盖有伤"
) {}
