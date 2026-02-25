package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.MonitoringService;
import com.healthfamily.web.dto.AlertResponse;
import com.healthfamily.web.dto.Result;
import com.healthfamily.web.dto.TelemetryIngestRequest;
import com.healthfamily.web.dto.ThresholdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
/**
 * 监测控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequestMapping("/api/monitor")
public class MonitoringController {

    private final MonitoringService monitoringService;

    @PostMapping("/ingest")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<AlertResponse> ingest(@AuthenticationPrincipal UserPrincipal principal,
                                        @RequestBody TelemetryIngestRequest request) {
        Long userId = principal.getUserId();
        return Result.success(monitoringService.ingest(userId, request));
    }

    @GetMapping("/alerts")
    public Result<List<AlertResponse>> alerts(@AuthenticationPrincipal UserPrincipal principal,
                                              @RequestParam(value = "familyId", required = false) Long familyId) {
        Long userId = principal.getUserId();
        return Result.success(monitoringService.getAlerts(userId, familyId));
    }

    @PutMapping("/alerts/{id}/ack")
    public Result<AlertResponse> acknowledge(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable("id") Long id) {
        Long userId = principal.getUserId();
        return Result.success(monitoringService.acknowledge(userId, id));
    }

    @GetMapping("/thresholds")
    public Result<List<ThresholdResponse>> thresholds(@AuthenticationPrincipal UserPrincipal principal,
                                                      @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        return Result.success(monitoringService.getThresholds(userId));
    }

    @PostMapping("/thresholds/optimize")
    public Result<List<ThresholdResponse>> optimize(@AuthenticationPrincipal UserPrincipal principal,
                                                    @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        return Result.success(monitoringService.optimizeThresholds(userId));
    }
}
