package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.HealthReportService;
import com.healthfamily.web.dto.HealthReportRequest;
import com.healthfamily.web.dto.HealthReportResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
/**
 * 健康报告控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequiredArgsConstructor
public class HealthReportController {

    private final HealthReportService healthReportService;

    @PostMapping
    /**
     * 提交
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<HealthReportResponse> submitReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid HealthReportRequest request) {
        return Result.success(healthReportService.submitReport(principal.getUserId(), request));
    }

    @GetMapping
    /**
     * 获取
     * @param principal 当前登录用户
     * @return 业务返回结果
     */
    public Result<List<HealthReportResponse>> getUserReports(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(healthReportService.getUserReports(principal.getUserId()));
    }

    @GetMapping("/{id}")
    /**
     * 获取
     * @param principal 当前登录用户
     * @param id 业务对象唯一标识
     * @return 业务返回结果
     */
    public Result<HealthReportResponse> getReportDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return Result.success(healthReportService.getReportDetail(principal.getUserId(), id));
    }

    @GetMapping("/{id}/status")
    /**
     * 获取
     * @param principal 当前登录用户
     * @param id 业务对象唯一标识
     * @return 业务返回结果
     */
    public Result<com.healthfamily.web.dto.ReportStatusResponse> getReportStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return Result.success(healthReportService.getReportStatus(principal.getUserId(), id));
    }
}
