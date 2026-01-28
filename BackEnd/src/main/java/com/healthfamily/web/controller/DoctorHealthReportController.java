package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.HealthReportService;
import com.healthfamily.web.dto.HealthReportResponse;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor/reports")
@RequiredArgsConstructor
public class DoctorHealthReportController {

    private final HealthReportService healthReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result<List<HealthReportResponse>> listReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long userId) {
        return Result.success(healthReportService.getReportsForDoctor(principal.getUserId(), userId));
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result<HealthReportResponse> getReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId) {
        return Result.success(healthReportService.getReportDetailForDoctor(principal.getUserId(), reportId));
    }

    @PostMapping("/{reportId}/comment")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result<HealthReportResponse> commentReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId,
            @RequestBody Map<String, String> body) {
        String comment = body.get("comment");
        return Result.success(healthReportService.addDoctorComment(principal.getUserId(), reportId, comment));
    }
}
