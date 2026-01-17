package com.healthfamily.web.controller;

import com.healthfamily.annotation.Audit;
import com.healthfamily.domain.constant.SensitivityLevel;
import com.healthfamily.service.OpsService;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员系统配置控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/system/config")
public class AdminSystemConfigController {

    private final OpsService opsService;

    /**
     * 获取系统配置
     */
    @GetMapping
    public Result<?> getSystemConfig() {
        Map<String, Object> config = opsService.getSystemConfig();
        return Result.success(config);
    }

    /**
     * 更新系统配置
     */
    @PutMapping
    @Audit(action = "UPDATE_CONFIG", resource = "SystemConfig", sensitivity = SensitivityLevel.HIGH)
    public Result<?> updateSystemConfig(@RequestBody Map<String, Object> config) {
        opsService.updateSystemConfig(config);
        return Result.success("配置更新成功");
    }

    /**
     * 获取系统监控数据
     */
    @GetMapping("/monitoring")
    public Result<?> getSystemMonitoring() {
        Map<String, Object> monitoringData = opsService.getSystemMonitoring();
        return Result.success(monitoringData);
    }

    /**
     * 备份系统配置
     */
    @PostMapping("/backup")
    @Audit(action = "BACKUP_CONFIG", resource = "SystemConfig", sensitivity = SensitivityLevel.HIGH)
    public Result<?> backupSystemConfig() {
        opsService.backupSystemConfig();
        return Result.success("配置备份成功");
    }

    /**
     * 恢复系统配置
     */
    @PostMapping("/restore")
    @Audit(action = "RESTORE_CONFIG", resource = "SystemConfig", sensitivity = SensitivityLevel.HIGH)
    public Result<?> restoreSystemConfig(@RequestParam String backupId) {
        opsService.restoreSystemConfig(backupId);
        return Result.success("配置恢复成功");
    }

    /**
     * 重置系统配置
     */
    @PostMapping("/reset")
    @Audit(action = "RESET_CONFIG", resource = "SystemConfig", sensitivity = SensitivityLevel.CRITICAL)
    public Result<?> resetSystemConfig() {
        opsService.resetSystemConfig();
        return Result.success("配置重置成功");
    }
}