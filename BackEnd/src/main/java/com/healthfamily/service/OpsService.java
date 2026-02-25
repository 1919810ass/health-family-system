package com.healthfamily.service;

import com.healthfamily.domain.constant.SystemLogType;
import com.healthfamily.domain.entity.SystemLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Ops服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
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
    String analyzeSystemHealth();
    
    // 异常日志查询
    List<SystemLog> getRecentErrorLogs();

    // 系统维护模式
    boolean getMaintenanceMode();
    void setMaintenanceMode(boolean enable);
}
