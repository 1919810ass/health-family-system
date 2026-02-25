package com.healthfamily.web.dto;

import java.time.LocalDateTime;
/**
 * 提醒Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record ReminderResponse(
        Long id,
        String type,
        String title,
        String content,
        LocalDateTime scheduledTime,
        LocalDateTime actualTime,
        String status,
        String priority,
        String channel,
        Map<String, Object> metadata,
        LocalDateTime createdAt,
        Long assignedToUserId,
        String assignedToUserName,
        Long familyId,
        Long creatorId,
        String creatorName
) {
}

