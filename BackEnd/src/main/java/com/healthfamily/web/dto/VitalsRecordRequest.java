package com.healthfamily.web.dto;

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

