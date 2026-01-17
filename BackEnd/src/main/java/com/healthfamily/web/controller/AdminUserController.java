package com.healthfamily.web.controller;

import com.healthfamily.annotation.Audit;
import com.healthfamily.domain.constant.SensitivityLevel;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.constant.UserRole;
import com.healthfamily.service.UserService;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    /**
     * 获取用户列表
     */
    @GetMapping
    public Result<?> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        // 调用服务层获取用户列表
        Map<String, Object> result = userService.getAdminUserList(keyword, role, status, page, size, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @Audit(action = "CREATE_USER", resource = "User", sensitivity = SensitivityLevel.HIGH)
    public Result<User> createUser(@RequestBody User user) {
        User createdUser = userService.create(user);
        return Result.success(createdUser);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @Audit(action = "UPDATE_USER", resource = "User")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.update(id, user);
        if (updatedUser == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(updatedUser);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Audit(action = "DELETE_USER", resource = "User", sensitivity = SensitivityLevel.HIGH)
    public Result<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "用户不存在");
        }
        return Result.success("删除成功");
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    @Audit(action = "UPDATE_USER_STATUS", resource = "User", sensitivity = SensitivityLevel.HIGH)
    public Result<?> updateUserStatus(@PathVariable Long id,
                                      @RequestParam(required = false) String status,
                                      @RequestBody(required = false) Map<String, Object> body) {
        String resolved = status;
        if ((resolved == null || resolved.isBlank()) && body != null && body.get("status") != null) {
            resolved = String.valueOf(body.get("status"));
        }
        if (resolved == null || resolved.isBlank()) {
            return Result.error(400, "缺少status参数");
        }
        boolean updated = userService.updateStatus(id, resolved);
        if (!updated) {
            return Result.error(404, "用户不存在");
        }
        return Result.success("状态更新成功");
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/password")
    @Audit(action = "RESET_PASSWORD", resource = "User", sensitivity = SensitivityLevel.HIGH)
    public Result<?> resetUserPassword(@PathVariable Long id, @RequestParam String password) {
        boolean updated = userService.resetPassword(id, password);
        if (!updated) {
            return Result.error(404, "用户不存在");
        }
        return Result.success("密码重置成功");
    }

    /**
     * 强制下线用户
     */
    @PostMapping("/{id}/logout")
    @Audit(action = "FORCE_LOGOUT", resource = "User", sensitivity = SensitivityLevel.HIGH)
    public Result<?> forceLogout(@PathVariable Long id) {
        boolean success = userService.forceLogout(id);
        if (!success) return Result.error(404, "用户不存在");
        return Result.success("用户已强制下线");
    }

    /**
     * 锁定用户
     */
    @PostMapping("/{id}/lock")
    @Audit(action = "LOCK_USER", resource = "User", sensitivity = SensitivityLevel.HIGH)
    public Result<?> lockUser(@PathVariable Long id) {
        boolean success = userService.lockUser(id);
        if (!success) return Result.error(404, "用户不存在");
        return Result.success("用户已锁定");
    }

    /**
     * 解锁用户
     */
    @PostMapping("/{id}/unlock")
    @Audit(action = "UNLOCK_USER", resource = "User", sensitivity = SensitivityLevel.HIGH)
    public Result<?> unlockUser(@PathVariable Long id) {
        boolean success = userService.unlockUser(id);
        if (!success) return Result.error(404, "用户不存在");
        return Result.success("用户已解锁");
    }

    /**
     * 审核通过用户
     */
    @PostMapping("/{id}/audit/approve")
    @Audit(action = "APPROVE_USER", resource = "User", sensitivity = SensitivityLevel.HIGH)
    public Result<?> approveUser(@PathVariable Long id) {
        boolean success = userService.auditUser(id, true, null);
        if (!success) return Result.error(404, "用户不存在");
        return Result.success("用户审核通过");
    }

    /**
     * 审核拒绝用户
     */
    @PostMapping("/{id}/audit/reject")
    @Audit(action = "REJECT_USER", resource = "User", sensitivity = SensitivityLevel.HIGH)
    public Result<?> rejectUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        boolean success = userService.auditUser(id, false, reason);
        if (!success) return Result.error(404, "用户不存在");
        return Result.success("用户审核已拒绝");
    }
}
