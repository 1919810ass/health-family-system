package com.healthfamily.web.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public record UpdateFollowUpTaskRequest(
        String title,
        String content,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime scheduledTime,
        String status,    // PENDING, COMPLETED, CANCELLED
        String priority,
        String result     // 随访结果
) {}
