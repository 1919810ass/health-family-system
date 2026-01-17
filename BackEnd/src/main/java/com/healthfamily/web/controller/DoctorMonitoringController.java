package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.DoctorMonitoringService;
import com.healthfamily.web.dto.*;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctor/monitoring")
public class DoctorMonitoringController {

    private final DoctorMonitoringService doctorMonitoringService;

    /**
     * 获取增强的监测数据
     * GET /api/doctor/monitoring?familyId={familyId}
     */
    @GetMapping
    public Result<EnhancedMonitoringDataResponse> getEnhancedMonitoringData(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("familyId") Long familyId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorMonitoringService.getEnhancedMonitoringData(doctorId, familyId));
        } catch (Exception e) {
            return Result.error(500, "获取监测数据失败");
        }
    }

    /**
     * 处理异常
     * POST /api/doctor/monitoring/alerts/{alertId}/handle
     */
    @PostMapping("/alerts/{alertId}/handle")
    public Result<Void> handleAlert(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("alertId") Long alertId,
            @RequestBody HandleAlertRequest request) {
        Long doctorId = principal.getUserId();
        try {
            doctorMonitoringService.handleAlert(doctorId, alertId, request);
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "处理异常失败");
        }
    }

    /**
     * 批量处理异常
     * POST /api/doctor/monitoring/alerts/batch-handle
     */
    @PostMapping("/alerts/batch-handle")
    public Result<Void> batchHandleAlerts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody BatchHandleRequest request) {
        Long doctorId = principal.getUserId();
        try {
            doctorMonitoringService.batchHandleAlerts(doctorId, request);
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "批量处理异常失败");
        }
    }

    /**
     * 发送患者通知
     * POST /api/doctor/monitoring/notifications/send
     */
    @PostMapping("/notifications/send")
    public Result<Void> sendPatientNotification(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody SendNotificationRequest request) {
        Long doctorId = principal.getUserId();
        try {
            doctorMonitoringService.sendPatientNotification(doctorId, request);
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "发送通知失败");
        }
    }

    /**
     * 获取处理历史
     * GET /api/doctor/monitoring/handling-history?familyId={familyId}&userId={userId}
     */
    @GetMapping("/handling-history")
    public Result<List<HandlingRecordResponse>> getHandlingHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("familyId") Long familyId,
            @RequestParam(value = "userId", required = false) Long userId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorMonitoringService.getHandlingHistory(doctorId, familyId, userId));
        } catch (Exception e) {
            return Result.error(500, "获取处理历史失败");
        }
    }
}