package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.domain.entity.DoctorHealthReport;
import com.healthfamily.service.DoctorHealthReportService;
import com.healthfamily.service.HealthReportService;
import com.healthfamily.web.dto.GenerateBatchReportRequest;
import com.healthfamily.web.dto.GenerateReportRequest;
import com.healthfamily.web.dto.ReportGenerationPreviewResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/doctor/report-generation")
/**
 * 医生报告Generation控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequiredArgsConstructor
public class DoctorReportGenerationController {

    private static final MediaType DOCX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private static final MediaType ZIP = MediaType.parseMediaType("application/zip");

    private final HealthReportService healthReportService;
    private final DoctorHealthReportService doctorHealthReportService;

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result<Long> submitReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid GenerateReportRequest request) {
        
        // 1. 获取报告内容（如果 finalContent 为空，则需要生成）
        String content = request.finalContent();
        if (content == null || content.isBlank()) {
            // 如果前端没有传 finalContent，后端生成预览内容作为正文
            // 这里为了简化，直接调用 generateReportPreview 获取内容
            // 实际生产中可能需要更高效的方式
            ReportGenerationPreviewResponse preview = healthReportService.generateReportPreview(principal.getUserId(), request);
            content = preview.draftContent();
        }

        // 2. 保存报告
        String doctorName = principal.user() != null ? principal.user().getNickname() : null;
        if (doctorName == null || doctorName.isBlank()) {
            doctorName = "医生" + principal.getUserId();
        }

        DoctorHealthReport report = doctorHealthReportService.createReport(
                principal.getUserId(),
                doctorName,
                request.userId(),
                null, // familyId 暂时不传，或者需要从 request 中获取（如果 request 中有的话）
                "健康诊断报告 " + LocalDate.now(),
                request.diagnosis(),
                content,
                null // pdfUrl 暂时为空，后续可以上传到对象存储
        );

        return Result.success(report.getId());
    }

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @return 业务返回结果
     */
    public ResponseEntity<byte[]> downloadTemplate(@AuthenticationPrincipal UserPrincipal principal) {
        byte[] bytes = healthReportService.getReportTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(DOCX);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("病例报告模板.docx", StandardCharsets.UTF_8)
                .build());
        headers.setCacheControl("no-store, max-age=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    private static final MediaType PDF = MediaType.APPLICATION_PDF;

    @PostMapping("/docx")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 生成
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public ResponseEntity<byte[]> generateDocx(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid GenerateReportRequest request) {
        byte[] bytes = healthReportService.generateReportDocx(principal.getUserId(), request);

        String filename = "健康病例报告_" + LocalDate.now() + "_患者" + request.userId() + ".docx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(DOCX);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());
        headers.setCacheControl("no-store, max-age=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
    
    @PostMapping("/pdf")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 生成
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public ResponseEntity<byte[]> generatePdf(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid GenerateReportRequest request) {
        byte[] bytes = healthReportService.generateReportPdf(principal.getUserId(), request);

        String filename = "健康病例报告_" + LocalDate.now() + "_患者" + request.userId() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());
        headers.setCacheControl("no-store, max-age=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    @PostMapping("/batch-docx")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 生成
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public ResponseEntity<byte[]> generateBatchDocx(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid GenerateBatchReportRequest request) {
        byte[] bytes = healthReportService.generateBatchReportZip(principal.getUserId(), request);

        String filename = "健康病例报告_批量_" + LocalDate.now() + ".zip";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(ZIP);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());
        headers.setCacheControl("no-store, max-age=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<ReportGenerationPreviewResponse> preview(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid GenerateReportRequest request) {
        return Result.success(healthReportService.generateReportPreview(principal.getUserId(), request));
    }

    @PostMapping(value = "/stream-preview", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 执行业务操作
     * @param principal 当前登录用户
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Flux<String> streamPreview(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid GenerateReportRequest request) {
        return healthReportService.streamReportPreview(principal.getUserId(), request);
    }
}
