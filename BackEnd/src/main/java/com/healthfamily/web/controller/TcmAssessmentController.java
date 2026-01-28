package com.healthfamily.web.controller;

import com.healthfamily.web.dto.Result;
import com.healthfamily.service.AssessmentService;
import com.healthfamily.web.dto.TcmPersonalizedPlanResponse;
import com.healthfamily.web.dto.ConstitutionTrendResponse;
import com.healthfamily.web.dto.FamilyTcmHealthOverviewResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/tcm-assessment")
@RequiredArgsConstructor
@Tag(name = "中医体质评估", description = "中医体质评估相关接口")
public class TcmAssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping(value = "/trend/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getTrendInsightsStream(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.healthfamily.security.UserPrincipal principal,
            @RequestParam(value = "lookbackDays", defaultValue = "90") int lookbackDays) {
        Long userId = headerUserId;
        if (userId == null && principal != null) {
            userId = principal.getUserId();
        }
        if (userId == null) {
            throw new com.healthfamily.common.exception.BusinessException(401, "未登录");
        }
        return assessmentService.getTrendInsightsStream(userId, lookbackDays);
    }

    /**
     * 获取个性化中医养生方案
     */
    @GetMapping("/personalized-plan")
    public Result<TcmPersonalizedPlanResponse> getPersonalizedPlan(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.healthfamily.security.UserPrincipal principal) {
        Long userId = headerUserId;
        if (userId == null && principal != null) {
            userId = principal.getUserId();
        }
        if (userId == null) {
            // 如果都为空，说明既没有Header也没有通过认证（理论上被Security拦截，但为了健壮性）
            throw new com.healthfamily.common.exception.BusinessException(401, "未登录");
        }
        TcmPersonalizedPlanResponse plan = assessmentService.getPersonalizedPlan(userId);
        return Result.success(plan);
    }

    /**
     * 获取体质变化趋势
     */
    @GetMapping("/trend")
    public Result<ConstitutionTrendResponse> getTrend(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.healthfamily.security.UserPrincipal principal,
            @RequestParam(value = "lookbackDays", defaultValue = "90") int lookbackDays) {
        Long userId = headerUserId;
        if (userId == null && principal != null) {
            userId = principal.getUserId();
        }
        if (userId == null) {
            throw new com.healthfamily.common.exception.BusinessException(401, "未登录");
        }
        ConstitutionTrendResponse trend = assessmentService.getConstitutionTrend(userId, lookbackDays);
        return Result.success(trend);
    }

    /**
     * 获取家庭中医健康概览
     */
    @GetMapping("/family-overview/{familyId}")
    public Result<FamilyTcmHealthOverviewResponse> getFamilyOverview(
            @PathVariable Long familyId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.healthfamily.security.UserPrincipal principal) {
        Long userId = headerUserId;
        if (userId == null && principal != null) {
            userId = principal.getUserId();
        }
        if (userId == null) {
            throw new com.healthfamily.common.exception.BusinessException(401, "未登录");
        }
        FamilyTcmHealthOverviewResponse overview = assessmentService.getFamilyHealthOverview(familyId, userId);
        return Result.success(overview);
    }
}