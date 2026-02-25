package com.healthfamily.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
/**
 * Custom报告Dto
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
@Builder
public class CustomReportDto {
    private String reportName;
    private List<Map<String, Object>> reportData;
    private Map<String, Object> chartData;
    private String chartType;
    private String exportUrl;
}