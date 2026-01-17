package com.healthfamily.web.controller;

import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.service.HealthLogService;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理员健康日志管理控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/health/logs")
public class AdminHealthLogController {

    private final HealthLogService healthLogService;

    /**
     * 获取健康日志列表
     */
    @GetMapping
    public Result<?> getHealthLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        // 调用服务层获取健康日志列表
        Map<String, Object> result = healthLogService.getAdminHealthLogList(userId, logType, keyword, page, size, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 获取健康日志统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getHealthLogStats() {
        Map<String, Object> stats = healthLogService.getAdminStatistics();
        return Result.success(stats);
    }

    /**
     * 根据ID获取健康日志详情
     */
    @GetMapping("/{id}")
    public Result<HealthLog> getHealthLogById(@PathVariable Long id) {
        HealthLog healthLog = healthLogService.findById(id);
        if (healthLog == null) {
            return Result.error(404, "健康日志不存在");
        }
        return Result.success(healthLog);
    }

    /**
     * 创建健康日志
     */
    @PostMapping
    public Result<HealthLog> createHealthLog(@RequestBody HealthLog healthLog) {
        HealthLog createdHealthLog = healthLogService.create(healthLog);
        return Result.success(createdHealthLog);
    }

    /**
     * 更新健康日志
     */
    @PutMapping("/{id}")
    public Result<HealthLog> updateHealthLog(@PathVariable Long id, @RequestBody HealthLog healthLog) {
        HealthLog updatedHealthLog = healthLogService.update(id, healthLog);
        if (updatedHealthLog == null) {
            return Result.error(404, "健康日志不存在");
        }
        return Result.success(updatedHealthLog);
    }

    /**
     * 删除健康日志
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteHealthLog(@PathVariable Long id) {
        boolean deleted = healthLogService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "健康日志不存在");
        }
        return Result.success("删除成功");
    }
}