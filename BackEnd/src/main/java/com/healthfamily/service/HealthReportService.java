package com.healthfamily.service;

import com.healthfamily.web.dto.GenerateBatchReportRequest;
import com.healthfamily.web.dto.HealthReportRequest;
import com.healthfamily.web.dto.HealthReportResponse;
import com.healthfamily.web.dto.ReportStatusResponse;
import com.healthfamily.web.dto.ReportGenerationPreviewResponse;

import java.util.List;

public interface HealthReportService {
    
    HealthReportResponse submitReport(Long userId, HealthReportRequest request);
    
    List<HealthReportResponse> getUserReports(Long userId);
    
    HealthReportResponse getReportDetail(Long userId, Long reportId);

    ReportStatusResponse getReportStatus(Long userId, Long reportId);

    HealthReportResponse getReportDetailForDoctor(Long doctorId, Long reportId);

    List<HealthReportResponse> getReportsForDoctor(Long doctorId, Long patientUserId);

    HealthReportResponse addDoctorComment(Long doctorId, Long reportId, String comment);

    byte[] generateReportDocx(Long doctorId, com.healthfamily.web.dto.GenerateReportRequest request);

    byte[] generateReportPdf(Long doctorId, com.healthfamily.web.dto.GenerateReportRequest request);

    byte[] generateBatchReportZip(Long doctorId, GenerateBatchReportRequest request);

    byte[] getReportTemplate();

    ReportGenerationPreviewResponse generateReportPreview(Long doctorId, com.healthfamily.web.dto.GenerateReportRequest request);

    reactor.core.publisher.Flux<String> streamReportPreview(Long doctorId, com.healthfamily.web.dto.GenerateReportRequest request);
}
