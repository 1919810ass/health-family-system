package com.healthfamily.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SleepRecordRequest(
        Long userId,
        Long familyId,
        @NotNull(message = "睡眠时长不能为空")
        @Min(value = 0, message = "睡眠时长不能小于0")
        @Max(value = 24, message = "睡眠时长不能超过24小时")
        Double hours,
        Double deepHours,
        Integer wakeCount,
        String note
) {}
