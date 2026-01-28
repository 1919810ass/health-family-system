package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HealthReportRequest(
    @NotBlank(message = "图片URL不能为空")
    String imageUrl,
    
    ReportType reportType,
    
    String reportName
) {}
