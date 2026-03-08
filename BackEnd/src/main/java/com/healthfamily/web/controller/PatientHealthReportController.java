package com.healthfamily.web.controller;

import com.healthfamily.domain.entity.DoctorHealthReport;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.DoctorHealthReportService;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient/reports")
@RequiredArgsConstructor
public class PatientHealthReportController {

    private final DoctorHealthReportService doctorHealthReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'FAMILY_ADMIN', 'ADMIN')")
    public Result<List<DoctorHealthReport>> getMyReports(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(doctorHealthReportService.getReportsForPatient(principal.getUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'FAMILY_ADMIN', 'ADMIN')")
    public Result<DoctorHealthReport> getReportDetail(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        DoctorHealthReport report = doctorHealthReportService.getReport(id);
        if (report == null) {
            return Result.error(404, "Report not found");
        }
        if (!report.getUserId().equals(principal.getUserId())) {
            return Result.error(403, "Unauthorized");
        }
        
        // Mark as read if not already
        if (!report.getIsRead()) {
            doctorHealthReportService.markAsRead(id);
        }
        
        return Result.success(report);
    }
}
