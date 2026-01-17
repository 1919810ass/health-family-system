package com.healthfamily.web.controller;

import com.healthfamily.domain.constant.RecommendationCategory;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.RecommendationService;
import com.healthfamily.web.dto.RecommendationFeedbackRequest;
import com.healthfamily.web.dto.RecommendationGenerateRequest;
import com.healthfamily.web.dto.RecommendationGenerateResponse;
import com.healthfamily.web.dto.RecommendationResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/generate")
    public Result<RecommendationGenerateResponse> generate(@AuthenticationPrincipal UserPrincipal principal,
                                                           @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                           @Valid @RequestBody RecommendationGenerateRequest request) {
        return Result.success(recommendationService.generate(resolveUserId(principal, userHeader), request));
    }

    @GetMapping
    public Result<List<RecommendationResponse>> list(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                     @RequestParam(value = "date", required = false)
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                     @RequestParam(value = "category", required = false) RecommendationCategory category) {
        return Result.success(recommendationService.list(resolveUserId(principal, userHeader), date, category));
    }

    @PostMapping("/{id}/feedback")
    public Result<RecommendationResponse> feedback(@AuthenticationPrincipal UserPrincipal principal,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                   @PathVariable("id") Long recommendationId,
                                                   @Valid @RequestBody RecommendationFeedbackRequest request) {
        return Result.success(recommendationService.feedback(resolveUserId(principal, userHeader), recommendationId, request));
    }

    private Long resolveUserId(UserPrincipal principal, Long userHeader) {
        if (principal == null) return userHeader;
        if (userHeader != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR") || a.getAuthority().equals("ROLE_ADMIN"))) {
            return userHeader;
        }
        return principal.getUserId();
    }
}

