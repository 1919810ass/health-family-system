package com.healthfamily.web.dto;

/**
 * VitalsRecordRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotBlank;

public record VitalsRecordRequest(
        Long userId,
        Long familyId,
        @NotBlank(message = "体征类型不能为空")
        String type,
        Double value,
        Double systolic,
        Double diastolic,
        String unit,
        String note,
        String time
) {}

