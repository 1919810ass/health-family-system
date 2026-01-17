package com.healthfamily.web.controller;

import com.healthfamily.domain.entity.HealthReminder;
import com.healthfamily.service.HealthReminderService;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理员健康提醒管理控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/health/reminders")
public class AdminHealthReminderController {

    private final HealthReminderService healthReminderService;

    /**
     * 获取健康提醒列表
     */
    @GetMapping
    public Result<?> getHealthReminders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String reminderType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        // 调用服务层获取健康提醒列表
        Map<String, Object> result = healthReminderService.getAdminReminderList(userId, reminderType, status, keyword, page, size, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 根据ID获取健康提醒详情
     */
    @GetMapping("/{id}")
    public Result<HealthReminder> getHealthReminderById(@PathVariable Long id) {
        HealthReminder reminder = healthReminderService.findById(id);
        if (reminder == null) {
            return Result.error(404, "健康提醒不存在");
        }
        return Result.success(reminder);
    }

    /**
     * 创建健康提醒
     */
    @PostMapping
    public Result<HealthReminder> createHealthReminder(@RequestBody HealthReminder reminder) {
        HealthReminder createdReminder = healthReminderService.create(reminder);
        return Result.success(createdReminder);
    }

    /**
     * 更新健康提醒
     */
    @PutMapping("/{id}")
    public Result<HealthReminder> updateHealthReminder(@PathVariable Long id, @RequestBody HealthReminder reminder) {
        HealthReminder updatedReminder = healthReminderService.update(id, reminder);
        if (updatedReminder == null) {
            return Result.error(404, "健康提醒不存在");
        }
        return Result.success(updatedReminder);
    }

    /**
     * 删除健康提醒
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteHealthReminder(@PathVariable Long id) {
        boolean deleted = healthReminderService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "健康提醒不存在");
        }
        return Result.success("删除成功");
    }

    /**
     * 更新健康提醒状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateHealthReminderStatus(@PathVariable Long id, @RequestParam String status) {
        boolean updated = healthReminderService.updateStatus(id, status);
        if (!updated) {
            return Result.error(404, "健康提醒不存在");
        }
        return Result.success("状态更新成功");
    }
}