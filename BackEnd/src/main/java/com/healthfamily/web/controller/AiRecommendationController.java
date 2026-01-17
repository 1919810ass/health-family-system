package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.AiRecommendationService;
import com.healthfamily.web.dto.AiRecommendationRequest;
import com.healthfamily.web.dto.AiRecommendationResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-recommendations")
public class AiRecommendationController {

    private final AiRecommendationService recommendationService;

    @PostMapping
    public Result<AiRecommendationResponse> generateRecommendation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
            @Valid @RequestBody AiRecommendationRequest request) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        return Result.success(recommendationService.generateRecommendation(userId, request));
    }

    @GetMapping
    public Result<List<AiRecommendationResponse>> getUserRecommendations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        return Result.success(recommendationService.getUserRecommendations(userId, startDate, endDate));
    }

    @PostMapping("/{id}/feedback")
    public Result<Void> feedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
            @PathVariable("id") Long recommendationId,
            @RequestBody Map<String, Integer> request) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        Integer feedback = request.get("feedback");
        recommendationService.feedback(recommendationId, userId, feedback);
        return Result.success();
    }
}

