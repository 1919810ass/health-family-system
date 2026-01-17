package com.healthfamily.web.dto;

import java.time.LocalDateTime;
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

