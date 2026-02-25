package com.healthfamily.web.controller;

import com.healthfamily.domain.constant.SystemLogType;
import com.healthfamily.domain.entity.SystemLog;
import com.healthfamily.service.OpsService;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
/**
 * Ops管理员控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequestMapping("/api/admin/ops")
public class OpsAdminController {

    private final OpsService opsService;

    @GetMapping("/logs")
    public Result<?> logs(@RequestParam(name = "type") SystemLogType type,
                          @RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                          @RequestParam(name = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                          @RequestParam(name = "limit", defaultValue = "50") int limit) {
        return Result.success(opsService.queryLogs(type, start, end, limit));
    }

    @GetMapping("/logs/errors")
    public Result<List<SystemLog>> getErrorLogs() {
        return Result.success(opsService.getRecentErrorLogs());
    }

    @PostMapping("/logs/ai-analysis")
    public Result<String> analyze(@RequestParam(name = "type") SystemLogType type,
                                  @RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                  @RequestParam(name = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return Result.success(opsService.analyzeLogsWithAI(type, start, end));
    }

    @GetMapping("/ai-diagnose")
    public Result<String> aiDiagnose() {
        return Result.success(opsService.analyzeSystemHealth());
    }

    @GetMapping("/maintenance")
    public Result<Boolean> getMaintenanceMode() {
        return Result.success(opsService.getMaintenanceMode());
    }

    @PostMapping("/maintenance")
    public Result<Void> setMaintenanceMode(@RequestBody Map<String, Boolean> payload) {
        Boolean enable = payload.get("enable");
        if (enable != null) {
            opsService.setMaintenanceMode(enable);
        }
        return Result.success();
    }

    @GetMapping("/reports/system")
    public Result<Map<String, Object>> systemReport(@RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                    @RequestParam(name = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.success(opsService.systemReport(start, end));
    }

    @GetMapping("/reports/family-trend")
    public Result<Map<String, Object>> familyTrend(@RequestParam(name = "familyId") Long familyId,
                                                   @RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                   @RequestParam(name = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.success(opsService.familyTrendReport(familyId, start, end));
    }

    @GetMapping("/settings")
    /**
     * 获取
     * @return 业务返回结果
     */
    public Result<Map<String, Object>> getSettings() {
        return Result.success(opsService.getSettings());
    }

    @PutMapping("/settings")
    /**
     * 更新
     * @param payload 请求体数据
     * @return 业务返回结果
     */
    public Result<?> updateSettings(@RequestBody Map<String, Object> payload) {
        opsService.updateSettings(payload);
        return Result.success(true);
    }
}
