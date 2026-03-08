package com.healthfamily.service;

import com.healthfamily.web.dto.GenerateBatchReportRequest;
import com.healthfamily.web.dto.GenerateReportRequest;
import com.healthfamily.web.dto.HealthReportRequest;
import com.healthfamily.web.dto.HealthReportResponse;
import com.healthfamily.web.dto.ReportGenerationPreviewResponse;
import com.healthfamily.web.dto.ReportStatusResponse;
import reactor.core.publisher.Flux;

import java.util.List;

public interface HealthReportService {
    String generateFamilyWeeklyReport(Long userId, Long familyId);

    // 用户端报告查询
    List<HealthReportResponse> getUserReports(Long userId);

    // 用户端报告操作
    HealthReportResponse submitReport(Long userId, HealthReportRequest request);
    HealthReportResponse getReportDetail(Long userId, Long reportId);
    ReportStatusResponse getReportStatus(Long userId, Long reportId);

    // 医生端报告管理
    List<HealthReportResponse> getReportsForDoctor(Long doctorId, Long userId);
    HealthReportResponse getReportDetailForDoctor(Long doctorId, Long reportId);
    HealthReportResponse addDoctorComment(Long doctorId, Long reportId, String comment);

    // 报告生成
    byte[] getReportTemplate();
    byte[] generateReportDocx(Long doctorId, GenerateReportRequest request);
    byte[] generateReportPdf(Long doctorId, GenerateReportRequest request);
    byte[] generateBatchReportZip(Long doctorId, GenerateBatchReportRequest request);
    ReportGenerationPreviewResponse generateReportPreview(Long doctorId, GenerateReportRequest request);
    Flux<String> streamReportPreview(Long doctorId, GenerateReportRequest request);
}
