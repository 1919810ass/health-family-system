package com.healthfamily.web.controller;

import com.healthfamily.service.OpsService;
import com.healthfamily.service.SystemMonitoringService;
import com.healthfamily.web.dto.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/monitoring")
public class AdminMonitoringController {

    private final OpsService opsService;
    private final SystemMonitoringService systemMonitoringService;

    /**
     * 获取API性能监控数据
     */
    @GetMapping("/api/performance")
    public Result<Map<String, Object>> getApiPerformance(
            @RequestParam(name = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        // 模拟API性能数据 - 在实际实现中，这里应该从日志或监控系统中获取真实数据
        Map<String, Object> performanceData = Map.of(
            "items", List.of(
                Map.of(
                    "endpoint", "/api/health/logs",
                    "method", "GET",
                    "calls", 1250,
                    "avgResponseTime", 120.5,
                    "errorRate", 0.2,
                    "successRate", 99.8
                ),
                Map.of(
                    "endpoint", "/api/health/logs",
                    "method", "POST",
                    "calls", 890,
                    "avgResponseTime", 150.2,
                    "errorRate", 0.1,
                    "successRate", 99.9
                ),
                Map.of(
                    "endpoint", "/api/users/profile",
                    "method", "GET",
                    "calls", 2100,
                    "avgResponseTime", 80.3,
                    "errorRate", 0.05,
                    "successRate", 99.95
                ),
                Map.of(
                    "endpoint", "/api/family/members",
                    "method", "GET",
                    "calls", 750,
                    "avgResponseTime", 200.1,
                    "errorRate", 0.3,
                    "successRate", 99.7
                ),
                Map.of(
                    "endpoint", "/api/auth/login",
                    "method", "POST",
                    "calls", 450,
                    "avgResponseTime", 300.0,
                    "errorRate", 0.5,
                    "successRate", 99.5
                )
            )
        );
        
        return Result.success(performanceData);
    }

    /**
     * 获取系统告警
     */
    @GetMapping("/alerts")
    public Result<Map<String, Object>> getSystemAlerts(
            @RequestParam(name = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        
        // 模拟系统告警数据
        Map<String, Object> alertsData = Map.of(
            "items", List.of(
                Map.of(
                    "id", 1,
                    "level", "警告",
                    "message", "数据库连接池使用率超过80%",
                    "source", "数据库监控",
                    "time", "2024-01-15 14:30:22",
                    "status", "未处理"
                ),
                Map.of(
                    "id", 2,
                    "level", "信息",
                    "message", "API响应时间超过阈值",
                    "source", "性能监控",
                    "time", "2024-01-15 14:25:15",
                    "status", "已处理"
                ),
                Map.of(
                    "id", 3,
                    "level", "错误",
                    "message", "文件存储服务异常",
                    "source", "存储监控",
                    "time", "2024-01-15 14:20:43",
                    "status", "未处理"
                ),
                Map.of(
                    "id", 4,
                    "level", "警告",
                    "message", "用户登录失败次数异常",
                    "source", "安全监控",
                    "time", "2024-01-15 14:15:33",
                    "status", "已处理"
                ),
                Map.of(
                    "id", 5,
                    "level", "信息",
                    "message", "系统负载正常",
                    "source", "系统监控",
                    "time", "2024-01-15 14:10:28",
                    "status", "已处理"
                )
            )
        );
        
        return Result.success(alertsData);
    }

    /**
     * 确认告警
     */
    @PutMapping("/alerts/{id}/acknowledge")
    public Result<?> acknowledgeAlert(@PathVariable Long id) {
        // 模拟确认告警操作
        return Result.success(Map.of("message", "告警已确认", "id", id));
    }

    /**
     * 解决告警
     */
    @PutMapping("/alerts/{id}/resolve")
    public Result<?> resolveAlert(@PathVariable Long id) {
        // 模拟解决告警操作
        return Result.success(Map.of("message", "告警已解决", "id", id));
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/system/stats")
    public Result<Map<String, Object>> getSystemStats() {
        Map<String, Object> monitoringData = opsService.getSystemMonitoring();
        return Result.success(monitoringData);
    }

    // ==================== 用户活动监控相关API ====================

    /**
     * 获取用户活动统计
     */
    @GetMapping("/user/activity/stats")
    public Result<?> getUserActivityStats(
            @RequestParam(name = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        return Result.success(systemMonitoringService.getUserActivityStats(startTime, endTime));
    }

    /**
     * 获取在线用户列表
     */
    @GetMapping("/user/online")
    public Result<List<Map<String, Object>>> getOnlineUsers() {
        return Result.success(systemMonitoringService.getOnlineUsers());
    }

    /**
     * 获取行为分析
     */
    @GetMapping("/user/behavior")
    public Result<List<Map<String, Object>>> getBehaviorAnalysis(
            @RequestParam(name = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        return Result.success(systemMonitoringService.getBehaviorAnalysis(startTime, endTime));
    }

    /**
     * 获取登录日志
     */
    @GetMapping("/user/login-logs")
    public Result<Map<String, Object>> getLoginLogs(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        return Result.success(systemMonitoringService.getLoginLogs(page, size, type, startTime, endTime));
    }

    // ==================== 数据报告相关API ====================

    /**
     * 获取数据报告
     */
    @GetMapping("/data/reports")
    public Result<?> getDataReports(
            @RequestParam(name = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        return Result.success(systemMonitoringService.getDataReports(startTime, endTime));
    }

    /**
     * 获取数据质量报告
     */
    @GetMapping("/data/quality")
    public Result<List<com.healthfamily.web.dto.QualityReportItemDto>> getQualityReport() {
        return Result.success(systemMonitoringService.getQualityReport());
    }

    // ==================== 自定义报告相关API ====================

    /**
     * 生成自定义报告
     */
    @PostMapping("/custom-reports/generate")
    public Result<?> generateCustomReport(@RequestBody Map<String, Object> config) {
        return Result.success(systemMonitoringService.generateCustomReport(config));
    }

    /**
     * 保存报告模板
     */
    @PostMapping("/custom-reports/templates")
    public Result<?> saveReportTemplate(@RequestParam String name, @RequestBody Map<String, Object> config) {
        systemMonitoringService.saveReportTemplate(name, config);
        return Result.success(Map.of("message", "模板保存成功"));
    }

    /**
     * 获取已保存的模板列表
     */
    @GetMapping("/custom-reports/templates")
    public Result<List<Map<String, Object>>> getSavedTemplates() {
        return Result.success(systemMonitoringService.getSavedTemplates());
    }

    /**
     * 加载模板
     */
    @GetMapping("/custom-reports/templates/{id}")
    public Result<Map<String, Object>> getTemplate(@PathVariable String id) {
        return Result.success(systemMonitoringService.getTemplate(id));
    }

    /**
     * 导出自定义报告
     */
    @GetMapping("/custom-reports/export")
    public void exportReport(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        systemMonitoringService.exportReport(params, response);
    }
}
