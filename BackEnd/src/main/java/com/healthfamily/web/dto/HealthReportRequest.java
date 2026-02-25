package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.ReportType;
import jakarta.validation.constraints.NotBlank;
/**
 * 健康报告Request
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotNull;

public record HealthReportRequest(
    @NotBlank(message = "图片URL不能为空")
    String imageUrl,
    
    ReportType reportType,
    
    String reportName
) {}
