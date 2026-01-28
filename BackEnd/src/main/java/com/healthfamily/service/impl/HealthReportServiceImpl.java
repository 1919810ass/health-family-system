package com.healthfamily.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.constant.ReportStatus;
import com.healthfamily.domain.constant.ReportType;
import com.healthfamily.domain.entity.*;
import com.healthfamily.domain.repository.*;
import com.healthfamily.modules.recommendationv2.service.DocRagService;
import com.healthfamily.service.HealthReportService;
import com.healthfamily.service.ReportAnalysisService;
import com.healthfamily.web.dto.GenerateBatchReportItem;
import com.healthfamily.web.dto.GenerateBatchReportRequest;
import com.healthfamily.web.dto.GenerateReportRequest;
import com.healthfamily.web.dto.HealthReportRequest;
import com.healthfamily.web.dto.HealthReportResponse;
import com.healthfamily.web.dto.RagEvidenceDto;
import com.healthfamily.web.dto.ReportGenerationPreviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReportServiceImpl implements HealthReportService {
    private static final Pattern INLINE_CITATION_PATTERN = Pattern.compile("\\\\[(\\\\d+)]");

    private final HealthReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ConstitutionAssessmentRepository assessmentRepository;
    private final HealthLogRepository logRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final DocRagService docRagService;
    private final ObjectMapper objectMapper;
    private final ReportAnalysisService reportAnalysisService;
    
    @Qualifier("reportExecutor")
    private final ThreadPoolTaskExecutor reportExecutor;

    @Override
    // Remove @Transactional to avoid race condition with async thread
    // @Transactional 
    public HealthReportResponse submitReport(Long userId, HealthReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 如果用户提供了报告名称，则使用用户提供的；否则使用默认名称
        String reportName = (request.reportName() != null && !request.reportName().trim().isEmpty())
                ? request.reportName()
                : "上传报告_" + LocalDate.now();

        HealthReport report = HealthReport.builder()
                .user(user)
                .reportName(reportName)
                .reportType(ReportType.LAB_REPORT) // 默认为化验单，实际应从request获取类型
                .imageUrl(request.imageUrl())
                .status(ReportStatus.PENDING)
                .progressPercent(0)
                .progressStage("待处理")
                .createdAt(LocalDateTime.now())
                .build();

        HealthReport savedReport = reportRepository.save(report);
        
        // 异步触发 AI 分析
        triggerAsyncAnalysis(savedReport.getId());
        
        return toResponse(savedReport);
    }

    private void triggerAsyncAnalysis(Long reportId) {
        CompletableFuture.runAsync(() -> {
            try {
                // Give a small delay to ensure transaction commit if any (safety net)
                Thread.sleep(500); 
                
                log.info("Starting async analysis for report: {}", reportId);
                HealthReport report = reportRepository.findById(reportId).orElse(null);
                if (report == null) {
                    log.error("Async analysis failed: Report not found for ID {}", reportId);
                    return;
                }
                if (report.getStatus() == ReportStatus.COMPLETED) {
                    log.info("Report {} already completed, skip async analysis.", reportId);
                    return;
                }
                report.setStatus(ReportStatus.PROCESSING);
                report.setProgressPercent(5);
                report.setProgressStage("开始处理");
                reportRepository.save(report);

                report.setProgressPercent(15);
                report.setProgressStage("读取图像");
                reportRepository.save(report);

                Map<String, Object> result = reportAnalysisService.performOcr(report.getImageUrl());
                
                report.setProgressPercent(60);
                report.setProgressStage("OCR完成");
                reportRepository.save(report);

                Map<String, Object> interpretationObj = reportAnalysisService.analyzeReport(result);
                
                report.setProgressPercent(85);
                report.setProgressStage("生成解读");
                reportRepository.save(report);

                String ocrJson = objectMapper.writeValueAsString(result);
                String interpretation = interpretationObj == null
                        ? ""
                        : objectMapper.writeValueAsString(interpretationObj);
                
                // Truncate interpretation if too long for column (TEXT in mysql is 65535)
                // If using TEXT in DB this is safer to truncate.
                if (interpretation != null && interpretation.length() > 60000) {
                     interpretation = interpretation.substring(0, 60000) + "...(截断)";
                }

                report.setOcrData(ocrJson);
                report.setInterpretation(interpretation);
                report.setStatus(ReportStatus.COMPLETED);
                report.setProgressPercent(100);
                report.setProgressStage("完成");
                reportRepository.save(report);
                log.info("Analysis completed for report: {}", reportId);
                
            } catch (Exception e) {
                log.error("Analysis failed for report: {}", reportId, e);
                reportRepository.findById(reportId).ifPresent(r -> {
                    r.setStatus(ReportStatus.FAILED);
                    String message = e.getMessage();
                    if (message != null && message.length() > 800) {
                        message = message.substring(0, 800);
                    }
                    r.setErrorMessage(message);
                    r.setProgressPercent(100);
                    r.setProgressStage("失败");
                    reportRepository.save(r);
                });
            }
        }, reportExecutor);
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 30000)
    public void retryPendingReports() {
        List<HealthReport> pending = reportRepository.findByStatusOrderByCreatedAtAsc(ReportStatus.PENDING);
        if (pending == null || pending.isEmpty()) return;
        pending.stream().limit(3).forEach(r -> {
            log.warn("Retrying pending report analysis: {}", r.getId());
            triggerAsyncAnalysis(r.getId());
        });
    }

    @Override
    public List<HealthReportResponse> getUserReports(Long userId) {
        return reportRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HealthReportResponse getReportDetail(Long userId, Long reportId) {
        HealthReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "报告不存在"));
        
        if (!report.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "无权查看此报告");
        }
        return toResponse(report);
    }

    @Override
    public com.healthfamily.web.dto.ReportStatusResponse getReportStatus(Long userId, Long reportId) {
        HealthReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "报告不存在"));
        if (!report.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "无权查看此报告");
        }
        return new com.healthfamily.web.dto.ReportStatusResponse(
                report.getId(),
                report.getStatus(),
                report.getProgressPercent(),
                report.getProgressStage(),
                report.getErrorMessage(),
                report.getUpdatedAt()
        );
    }

    @Override
    public HealthReportResponse getReportDetailForDoctor(Long doctorId, Long reportId) {
        // 实际应添加医生与患者关系的校验
        HealthReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "报告不存在"));
        return toResponse(report);
    }

    @Override
    public List<HealthReportResponse> getReportsForDoctor(Long doctorId, Long patientUserId) {
        // 实际应添加医生与患者关系的校验
        return reportRepository.findByUserIdOrderByCreatedAtDesc(patientUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HealthReportResponse addDoctorComment(Long doctorId, Long reportId, String comment) {
        HealthReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(404, "报告不存在"));
        
        report.setDoctorComment(comment);
        report.setDoctorCommentTime(LocalDateTime.now());
        report.setStatus(ReportStatus.COMPLETED); // 点评后视为完成
        
        return toResponse(reportRepository.save(report));
    }

    @Override
    public byte[] generateReportDocx(Long doctorId, GenerateReportRequest request) {
        Long userId = request.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "患者不存在"));
        Profile profile = profileRepository.findById(userId).orElse(null);

        BuildContext ctx = buildContext(user, profile, request.diagnosis());

        // 正文：医生若提供 finalContent 则直接使用（避免重复调用大模型）
        String content = Optional.ofNullable(request.finalContent())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(ctx.draftContent);

        // 3. 生成 Docx
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 标题
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("家庭健康智能病例报告");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // 基础信息
            createParagraph(document, "报告日期: " + LocalDate.now());
            createParagraph(document, "主治医师 ID: " + doctorId);
            createParagraph(document, "----------------------------------------");
            
            // 患者信息
            createSectionTitle(document, "一、患者基本信息");
            createParagraph(document, ctx.patientInfoText);
            if (!ctx.profileTagsText.isBlank()) {
                createParagraph(document, ctx.profileTagsText);
            }
            
            // 医生诊断
            createSectionTitle(document, "二、医生临床意见");
            createParagraph(document, request.diagnosis());

            // 健康数据摘要（便于医生核对）
            createSectionTitle(document, "三、患者健康数据摘要");
            for (String line : ctx.healthDataSummaryText.split("\n")) {
                if (line.trim().isEmpty()) continue;
                createParagraph(document, line);
            }

            // 正文（AI草稿/医生最终稿）
            createSectionTitle(document, "四、病例报告正文");
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                createParagraph(document, line);
            }

            // 证据来源（RAG）
            createSectionTitle(document, "五、证据来源（检索片段）");
            if (ctx.evidences.isEmpty()) {
                createParagraph(document, "未检索到可用资料片段。");
            } else {
                int idx = 1;
                for (RagEvidenceDto ev : ctx.evidences) {
                    String header = String.format(Locale.ROOT, "[%d] %s（%s，fragmentId=%s）",
                            idx++,
                            safe(ev.title(), "Untitled"),
                            safe(ev.source(), "Unknown"),
                            ev.fragmentId() == null ? "-" : String.valueOf(ev.fragmentId()));
                    createParagraph(document, header);
                    createParagraph(document, safe(ev.snippet(), "").trim());
                    createParagraph(document, "");
                }
            }
            
            // 底部
            createParagraph(document, "");
            createParagraph(document, "----------------------------------------");
            createParagraph(document, "本报告由家庭健康系统辅助生成，仅供参考。");

            document.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Docx generation failed", e);
            throw new BusinessException(500, "报告文件生成失败");
        }
    }

    @Override
    public byte[] generateReportPdf(Long doctorId, GenerateReportRequest request) {
        Long userId = request.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "患者不存在"));
        Profile profile = profileRepository.findById(userId).orElse(null);

        BuildContext ctx = buildContext(user, profile, request.diagnosis());

        // 正文
        String content = Optional.ofNullable(request.finalContent())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(ctx.draftContent);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // 字体设置 (尝试使用系统字体，或者回退到默认)
            // 注意：OpenPDF 默认不支持中文，需要指定中文字体
            // 这里为了简单，我们尝试加载系统宋体或黑体，如果不存在则可能乱码
            // 生产环境应打包 .ttf 文件
            BaseFont bfChinese;
            try {
                bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                 // Fallback to a font likely to exist on Windows/Linux or throw
                 try {
                     bfChinese = BaseFont.createFont("c:/windows/fonts/simsun.ttc,1", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                 } catch (Exception e2) {
                     // Last resort, might not support Chinese
                     bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                 }
            }
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font sectionFont = new Font(bfChinese, 14, Font.BOLD);
            Font textFont = new Font(bfChinese, 11, Font.NORMAL);
            Font infoFont = new Font(bfChinese, 10, Font.NORMAL);

            // 标题
            Paragraph title = new Paragraph("家庭健康智能病例报告", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 基础信息
            document.add(new Paragraph("报告日期: " + LocalDate.now(), infoFont));
            document.add(new Paragraph("主治医师 ID: " + doctorId, infoFont));
            document.add(new Paragraph("----------------------------------------", textFont));

            // 一、患者基本信息
            Paragraph s1 = new Paragraph("一、患者基本信息", sectionFont);
            s1.setSpacingBefore(10);
            s1.setSpacingAfter(5);
            document.add(s1);
            document.add(new Paragraph(ctx.patientInfoText, textFont));
            if (!ctx.profileTagsText.isBlank()) {
                document.add(new Paragraph(ctx.profileTagsText, textFont));
            }

            // 二、医生临床意见
            Paragraph s2 = new Paragraph("二、医生临床意见", sectionFont);
            s2.setSpacingBefore(10);
            s2.setSpacingAfter(5);
            document.add(s2);
            document.add(new Paragraph(request.diagnosis(), textFont));

            // 三、健康数据摘要
            Paragraph s3 = new Paragraph("三、患者健康数据摘要", sectionFont);
            s3.setSpacingBefore(10);
            s3.setSpacingAfter(5);
            document.add(s3);
            for (String line : ctx.healthDataSummaryText.split("\n")) {
                if (line.trim().isEmpty()) continue;
                document.add(new Paragraph(line, textFont));
            }

            // 四、病例报告正文
            Paragraph s4 = new Paragraph("四、病例报告正文", sectionFont);
            s4.setSpacingBefore(10);
            s4.setSpacingAfter(5);
            document.add(s4);
            for (String line : content.split("\n")) {
                if (line.trim().isEmpty()) continue;
                Paragraph p = new Paragraph(line, textFont);
                p.setSpacingAfter(2);
                document.add(p);
            }

            // 五、证据来源
            Paragraph s5 = new Paragraph("五、证据来源（检索片段）", sectionFont);
            s5.setSpacingBefore(10);
            s5.setSpacingAfter(5);
            document.add(s5);
            if (ctx.evidences.isEmpty()) {
                document.add(new Paragraph("未检索到可用资料片段。", textFont));
            } else {
                int idx = 1;
                for (RagEvidenceDto ev : ctx.evidences) {
                    String header = String.format(Locale.ROOT, "[%d] %s（%s，fragmentId=%s）",
                            idx++,
                            safe(ev.title(), "Untitled"),
                            safe(ev.source(), "Unknown"),
                            ev.fragmentId() == null ? "-" : String.valueOf(ev.fragmentId()));
                    Paragraph hp = new Paragraph(header, textFont);
                    hp.setSpacingBefore(4);
                    document.add(hp);
                    document.add(new Paragraph(safe(ev.snippet(), "").trim(), infoFont));
                }
            }

            // 底部
            document.add(new Paragraph("\n----------------------------------------", textFont));
            document.add(new Paragraph("本报告由家庭健康系统辅助生成，仅供参考。", infoFont));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation failed", e);
            throw new BusinessException(500, "PDF报告生成失败");
        }
    }

    @Override
    public ReportGenerationPreviewResponse generateReportPreview(Long doctorId, GenerateReportRequest request) {
        Long userId = request.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "患者不存在"));
        Profile profile = profileRepository.findById(userId).orElse(null);

        BuildContext ctx = buildContext(user, profile, request.diagnosis());
        return new ReportGenerationPreviewResponse(ctx.draftContent, ctx.evidences);
    }

    @Override
    public Flux<String> streamReportPreview(Long doctorId, GenerateReportRequest request) {
        return Mono.fromCallable(() -> {
            Long userId = request.userId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(404, "患者不存在"));
            Profile profile = profileRepository.findById(userId).orElse(null);
            // 这里执行 RAG 检索
            return buildContext(user, profile, request.diagnosis());
        }).flatMapMany(ctx -> {
            // 1. 构建元数据事件 (包含证据)
            String metaJson;
            try {
                Map<String, Object> meta = new HashMap<>();
                meta.put("type", "meta");
                meta.put("evidences", ctx.evidences);
                metaJson = objectMapper.writeValueAsString(meta);
            } catch (Exception e) {
                metaJson = "{\"type\":\"meta\",\"evidences\":[]}";
            }
            
            // 2. 构建 Prompt
            String prompt = buildPromptForReport(ctx.patientInfoText, ctx.profileTagsText, ctx.healthDataSummaryText, request.diagnosis(), ctx.evidences);
            
            // 3. 启动流式生成
            Flux<String> contentStream = chatClientBuilder.build()
                    .prompt(prompt)
                    .stream()
                    .content()
                    .map(text -> {
                        try {
                            Map<String, String> chunk = new HashMap<>();
                            chunk.put("type", "content");
                            chunk.put("text", text);
                            return objectMapper.writeValueAsString(chunk);
                        } catch (Exception e) {
                            return "";
                        }
                    });

            return Flux.concat(Flux.just(metaJson), contentStream);
        });
    }

    @Override
    public byte[] generateBatchReportZip(Long doctorId, GenerateBatchReportRequest request) {
        List<GenerateBatchReportItem> items = request.items() == null ? List.of() : request.items();
        if (items.isEmpty()) {
            throw new BusinessException(400, "批量生成列表不能为空");
        }
        if (items.size() > 50) {
            throw new BusinessException(400, "单次批量生成最多支持50名患者");
        }

        List<CompletableFuture<BatchResult>> futures = new ArrayList<>();
        for (GenerateBatchReportItem item : items) {
            futures.add(CompletableFuture.supplyAsync(() -> generateSingleReport(doctorId, item), reportExecutor));
        }

        List<BatchResult> results = new ArrayList<>();
        for (Future<BatchResult> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                results.add(new BatchResult(null, null, null, "批量任务执行失败"));
            }
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(out)) {
            List<Map<String, Object>> success = new ArrayList<>();
            List<Map<String, Object>> failures = new ArrayList<>();

            for (BatchResult result : results) {
                if (result.bytes != null && result.filename != null) {
                    ZipEntry entry = new ZipEntry(result.filename);
                    zip.putNextEntry(entry);
                    zip.write(result.bytes);
                    zip.closeEntry();
                    Map<String, Object> successItem = new HashMap<>();
                    successItem.put("userId", result.userId);
                    successItem.put("filename", result.filename);
                    success.add(successItem);
                } else {
                    Map<String, Object> failureItem = new HashMap<>();
                    failureItem.put("userId", result.userId);
                    failureItem.put("error", result.error);
                    failures.add(failureItem);
                }
            }

            Map<String, Object> summary = new HashMap<>();
            summary.put("generatedAt", LocalDateTime.now());
            summary.put("total", results.size());
            summary.put("successCount", success.size());
            summary.put("failureCount", failures.size());
            summary.put("success", success);
            summary.put("failures", failures);

            ZipEntry summaryEntry = new ZipEntry("batch_result.json");
            zip.putNextEntry(summaryEntry);
            zip.write(objectMapper.writeValueAsBytes(summary));
            zip.closeEntry();

            zip.finish();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Batch report zip generation failed", e);
            throw new BusinessException(500, "批量报告打包失败");
        }
    }

    private record BuildContext(
            String patientInfoText,
            String profileTagsText,
            String healthDataSummaryText,
            List<RagEvidenceDto> evidences,
            String draftContent
    ) {}

    private record BatchResult(Long userId, String filename, byte[] bytes, String error) {}

    private BatchResult generateSingleReport(Long doctorId, GenerateBatchReportItem item) {
        if (item == null || item.userId() == null) {
            return new BatchResult(null, null, null, "患者ID不能为空");
        }
        try {
            GenerateReportRequest request = new GenerateReportRequest(
                    item.userId(),
                    item.diagnosis(),
                    item.finalContent()
            );
            // 修改为 PDF 生成
            byte[] bytes = generateReportPdf(doctorId, request);
            String filename = "健康病例报告_患者" + item.userId() + ".pdf";
            return new BatchResult(item.userId(), filename, bytes, null);
        } catch (Exception e) {
            String message = e instanceof BusinessException ? e.getMessage() : "报告生成失败";
            return new BatchResult(item.userId(), null, null, message);
        }
    }

    private BuildContext buildContext(User user, Profile profile, String diagnosis) {
        Long userId = user.getId();

        // 基础信息
        String displayName = user.getNickname() != null ? user.getNickname() : user.getPhone();
        String sex = profile != null && profile.getSex() != null ? profile.getSex().name() : "未知";
        int age = 0;
        if (profile != null && profile.getBirthday() != null) {
            age = Period.between(profile.getBirthday(), LocalDate.now()).getYears();
        }
        String patientInfo = String.format(Locale.ROOT, "姓名: %s, 性别: %s, 年龄: %d", displayName, sex, age);

        // 体质（取最新一条）
        List<ConstitutionAssessment> assessmentList = assessmentRepository.findByUserOrderByCreatedAtDesc(user);
        ConstitutionAssessment assessment = assessmentList.isEmpty() ? null : assessmentList.get(0);
        String constitutionInfo = assessment != null ? "当前体质: " + safe(assessment.getPrimaryType(), "未知") : "暂无体质测评记录";

        // 日志（30天）
        List<HealthLog> logs30 = logRepository.findByUserAndLogDateBetweenOrderByLogDateDesc(
                user, LocalDate.now().minusDays(30), LocalDate.now());
        List<HealthLog> abnormal = logRepository.findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(userId);
        if (abnormal.size() > 10) abnormal = abnormal.subList(0, 10);
        String logsSummary = buildLogsSummaryText(logs30, abnormal);

        // 最近报告（带OCR数据）
        List<HealthReport> reports = reportRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<HealthReport> topReports = reports.stream()
                .filter(r -> r.getOcrData() != null && !r.getOcrData().isBlank())
                .limit(3)
                .collect(Collectors.toList());
        String reportSummary = buildReportSummaryText(topReports);

        String profileTags = buildProfileTagsText(profile, assessment);

        String healthSummary = constitutionInfo
                + "\n" + logsSummary
                + (reportSummary.isBlank() ? "" : ("\n" + reportSummary));

        // RAG 证据
        String ragQuery = buildRagQuery(profile, assessment, abnormal, diagnosis);
        List<RagEvidenceDto> evidences = mapEvidences(docRagService.search(ragQuery));

        // Prompt & draft
        String prompt = buildPromptForReport(patientInfo, profileTags, healthSummary, diagnosis, evidences);
        String draft;
        try {
            draft = chatClientBuilder.build()
                    .prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI Generation failed", e);
            draft = "AI 分析服务暂时不可用，请稍后重试。\n\n医生诊断意见：\n" + diagnosis;
        }

        // 轻量兜底：如果有证据但模型完全没做任何 [n] 行内引用，则提示医生补引用（避免误以为已溯源）
        if (draft != null && !draft.isBlank() && evidences != null && !evidences.isEmpty()) {
            if (!INLINE_CITATION_PATTERN.matcher(draft).find()) {
                draft = draft.trim()
                        + "\n\n参考依据\n- 未检测到正文中的行内引用标记（如[1]）。如需溯源，请结合下方“证据来源”手动补充引用。";
            }
        }

        return new BuildContext(patientInfo, profileTags, healthSummary, evidences, draft);
    }

    private String buildLogsSummaryText(List<HealthLog> logs, List<HealthLog> abnormal) {
        if (logs == null || logs.isEmpty()) {
            return "近30天无健康日志记录。";
        }
        long diet = logs.stream().filter(l -> l.getType() == com.healthfamily.domain.constant.HealthLogType.DIET).count();
        long sleep = logs.stream().filter(l -> l.getType() == com.healthfamily.domain.constant.HealthLogType.SLEEP).count();
        long sport = logs.stream().filter(l -> l.getType() == com.healthfamily.domain.constant.HealthLogType.SPORT).count();
        long mood = logs.stream().filter(l -> l.getType() == com.healthfamily.domain.constant.HealthLogType.MOOD).count();
        long vitals = logs.stream().filter(l -> l.getType() == com.healthfamily.domain.constant.HealthLogType.VITALS).count();

        String latest = logs.get(0).getType().name() + "@" + logs.get(0).getLogDate();
        String abnormalSummary = (abnormal == null || abnormal.isEmpty())
                ? "异常标记: 无"
                : ("异常标记: " + abnormal.size() + "条（最近：" + abnormal.get(0).getType().name() + "@" + abnormal.get(0).getLogDate() + "）");

        return String.format(Locale.ROOT,
                "近30天日志统计：饮食%d，睡眠%d，运动%d，情绪%d，体征%d；最新记录：%s。\n%s",
                diet, sleep, sport, mood, vitals, latest, abnormalSummary);
    }

    private String buildReportSummaryText(List<HealthReport> reports) {
        if (reports == null || reports.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("最近上传体检/化验报告（含OCR数据）:");
        int idx = 1;
        for (HealthReport r : reports) {
            sb.append("\n- ").append(idx++).append(". ")
                    .append(safe(r.getReportName(), "报告"))
                    .append(" / ").append(r.getReportType() != null ? r.getReportType().name() : "-")
                    .append(" / ").append(r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate() : "-");
        }
        return sb.toString();
    }

    private String buildProfileTagsText(Profile profile, ConstitutionAssessment assessment) {
        if (profile == null && assessment == null) return "";
        List<String> parts = new ArrayList<>();
        if (profile != null) {
            List<String> healthTags = parseStringList(profile.getHealthTags());
            List<String> allergies = parseStringList(profile.getAllergies());
            List<String> goals = parseStringList(profile.getGoals());
            List<String> tcmTags = parseStringList(profile.getTcmTags());
            if (!healthTags.isEmpty()) parts.add("健康标签: " + String.join("、", healthTags));
            if (!tcmTags.isEmpty()) parts.add("中医标签: " + String.join("、", tcmTags));
            if (!allergies.isEmpty()) parts.add("过敏/禁忌: " + String.join("、", allergies));
            if (!goals.isEmpty()) parts.add("健康目标: " + String.join("、", goals));
        }
        if (assessment != null && assessment.getConstitutionTags() != null) {
            List<String> cTags = parseStringList(assessment.getConstitutionTags());
            if (!cTags.isEmpty()) parts.add("体质标签: " + String.join("、", cTags));
        }
        return String.join("\n", parts);
    }

    private String buildRagQuery(Profile profile, ConstitutionAssessment assessment, List<HealthLog> abnormal, String diagnosis) {
        StringBuilder sb = new StringBuilder();
        sb.append("病例报告 ");
        if (diagnosis != null) sb.append(diagnosis).append(' ');
        if (assessment != null && assessment.getPrimaryType() != null) sb.append(assessment.getPrimaryType()).append(' ');
        if (profile != null) {
            parseStringList(profile.getHealthTags()).forEach(t -> sb.append(t).append(' '));
            parseStringList(profile.getTcmTags()).forEach(t -> sb.append(t).append(' '));
        }
        if (abnormal != null && !abnormal.isEmpty()) {
            sb.append("异常 ");
            abnormal.stream().limit(3).forEach(l -> sb.append(l.getType().name()).append(' '));
        }
        return sb.toString().trim();
    }

    private List<RagEvidenceDto> mapEvidences(List<Map<String, Object>> raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        List<RagEvidenceDto> out = new ArrayList<>();
        for (Map<String, Object> m : raw) {
            Long id = null;
            Object idObj = m.get("fragmentId");
            if (idObj instanceof Number n) id = n.longValue();
            else if (idObj != null) {
                try { id = Long.parseLong(String.valueOf(idObj)); } catch (Exception ignored) {}
            }
            String title = m.get("title") != null ? String.valueOf(m.get("title")) : null;
            String source = m.get("source") != null ? String.valueOf(m.get("source")) : null;
            String snippet = m.get("snippet") != null ? String.valueOf(m.get("snippet")) : null;
            String content = m.get("content") != null ? String.valueOf(m.get("content")) : null;
            out.add(new RagEvidenceDto(id, title, source, snippet, content));
        }
        return out;
    }

    private String buildPromptForReport(
            String patientInfo,
            String profileTags,
            String healthDataSummary,
            String diagnosis,
            List<RagEvidenceDto> evidences
    ) {
        StringBuilder evidenceBlock = new StringBuilder();
        if (evidences != null && !evidences.isEmpty()) {
            int i = 1;
            for (RagEvidenceDto ev : evidences) {
                evidenceBlock.append('[').append(i++).append("] ")
                        .append(safe(ev.title(), "Untitled"))
                        .append("（").append(safe(ev.source(), "Unknown")).append("，fragmentId=").append(ev.fragmentId() == null ? "-" : ev.fragmentId()).append("）\n")
                        .append(safe(ev.content(), safe(ev.snippet(), ""))).append("\n\n");
            }
        }

        return String.format(Locale.ROOT, """
                你是一名经验丰富的家庭医生助手。请根据【患者信息】【健康数据摘要】【医生诊断/临床意见】以及【检索资料片段】生成一份“病例报告正文”草稿。
                
                【患者信息】
                %s
                
                【患者标签/背景】
                %s
                
                【健康数据摘要】
                %s
                
                【医生诊断/临床意见】
                %s
                
                【检索资料片段（用于溯源，不要编造来源）】
                %s
                
                【输出要求】
                1. 直接输出纯文本，不要Markdown、不要代码块。
                2. 使用分节标题，建议包含：病情摘要、健康数据分析、诊断依据与风险评估、治疗与随访计划、生活方式与用药/饮食建议、注意事项与复诊提醒。
                3. 如果某项数据缺失，请写“暂无数据/未记录”，不要猜测。
                4. 引用规则（必须遵守）：
                   - 你只允许引用上面的资料片段编号 [1]...[N]。
                   - 当你使用某条资料片段的信息时，必须在对应句子末尾添加行内引用，例如：……[2]
                   - 不要引用不存在的编号，不要编造来源。
                5. 在正文末尾新增“参考依据”小节：逐条列出你实际引用过的编号（如[1][2]），并写一句“为何引用”；若未引用任何片段，写“未引用检索资料片段”。
                """,
                safe(patientInfo, ""),
                safe(profileTags, "（无）"),
                safe(healthDataSummary, ""),
                safe(diagnosis, ""),
                evidenceBlock.isEmpty() ? "（未检索到资料片段）" : evidenceBlock.toString()
        );
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            if (obj instanceof List<?> list) {
                List<String> out = new ArrayList<>();
                for (Object o : list) out.add(String.valueOf(o));
                return out;
            }
            return Collections.emptyList();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private String safe(String s, String def) {
        if (s == null) return def;
        String t = s.trim();
        return t.isEmpty() ? def : t;
    }

    @Override
    public byte[] getReportTemplate() {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("家庭健康病例报告模板");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            createSectionTitle(document, "一、患者基本信息");
            createParagraph(document, "[此处填写患者姓名、性别、年龄]");
            
            createSectionTitle(document, "二、主诉与病史");
            createParagraph(document, "[此处填写]");

            createSectionTitle(document, "三、诊断意见");
            createParagraph(document, "[此处填写]");

            document.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException(500, "模板生成失败");
        }
    }

    private void createSectionTitle(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(true);
        r.setFontSize(14);
        r.addBreak();
    }

    private void createParagraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
    }

    private HealthReportResponse toResponse(HealthReport report) {
        return new HealthReportResponse(
                report.getId(),
                report.getReportName(),
                report.getReportType(),
                report.getImageUrl(),
                report.getStatus(),
                report.getOcrData(),
                report.getInterpretation(),
                report.getDoctorComment(),
                report.getDoctorCommentTime(),
                report.getCreatedAt()
        );
    }
}
