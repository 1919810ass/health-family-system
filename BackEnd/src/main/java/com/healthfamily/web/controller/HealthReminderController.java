package com.healthfamily.web.controller;

import com.healthfamily.domain.entity.User;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.HealthReminderService;
import com.healthfamily.web.dto.ReminderRequest;
import com.healthfamily.web.dto.ReminderResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reminders")
public class HealthReminderController {

    private final HealthReminderService reminderService;
    private final com.healthfamily.security.AccessGuard accessGuard;
    private final com.healthfamily.domain.repository.UserRepository userRepository;

    @PostMapping
    public Result<ReminderResponse> createReminder(@AuthenticationPrincipal UserPrincipal principal,
                                                    @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                    @Valid @RequestBody ReminderRequest request) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        // 检查用户是否有权限创建提醒
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        log.info("用户 {} 尝试创建健康提醒，角色为 {}", userId, user.getRole());
        if (!hasReminderPermission(user.getRole())) {
            log.warn("用户 {} (角色: {}) 无权创建健康提醒", userId, user.getRole());
            throw new RuntimeException("无权创建健康提醒");
        }
        log.info("用户 {} (角色: {}) 有权限创建健康提醒", userId, user.getRole());
        return Result.success(reminderService.createReminder(userId, request));
    }

    @GetMapping
    public Result<List<ReminderResponse>> getUserReminders(@AuthenticationPrincipal UserPrincipal principal,
                                                             @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                             @RequestParam(value = "status", required = false) String status,
                                                             @RequestParam(value = "familyId", required = false) Long familyId) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (familyId != null) {
            // 检查用户是否为ADMIN角色，如果是则跳过家庭访问检查
            if (user.getRole() != com.healthfamily.domain.constant.UserRole.ADMIN) {
                accessGuard.assertFamilyAccess(userId, familyId);
            }
            // 简单返回家庭范围内的提醒，按计划时间升序
            return Result.success(reminderService.getFamilyReminders(userId, familyId));
        }
        return Result.success(reminderService.getUserReminders(userId, status));
    }

    @PutMapping("/{id}/status")
    public Result<ReminderResponse> updateStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                   @PathVariable("id") Long reminderId,
                                                   @RequestBody Map<String, String> request) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        String status = request.get("status");
        return Result.success(reminderService.updateReminderStatus(userId, reminderId, status));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReminder(@AuthenticationPrincipal UserPrincipal principal,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                        @PathVariable("id") Long reminderId) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        reminderService.deleteReminder(userId, reminderId);
        return Result.success();
    }

    @PostMapping("/generate")
    public Result<List<ReminderResponse>> generateSmartReminders(@AuthenticationPrincipal UserPrincipal principal,
                                                                    @RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        // 检查用户是否有权限生成智能提醒
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        log.info("用户 {} 尝试生成智能健康提醒，角色为 {}", userId, user.getRole());
        if (!hasReminderPermission(user.getRole())) {
            log.warn("用户 {} (角色: {}) 无权生成健康提醒", userId, user.getRole());
            throw new RuntimeException("无权生成健康提醒");
        }
        log.info("用户 {} (角色: {}) 有权限生成健康提醒", userId, user.getRole());
        return Result.success(reminderService.generateSmartReminders(userId));
    }

    @PostMapping("/generate/user/{targetUserId}")
    public Result<List<ReminderResponse>> generateSmartRemindersForUser(@AuthenticationPrincipal UserPrincipal principal,
                                                                         @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                                         @PathVariable("targetUserId") Long targetUserId) {
        Long adminId = principal != null ? principal.getUserId() : userHeader;
        // 检查用户是否有权限为其他用户生成提醒
        User user = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("用户不存在"));
        log.info("用户 {} 尝试为用户 {} 生成健康提醒，角色为 {}", adminId, targetUserId, user.getRole());
        if (!hasReminderPermission(user.getRole())) {
            log.warn("用户 {} (角色: {}) 无权为其他用户生成健康提醒", adminId, user.getRole());
            throw new RuntimeException("无权为其他用户生成健康提醒");
        }
        log.info("用户 {} (角色: {}) 有权限为其他用户生成健康提醒", adminId, user.getRole());
        return Result.success(reminderService.generateSmartRemindersForUser(adminId, targetUserId));
    }

    @GetMapping("/todo")
    public Result<List<ReminderResponse>> getUserTodoItems(@AuthenticationPrincipal UserPrincipal principal,
                                                           @RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        // 普通用户可以获取自己的待办事项（包括待发送和已发送未确认的）
        return Result.success(reminderService.getUserTodoReminders(userId));
    }

    @PostMapping("/generate/collaboration")
    public Result<List<ReminderResponse>> generateCollaborationReminders(@AuthenticationPrincipal UserPrincipal principal,
                                                                         @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                                         @RequestParam("familyId") Long familyId) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        // 检查用户是否有权限生成协作提醒
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        log.info("用户 {} 尝试生成协作提醒，角色为 {}", userId, user.getRole());
        if (!hasReminderPermission(user.getRole())) {
            log.warn("用户 {} (角色: {}) 无权生成协作提醒", userId, user.getRole());
            throw new RuntimeException("无权生成协作提醒");
        }
        log.info("用户 {} (角色: {}) 有权限生成协作提醒", userId, user.getRole());
        // 检查用户是否为ADMIN角色，如果是则跳过家庭访问检查
        if (user.getRole() != com.healthfamily.domain.constant.UserRole.ADMIN) {
            accessGuard.assertFamilyAccess(userId, familyId);
        }
        return Result.success(reminderService.generateSmartReminders(userId, familyId));
    }
    
    private boolean hasReminderPermission(com.healthfamily.domain.constant.UserRole userRole) {
        // 家庭管理员、医生和系统管理员角色可以访问健康提醒功能
        boolean hasPermission = userRole == com.healthfamily.domain.constant.UserRole.FAMILY_ADMIN || 
                               userRole == com.healthfamily.domain.constant.UserRole.DOCTOR ||
                               userRole == com.healthfamily.domain.constant.UserRole.ADMIN;
        log.debug("检查用户角色 {} 的权限，结果: {}", userRole, hasPermission);
        return hasPermission;
    }
    
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HealthReminderController.class);
}

