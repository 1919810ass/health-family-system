package com.healthfamily.web.controller;

import com.healthfamily.web.dto.Result;
import com.healthfamily.service.AssessmentService;
import com.healthfamily.web.dto.TcmPersonalizedPlanResponse;
import com.healthfamily.web.dto.ConstitutionTrendResponse;
import com.healthfamily.web.dto.FamilyTcmHealthOverviewResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tcm-assessment")
@RequiredArgsConstructor
@Tag(name = "中医体质评估", description = "中医体质评估相关接口")
public class TcmAssessmentController {

    private final AssessmentService assessmentService;

    /**
     * 获取个性化中医养生方案
     */
    @GetMapping("/personalized-plan")
    public Result<TcmPersonalizedPlanResponse> getPersonalizedPlan(@RequestHeader("X-User-Id") Long userId) {
        TcmPersonalizedPlanResponse plan = assessmentService.getPersonalizedPlan(userId);
        return Result.success(plan);
    }

    /**
     * 获取体质变化趋势
     */
    @GetMapping("/trend")
    public Result<ConstitutionTrendResponse> getTrend(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "lookbackDays", defaultValue = "90") int lookbackDays) {
        ConstitutionTrendResponse trend = assessmentService.getConstitutionTrend(userId, lookbackDays);
        return Result.success(trend);
    }

    /**
     * 获取家庭中医健康概览
     */
    @GetMapping("/family-overview/{familyId}")
    public Result<FamilyTcmHealthOverviewResponse> getFamilyOverview(
            @PathVariable Long familyId,
            @RequestHeader("X-User-Id") Long userId) {
        FamilyTcmHealthOverviewResponse overview = assessmentService.getFamilyHealthOverview(familyId, userId);
        return Result.success(overview);
    }
}