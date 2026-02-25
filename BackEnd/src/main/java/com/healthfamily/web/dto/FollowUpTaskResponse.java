package com.healthfamily.web.dto;

/**
 * FollowUpTaskResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
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
