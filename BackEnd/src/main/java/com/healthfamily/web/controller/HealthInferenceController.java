package com.healthfamily.web.controller;

import com.healthfamily.web.dto.Result;
import com.healthfamily.service.HealthInferenceService;
import com.healthfamily.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inference")
@RequiredArgsConstructor
public class HealthInferenceController {

    private final HealthInferenceService inferenceService;

    @PostMapping("/analyze")
    public Result<String> triggerAnalysis(@AuthenticationPrincipal UserPrincipal principal,
                                          @RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        
        // 执行推理
        String reportMarkdown = inferenceService.generateCrossDomainInference(userId);
        
        return Result.success(reportMarkdown);
    }
}
