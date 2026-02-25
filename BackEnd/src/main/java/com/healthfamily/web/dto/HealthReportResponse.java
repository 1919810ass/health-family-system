package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.ReportStatus;
import com.healthfamily.domain.constant.ReportType;

/**
 * 健康报告Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.time.LocalDateTime;

public record HealthReportResponse(
    Long id,
    String reportName,
    ReportType reportType,
    String imageUrl,
    ReportStatus status,
    String ocrData,
    String interpretation,
    String doctorComment,
    LocalDateTime doctorCommentTime,
    LocalDateTime createdAt
) {}
