package com.healthfamily.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
/**
 * ExerciseRecordRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotNull;

public record ExerciseRecordRequest(
        Long userId,
        Long familyId,
        @NotBlank(message = "运动类型不能为空")
        String type,
        @NotNull(message = "运动时长不能为空")
        @Min(value = 1, message = "运动时长至少1分钟")
        Integer durationMinutes,
        Double distanceKm,
        Integer steps,
        String note
) {}
