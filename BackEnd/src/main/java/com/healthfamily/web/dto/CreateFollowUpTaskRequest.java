package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
/**
 * CreateFollowUpTaskRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import com.fasterxml.jackson.annotation.JsonFormat;

public record CreateFollowUpTaskRequest(
        @NotBlank String title,
        String content,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime scheduledTime,
        String priority, // HIGH, MEDIUM, LOW
        Long planId      // 关联计划ID (可选)
) {}
