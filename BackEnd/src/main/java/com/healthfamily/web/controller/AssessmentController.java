package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.AssessmentService;
import com.healthfamily.web.dto.AssessmentHistoryResponse;
import com.healthfamily.web.dto.AssessmentResponse;
import com.healthfamily.web.dto.AssessmentSchemaResponse;
import com.healthfamily.web.dto.AssessmentSubmitRequest;
import com.healthfamily.web.dto.Result;
import com.healthfamily.web.dto.FamilyMemberLatestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/schema")
    public Result<AssessmentSchemaResponse> getSchema() {
        return Result.success(assessmentService.getSchema());
    }

    @PostMapping
    public Result<AssessmentResponse> submitAssessment(@AuthenticationPrincipal UserPrincipal principal,
                                                       @Valid @RequestBody AssessmentSubmitRequest request) {
        return Result.success(assessmentService.submitAssessment(principal.getUserId(), request));
    }

    @GetMapping("/{id}")
    public Result<AssessmentResponse> getAssessment(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable("id") Long assessmentId) {
        return Result.success(assessmentService.getAssessment(principal.getUserId(), assessmentId));
    }

    @GetMapping("/history")
    public Result<List<AssessmentHistoryResponse>> listHistory(@AuthenticationPrincipal UserPrincipal principal,
                                                               @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        if (userId == null) throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("未登录或缺少用户身份信息");
        return Result.success(assessmentService.listHistory(userId));
    }

    @GetMapping("/family/{id}/latest")
    public Result<List<FamilyMemberLatestResponse>> listFamilyLatest(@AuthenticationPrincipal UserPrincipal principal,
                                                                     @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                                     @PathVariable("id") Long familyId) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        if (userId == null) throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("未登录或缺少用户身份信息");
        return Result.success(assessmentService.listFamilyLatest(userId, familyId));
    }
}

