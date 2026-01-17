package com.healthfamily.web.controller;

import com.healthfamily.domain.entity.User;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.DoctorService;
import com.healthfamily.web.dto.Result;
import com.healthfamily.web.dto.AdminDoctorDto;
import com.healthfamily.web.dto.DoctorCertificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理员医生管理控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/doctors")
public class AdminDoctorController {

    private final DoctorService doctorService;

    /**
     * 获取医生列表
     */
    @GetMapping
    public Result<?> getDoctors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String specialty,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        // 调用服务层获取医生列表
        Map<String, Object> result = doctorService.getAdminDoctorList(keyword, status, specialty, page, size, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 根据ID获取医生详情
     */
    @GetMapping("/{id}")
    public Result<AdminDoctorDto> getDoctorById(@PathVariable Long id) {
        AdminDoctorDto doctor = doctorService.getAdminDoctorById(id);
        if (doctor == null) {
            return Result.error(404, "医生不存在");
        }
        return Result.success(doctor);
    }

    /**
     * 获取医生统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDoctorStats() {
        Map<String, Object> stats = doctorService.getAdminDoctorStats();
        return Result.success(stats);
    }

    /**
     * 获取待审核医生列表
     */
    @GetMapping("/pending")
    public Result<?> getPendingDoctors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        java.util.List<AdminDoctorDto> doctors = doctorService.getPendingDoctors(page, size);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", doctors);
        result.put("total", doctors.size()); // 简单处理，实际应返回总数
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 审核医生（通过）
     */
    @PostMapping("/{id}/approve")
    public Result<?> approveDoctor(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long adminId = principal != null ? principal.getUserId() : null;
        boolean success = doctorService.approveDoctor(id, adminId);
        if (success) {
            // 审核通过后启用医生账号
            doctorService.updateStatus(id, "1");
            return Result.success("医生认证通过");
        }
        return Result.error(500, "操作失败");
    }

    /**
     * 审核医生（拒绝）
     */
    @PostMapping("/{id}/reject")
    public Result<?> rejectDoctor(
            @PathVariable Long id,
            @RequestBody(required = false) DoctorCertificationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long adminId = principal != null ? principal.getUserId() : null;
        String reason = request != null ? request.rejectReason() : "不符合要求";
        boolean success = doctorService.rejectDoctor(id, adminId, reason);
        if (success) {
            return Result.success("医生认证已拒绝");
        }
        return Result.error(500, "操作失败");
    }

    /**
     * 创建医生
     */
    @PostMapping
    public Result<User> createDoctor(@RequestBody User user) {
        // 确保用户角色为DOCTOR
        user.setRole(com.healthfamily.domain.constant.UserRole.DOCTOR);
        User createdDoctor = doctorService.create(user);
        return Result.success(createdDoctor);
    }

    /**
     * 更新医生信息
     */
    @PutMapping("/{id}")
    public Result<User> updateDoctor(@PathVariable Long id, @RequestBody User user) {
        // 确保用户角色为DOCTOR
        user.setRole(com.healthfamily.domain.constant.UserRole.DOCTOR);
        User updatedDoctor = doctorService.update(id, user);
        if (updatedDoctor == null) {
            return Result.error(404, "医生不存在");
        }
        return Result.success(updatedDoctor);
    }

    /**
     * 删除医生
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteDoctor(@PathVariable Long id) {
        boolean deleted = doctorService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "医生不存在");
        }
        return Result.success("删除成功");
    }

    /**
     * 更新医生状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateDoctorStatus(@PathVariable Long id, @RequestParam String status) {
        boolean updated = doctorService.updateStatus(id, status);
        if (!updated) {
            return Result.error(404, "医生不存在");
        }
        return Result.success("状态更新成功");
    }

    /**
     * 更新医生认证状态
     */
    @PutMapping("/{id}/certification")
    public Result<?> updateDoctorCertification(
            @PathVariable Long id,
            @RequestParam Boolean certified) {
        boolean updated = doctorService.updateDoctorCertification(id, certified);
        if (!updated) {
            return Result.error(404, "医生不存在");
        }
        // 如果认证通过，启用账号
        if (certified) {
            doctorService.updateStatus(id, "1");
        }
        return Result.success("认证状态更新成功");
    }
}