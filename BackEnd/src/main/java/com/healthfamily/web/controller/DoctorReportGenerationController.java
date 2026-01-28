package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
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
@RequiredArgsConstructor
public class DoctorReportGenerationController {

    private static final MediaType DOCX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private static final MediaType ZIP = MediaType.parseMediaType("application/zip");

    private final HealthReportService healthReportService;

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
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
    public Result<ReportGenerationPreviewResponse> preview(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid GenerateReportRequest request) {
        return Result.success(healthReportService.generateReportPreview(principal.getUserId(), request));
    }

    @PostMapping(value = "/stream-preview", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Flux<String> streamPreview(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid GenerateReportRequest request) {
        return healthReportService.streamReportPreview(principal.getUserId(), request);
    }
}
