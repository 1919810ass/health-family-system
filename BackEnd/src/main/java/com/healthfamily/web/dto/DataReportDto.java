package com.healthfamily.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DataReportDto {
    private Long totalUsers;
    private Long totalFamilies;
    private Long totalHealthLogs;
    private Long totalDoctors;
    private List<Map<String, Object>> newUserTrend;
    private List<Map<String, Object>> featureUsageStats;
    private List<Map<String, Object>> healthLogTrend;
    private List<Map<String, Object>> familyGrowthTrend;
    private List<QualityReportItemDto> qualityReport;
}