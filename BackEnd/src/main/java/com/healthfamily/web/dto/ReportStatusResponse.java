package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.ReportStatus;

/**
 * 报告StatusResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
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
