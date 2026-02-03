package com.healthfamily.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MoodRecordRequest(
        Long userId,
        Long familyId,
        String emotion,
        @NotNull(message = "情绪强度不能为空")
        @Min(value = 1, message = "情绪强度至少为1")
        @Max(value = 5, message = "情绪强度最多为5")
        Integer level,
        @Min(value = 0, message = "压力评分不能小于0")
        @Max(value = 10, message = "压力评分不能大于10")
        Integer stress,
        @Min(value = 0, message = "精力评分不能小于0")
        @Max(value = 10, message = "精力评分不能大于10")
        Integer energy,
        String note,
        String time
) {}

