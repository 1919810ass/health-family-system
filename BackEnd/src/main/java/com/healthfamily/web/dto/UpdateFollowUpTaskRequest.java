package com.healthfamily.web.dto;

import java.time.LocalDateTime;
/**
 * UpdateFollowUpTaskRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
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
