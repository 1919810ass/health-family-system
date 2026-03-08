package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.HealthReportService;
import com.healthfamily.web.dto.HealthReportRequest;
import com.healthfamily.web.dto.HealthReportResponse;
import com.healthfamily.web.dto.ReportStatusResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class HealthReportController {

    private final HealthReportService healthReportService;

    @GetMapping
    public Result<List<HealthReportResponse>> getUserReports(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(healthReportService.getUserReports(principal.getUserId()));
    }

    @PostMapping
    public Result<HealthReportResponse> submitReport(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody HealthReportRequest request) {
        return Result.success(healthReportService.submitReport(principal.getUserId(), request));
    }

    @GetMapping("/{id}")
    public Result<HealthReportResponse> getReportDetail(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Long id) {
        return Result.success(healthReportService.getReportDetail(principal.getUserId(), id));
    }

    @GetMapping("/{id}/status")
    public Result<ReportStatusResponse> getReportStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Long id) {
        return Result.success(healthReportService.getReportStatus(principal.getUserId(), id));
    }

    @PostMapping("/family/{familyId}/weekly")
    public ResponseEntity<String> generateFamilyWeeklyReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long familyId) {
        try {
            String report = healthReportService.generateFamilyWeeklyReport(principal.getUserId(), familyId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
