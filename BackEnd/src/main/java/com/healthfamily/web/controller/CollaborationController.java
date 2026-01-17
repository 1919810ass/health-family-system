package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.CollaborationService;
import com.healthfamily.web.dto.FamilyDashboardResponse;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/families")
public class CollaborationController {

    private final CollaborationService collaborationService;

    @GetMapping("/{id}/dashboard")
    public Result<FamilyDashboardResponse> dashboard(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                     @PathVariable("id") Long familyId) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(collaborationService.getFamilyDashboard(userId, familyId));
    }

    @GetMapping("/{id}/abnormal-today")
    public Result<com.healthfamily.web.dto.HomeAbnormalTodayResponse> abnormalToday(@AuthenticationPrincipal UserPrincipal principal,
                                                                                    @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                                                    @PathVariable("id") Long familyId) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(collaborationService.getAbnormalToday(userId, familyId));
    }

    @GetMapping("/{id}/health-index")
    public Result<com.healthfamily.web.dto.HomeHealthIndexResponse> healthIndex(@AuthenticationPrincipal UserPrincipal principal,
                                                                                @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                                                @PathVariable("id") Long familyId) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(collaborationService.getHealthIndex(userId, familyId));
    }

    @GetMapping("/{id}/metrics/trends")
    public Result<com.healthfamily.web.dto.HomeTrendResponse> metricTrend(@AuthenticationPrincipal UserPrincipal principal,
                                                                          @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                                          @PathVariable("id") Long familyId,
                                                                          @org.springframework.web.bind.annotation.RequestParam(name = "metric") String metric,
                                                                          @org.springframework.web.bind.annotation.RequestParam(name = "period", defaultValue = "month") String period) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(collaborationService.getMetricTrend(userId, familyId, metric, period));
    }

    @GetMapping("/{id}/status/distribution")
    public Result<com.healthfamily.web.dto.HomeStatusDistributionResponse> statusDistribution(@AuthenticationPrincipal UserPrincipal principal,
                                                                                              @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                                                              @PathVariable("id") Long familyId) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(collaborationService.getStatusDistribution(userId, familyId));
    }

    @GetMapping("/{id}/events/recent")
    public Result<com.healthfamily.web.dto.HomeEventsResponse> recentEvents(@AuthenticationPrincipal UserPrincipal principal,
                                                                            @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                                            @PathVariable("id") Long familyId) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(collaborationService.getRecentEvents(userId, familyId));
    }

    private Long resolveUserId(UserPrincipal principal, Long userHeader) {
        if (principal != null) {
            return principal.getUserId();
        }
        if (userHeader != null) {
            return userHeader;
        }
        throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("未登录或缺少用户身份信息");
    }
}
