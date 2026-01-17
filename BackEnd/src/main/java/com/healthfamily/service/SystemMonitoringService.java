package com.healthfamily.service;

import com.healthfamily.web.dto.*;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SystemMonitoringService {
    // 用户活动相关
    UserActivityStatsDto getUserActivityStats(LocalDateTime startTime, LocalDateTime endTime);
    List<Map<String, Object>> getOnlineUsers();
    List<Map<String, Object>> getBehaviorAnalysis(LocalDateTime startTime, LocalDateTime endTime);
    Map<String, Object> getLoginLogs(int page, int size, String type, LocalDateTime startTime, LocalDateTime endTime);

    // 数据报告相关
    DataReportDto getDataReports(LocalDateTime startTime, LocalDateTime endTime);
    List<QualityReportItemDto> getQualityReport();

    // 自定义报告相关
    CustomReportDto generateCustomReport(Map<String, Object> config);
    void saveReportTemplate(String templateName, Map<String, Object> config);
    List<Map<String, Object>> getSavedTemplates();
    Map<String, Object> getTemplate(String templateId);
    
    void exportReport(Map<String, Object> params, HttpServletResponse response);
    
    // System Health & Alerts
    void checkSystemHealth();
    Map<String, Object> getSystemAlerts(LocalDateTime startTime, LocalDateTime endTime, int limit);
    void acknowledgeAlert(Long id);
    void resolveAlert(Long id);
}