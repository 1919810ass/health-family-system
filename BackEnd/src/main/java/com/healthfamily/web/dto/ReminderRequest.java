package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record ReminderRequest(
        @NotBlank(message = "提醒类型不能为空")
        String type,
        @NotBlank(message = "提醒标题不能为空")
        String title,
        @NotBlank(message = "提醒内容不能为空")
        String content,
        @NotNull(message = "计划时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime scheduledTime,
        String priority,
        String channel,
        Map<String, Object> triggerCondition,
        Map<String, Object> metadata,
        Long assignedToUserId,
        Long familyId
) {
}

