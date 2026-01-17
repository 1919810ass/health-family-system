package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.HealthConsultationService;
import com.healthfamily.web.dto.ConsultationRequest;
import com.healthfamily.web.dto.ConsultationResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consultation")
public class HealthConsultationController {

    private final HealthConsultationService consultationService;

    @PostMapping
    public Result<ConsultationResponse> consult(@AuthenticationPrincipal UserPrincipal principal,
                                                 @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                 @Valid @RequestBody ConsultationRequest request) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        return Result.success(consultationService.consult(userId, request));
    }

    @GetMapping("/history")
    public Result<List<ConsultationResponse>> getHistory(@AuthenticationPrincipal UserPrincipal principal,
                                                          @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                          @RequestParam(value = "sessionId", required = false) String sessionId) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        return Result.success(consultationService.getHistory(userId, sessionId));
    }

    @PostMapping("/{id}/feedback")
    public Result<Void> feedback(@AuthenticationPrincipal UserPrincipal principal,
                                 @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                 @PathVariable("id") Long consultationId,
                                 @RequestBody Map<String, Integer> request) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        Integer feedback = request.get("feedback");
        consultationService.feedback(consultationId, userId, feedback);
        return Result.success();
    }
}

