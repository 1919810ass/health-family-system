package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
