package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.HealthReportService;
import com.healthfamily.web.dto.HealthReportResponse;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor/reports")
/**
 * 医生健康报告控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequiredArgsConstructor
public class DoctorHealthReportController {

    private final HealthReportService healthReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 查询列表
     * @param principal 当前登录用户
     * @param userId 家庭成员唯一标识
     * @return 业务返回结果
     */
    public Result<List<HealthReportResponse>> listReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long userId) {
        return Result.success(healthReportService.getReportsForDoctor(principal.getUserId(), userId));
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 获取
     * @param principal 当前登录用户
     * @param reportId 报告唯一标识
     * @return 业务返回结果
     */
    public Result<HealthReportResponse> getReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId) {
        return Result.success(healthReportService.getReportDetailForDoctor(principal.getUserId(), reportId));
    }

    @PostMapping("/{reportId}/comment")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param reportId 报告唯一标识
     * @param body 业务参数
     * @return 业务返回结果
     */
    public Result<HealthReportResponse> commentReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId,
            @RequestBody Map<String, String> body) {
        String comment = body.get("comment");
        return Result.success(healthReportService.addDoctorComment(principal.getUserId(), reportId, comment));
    }
}
