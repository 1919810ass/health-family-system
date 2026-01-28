package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.ReportStatus;

import java.time.LocalDateTime;

public record ReportStatusResponse(
        Long id,
        ReportStatus status,
        Integer progressPercent,
        String progressStage,
        String errorMessage,
        LocalDateTime updatedAt
) {
}
