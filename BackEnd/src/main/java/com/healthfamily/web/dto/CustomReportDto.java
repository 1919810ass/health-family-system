package com.healthfamily.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CustomReportDto {
    private String reportName;
    private List<Map<String, Object>> reportData;
    private Map<String, Object> chartData;
    private String chartType;
    private String exportUrl;
}