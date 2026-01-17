package com.healthfamily.service;

import com.healthfamily.domain.constant.SystemLogType;
import com.healthfamily.domain.entity.SystemLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OpsService {
    void recordOperation(Long userId, String module, String action, String detail);
    List<SystemLog> queryLogs(SystemLogType type, LocalDateTime start, LocalDateTime end, int limit);
    String analyzeLogsWithAI(SystemLogType type, LocalDateTime start, LocalDateTime end);
    Map<String, Object> systemReport(LocalDate start, LocalDate end);
    Map<String, Object> familyTrendReport(Long familyId, LocalDate start, LocalDate end);
    Map<String, Object> getSettings();
    void updateSettings(Map<String, Object> payload);
    
    // 管理员系统配置相关方法
    Map<String, Object> getSystemConfig();
    void updateSystemConfig(Map<String, Object> config);
    Map<String, Object> getSystemMonitoring();
    void backupSystemConfig();
    void restoreSystemConfig(String backupId);
    void resetSystemConfig();
    List<Map<String, Object>> getSystemConfigHistory();
}
