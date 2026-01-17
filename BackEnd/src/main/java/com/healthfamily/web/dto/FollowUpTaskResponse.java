package com.healthfamily.web.dto;

import java.time.LocalDateTime;

public record FollowUpTaskResponse(
        Long id,
        String title,
        String content,
        LocalDateTime scheduledTime,
        String status,     // PENDING, COMPLETED, CANCELLED
        String priority,   // HIGH, MEDIUM, LOW
        String result,     // 随访结果
        Long planId,       // 关联计划ID (可选)
        String planTitle   // 关联计划标题 (可选)
) {}
