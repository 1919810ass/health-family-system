package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.ReportStatus;
import com.healthfamily.domain.constant.ReportType;

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
