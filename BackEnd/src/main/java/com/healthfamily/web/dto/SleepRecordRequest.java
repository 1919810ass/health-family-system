package com.healthfamily.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
/**
 * SleepRecordRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
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
        String note,
        // 新增字段
        String bedtime,       // 上床时间 HH:mm
        String wakeTime,      // 起床时间 HH:mm
        Integer sleepLatency, // 上床后多少时间入睡 (分钟)
        Integer wakeUpLatency // 醒来后多长时间起床 (分钟)
) {}
