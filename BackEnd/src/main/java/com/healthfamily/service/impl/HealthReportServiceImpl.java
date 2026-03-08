package com.healthfamily.service.impl;

import com.healthfamily.domain.constant.ReportStatus;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyMember;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.entity.HealthReport;
import com.healthfamily.domain.repository.HealthReportRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.HealthReportService;
import com.healthfamily.web.dto.GenerateBatchReportRequest;
import com.healthfamily.web.dto.GenerateReportRequest;
import com.healthfamily.web.dto.HealthReportRequest;
import com.healthfamily.web.dto.HealthReportResponse;
import com.healthfamily.web.dto.ReportGenerationPreviewResponse;
import com.healthfamily.web.dto.ReportStatusResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReportServiceImpl implements HealthReportService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final HealthLogRepository healthLogRepository;
    private final HealthReportRepository healthReportRepository;
    private final UserRepository userRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public List<HealthReportResponse> getUserReports(Long userId) {
        List<HealthReport> reports = healthReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HealthReportResponse submitReport(Long userId, HealthReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        HealthReport report = HealthReport.builder()
                .user(user)
                .reportName(request.reportName())
                .reportType(request.reportType())
                .imageUrl(request.imageUrl())
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        HealthReport savedReport = healthReportRepository.save(report);

        // 触发异步分析任务
        new Thread(() -> {
            try {
                // 1. 准备阶段
                savedReport.setStatus(ReportStatus.PROCESSING);
                savedReport.setProgressPercent(10);
                savedReport.setProgressStage("正在加载报告图片...");
                healthReportRepository.save(savedReport);

                // 2. 获取图片资源
                String imageUrl = savedReport.getImageUrl();
                Resource imageResource = null;
                if (imageUrl.startsWith("/api/files/")) {
                    // 本地文件
                    // URL format: /api/files/{date}/{filename}
                    // Extract date/filename part
                    String relativePath = imageUrl.substring("/api/files/".length());
                    Path filePath = Paths.get(uploadDir).resolve(relativePath).toAbsolutePath();
                    imageResource = new FileSystemResource(filePath.toFile());
                } else {
                    // 暂不支持远程URL或默认图片
                    throw new RuntimeException("不支持的图片来源: " + imageUrl);
                }

                if (!imageResource.exists()) {
                    throw new RuntimeException("图片文件不存在: " + imageResource.getDescription());
                }

                // 3. 构建 Prompt 并调用 AI
                savedReport.setProgressPercent(30);
                savedReport.setProgressStage("正在进行AI多模态识别与分析...");
                healthReportRepository.save(savedReport);

                String userText = "你是一个专业的医疗检验单分析助手。请识别图片中的化验单内容，提取所有检验项目、结果、单位和参考范围。\n" +
                        "**核心任务：**\n" +
                        "1. **OCR识别**：准确提取项目名称、结果数值、单位、参考范围。若数值包含'<'或'>'等符号，请保留。\n" +
                        "2. **输出格式**：\n" +
                        "   - 严禁输出 Markdown 代码块标记（如 ```json），直接输出纯 JSON 字符串。\n" +
                        "   - 只需要返回 ocrData，不需要进行异常判定和解读。\n" +
                        "   - JSON 结构如下：\n" +
                        "{\n" +
                        "  \"ocrData\": {\n" +
                        "    \"items\": [\n" +
                        "      { \"name\": \"项目名称\", \"value\": \"结果数值\", \"unit\": \"单位\", \"reference\": \"参考范围\" }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}";

                UserMessage userMessage = new UserMessage(userText, new Media(MimeTypeUtils.IMAGE_JPEG, imageResource));
                
                // 使用 qwen2.5vl:3b 视觉模型
                ChatResponse response = chatModel.call(new Prompt(userMessage, OllamaOptions.builder()
                        .withModel("qwen2.5vl:3b")
                        .withTemperature(0.1)
                        .withNumPredict(2048) // 增加最大输出 token 数
                        .build()));

                String content = response.getResult().getOutput().getContent();
                log.info("AI Analysis Result: {}", content);

                // 4. 解析结果
                savedReport.setProgressPercent(80);
                savedReport.setProgressStage("正在整理分析结果...");
                healthReportRepository.save(savedReport);

                // 清理可能的 Markdown 标记
                if (content.startsWith("```json")) {
                    content = content.substring(7);
                }
                if (content.startsWith("```")) {
                    content = content.substring(3);
                }
                if (content.endsWith("```")) {
                    content = content.substring(0, content.length() - 3);
                }
                content = content.trim();

                // 尝试修复未闭合的 JSON
                if (!content.endsWith("}")) {
                    log.warn("检测到 JSON 不完整，尝试修复...");
                    if (content.lastIndexOf("}") < content.lastIndexOf("{")) {
                        content += "}";
                    }
                    if (content.lastIndexOf("]") < content.lastIndexOf("[")) {
                         content += "]";
                         if (content.lastIndexOf("}") < content.lastIndexOf("{")) {
                             content += "}";
                         }
                    }
                }

                JsonNode jsonNode = null;
                try {
                    jsonNode = objectMapper.readTree(content);
                } catch (Exception e) {
                    log.error("JSON 解析失败，原始内容: {}", content, e);
                    throw new RuntimeException("AI 返回格式错误，请重试");
                }
                
                // 后处理：在 Java 中进行数值比对和解读生成
                JsonNode ocrDataNode = jsonNode.get("ocrData");
                // 修复 com.fasterxml.jackson.node 包路径错误，使用完全限定名或正确导入
                com.fasterxml.jackson.databind.node.ObjectNode interpretationNode = objectMapper.createObjectNode();
                com.fasterxml.jackson.databind.node.ObjectNode detailsNode = objectMapper.createObjectNode();
                
                int abnormalCount = 0;
                int totalCount = 0;
                StringBuilder summaryBuilder = new StringBuilder();
                
                if (ocrDataNode != null && ocrDataNode.has("items")) {
                    JsonNode itemsNode = ocrDataNode.get("items");
                    if (itemsNode.isArray()) {
                        for (JsonNode item : itemsNode) {
                            totalCount++;
                            String name = item.has("name") ? item.get("name").asText() : "未知项目";
                            String valueStr = item.has("value") ? item.get("value").asText() : "";
                            String referenceStr = item.has("reference") ? item.get("reference").asText() : "";
                            
                            boolean isAbnormal = checkAbnormal(valueStr, referenceStr);
                            
                            // 更新 isAbnormal 字段到 item 中 (如果是 ObjectNode)
                            if (item instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                                ((com.fasterxml.jackson.databind.node.ObjectNode) item).put("isAbnormal", isAbnormal);
                            }
                            
                            // 生成解读
                            String interpretationText = "正常";
                            if (isAbnormal) {
                                abnormalCount++;
                                interpretationText = "异常，建议关注"; // 简单逻辑，无法判断偏高偏低
                                // 尝试判断偏高偏低
                                String highLow = checkHighLow(valueStr, referenceStr);
                                if (!highLow.isEmpty()) {
                                    interpretationText = highLow;
                                }
                            }
                            detailsNode.put(name, interpretationText);
                            
                            // 将解读结果直接写入 item，方便前端直接使用，避免 key 不匹配问题
                            if (item instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                                ((com.fasterxml.jackson.databind.node.ObjectNode) item).put("interpretation", interpretationText);
                            }
                        }
                    }
                }
                
                if (abnormalCount == 0) {
                    summaryBuilder.append("总体评价：所有项目结果均在正常范围内，身体状况良好。");
                } else {
                    summaryBuilder.append(String.format("总体评价：共检测 %d 项，发现 %d 项异常，请关注。", totalCount, abnormalCount));
                }
                
                // 第二阶段：调用 AI 生成深度解读（仅针对异常项和总体建议）
                savedReport.setProgressPercent(90);
                savedReport.setProgressStage("正在生成专业解读...");
                healthReportRepository.save(savedReport);

                try {
                    // 构建 Prompt
                    StringBuilder promptBuilder = new StringBuilder();
                    promptBuilder.append("你是一位经验丰富的全科医生。请根据以下化验单数据，为患者提供专业的解读和建议。\n\n");
                    promptBuilder.append("【检测数据】\n");
                    
                    if (ocrDataNode != null && ocrDataNode.has("items")) {
                        for (JsonNode item : ocrDataNode.get("items")) {
                            String name = item.has("name") ? item.get("name").asText() : "";
                            String value = item.has("value") ? item.get("value").asText() : "";
                            String ref = item.has("reference") ? item.get("reference").asText() : "";
                            boolean isAbnormal = item.has("isAbnormal") && item.get("isAbnormal").asBoolean();
                            String status = isAbnormal ? "异常" : "正常";
                            
                            promptBuilder.append(String.format("- %s: %s (参考: %s) -> %s\n", name, value, ref, status));
                        }
                    }
                    
                    promptBuilder.append("\n【任务要求】\n");
                    promptBuilder.append("1. **总体健康摘要**：用通俗易懂的语言总结患者的健康状况。\n");
                    promptBuilder.append("2. **异常项深度解读**：仅针对状态为“异常”的项目进行深度解读，解释其可能的临床意义（如可能的原因、相关疾病）以及生活方式上的建议（如饮食、运动）。对于状态为“正常”的项目，请直接忽略，不要输出任何解读内容。\n");
                    promptBuilder.append("3. **输出格式**：请直接输出纯 JSON 字符串，不要包含 Markdown 标记。\n");
                    promptBuilder.append("JSON 结构如下：\n");
                    promptBuilder.append("{\n");
                    promptBuilder.append("  \"summary\": \"...\",\n");
                    promptBuilder.append("  \"details\": {\n");
                    promptBuilder.append("    \"异常项目名称1\": \"原因：... 建议：...\",\n");
                    promptBuilder.append("    \"异常项目名称2\": \"原因：... 建议：...\"\n");
                    promptBuilder.append("  }\n");
                    promptBuilder.append("}\n");

                    // 调用 AI
                    ChatResponse aiResponse = chatModel.call(new Prompt(new UserMessage(promptBuilder.toString()), OllamaOptions.builder()
                            .withModel("qwen2.5:7b") // 使用文本模型
                            .withTemperature(0.7)
                            .withNumPredict(1024)
                            .build()));
                            
                    String aiContent = aiResponse.getResult().getOutput().getContent();
                    log.info("AI Interpretation Result: {}", aiContent);
                    
                    // 解析 AI 返回的 JSON
                    if (aiContent.startsWith("```json")) {
                        aiContent = aiContent.substring(7);
                    }
                    if (aiContent.startsWith("```")) {
                        aiContent = aiContent.substring(3);
                    }
                    if (aiContent.endsWith("```")) {
                        aiContent = aiContent.substring(0, aiContent.length() - 3);
                    }
                    aiContent = aiContent.trim();
                    
                    JsonNode aiJson = objectMapper.readTree(aiContent);
                    if (aiJson.has("summary")) {
                        interpretationNode.put("summary", aiJson.get("summary").asText());
                    }
                    if (aiJson.has("details")) {
                        JsonNode aiDetails = aiJson.get("details");
                        // 合并 AI 的详细解读到 detailsNode
                        aiDetails.fieldNames().forEachRemaining(fieldName -> {
                            // 仅当项目被判定为异常时，才采纳 AI 的详细解读
                            boolean isActuallyAbnormal = false;
                            if (ocrDataNode != null && ocrDataNode.has("items")) {
                                for (JsonNode item : ocrDataNode.get("items")) {
                                    if (item.has("name") && item.get("name").asText().equals(fieldName)) {
                                        if (item.has("isAbnormal") && item.get("isAbnormal").asBoolean()) {
                                            isActuallyAbnormal = true;
                                        }
                                        break;
                                    }
                                }
                            }

                            if (isActuallyAbnormal) {
                                detailsNode.put(fieldName, aiDetails.get(fieldName).asText());
                                
                                // 同时更新 ocrData 中的 interpretation 字段
                                if (ocrDataNode != null && ocrDataNode.has("items")) {
                                    for (JsonNode item : ocrDataNode.get("items")) {
                                        if (item.has("name") && item.get("name").asText().equals(fieldName)) {
                                             if (item instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                                                ((com.fasterxml.jackson.databind.node.ObjectNode) item).put("interpretation", aiDetails.get(fieldName).asText());
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error("AI 深度解读失败", e);
                    // 降级处理：保持原有的简单解读
                }

                interpretationNode.put("summary", summaryBuilder.toString()); // 如果AI失败，保留Java生成的简单summary
                if (interpretationNode.has("summary") && !interpretationNode.get("summary").asText().isEmpty()) {
                     // 如果AI成功生成了summary，则不覆盖
                } else {
                     interpretationNode.put("summary", summaryBuilder.toString());
                }
                
                interpretationNode.set("details", detailsNode);

                String ocrDataStr = objectMapper.writeValueAsString(ocrDataNode);
                String interpretationStr = objectMapper.writeValueAsString(interpretationNode);

                // 5. 保存结果
                savedReport.setStatus(ReportStatus.COMPLETED);
                savedReport.setProgressPercent(100);
                savedReport.setProgressStage("分析完成");
                savedReport.setOcrData(ocrDataStr);
                savedReport.setInterpretation(interpretationStr);
                healthReportRepository.save(savedReport);

            } catch (Exception e) {
                log.error("AI Report Analysis Failed", e);
                savedReport.setStatus(ReportStatus.FAILED);
                savedReport.setErrorMessage("分析失败: " + e.getMessage());
                healthReportRepository.save(savedReport);
            }
        }).start();

        return mapToResponse(savedReport);
    }

    @Override
    public HealthReportResponse getReportDetail(Long userId, Long reportId) {
        HealthReport report = healthReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("报告不存在"));

        if (!report.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问该报告");
        }

        return mapToResponse(report);
    }

    @Override
    public ReportStatusResponse getReportStatus(Long userId, Long reportId) {
        HealthReport report = healthReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("报告不存在"));

        if (!report.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问该报告");
        }

        return new ReportStatusResponse(
                report.getId(),
                report.getStatus(),
                report.getProgressPercent(),
                report.getProgressStage(),
                report.getErrorMessage(),
                report.getUpdatedAt()
        );
    }

    @Override
    public List<HealthReportResponse> getReportsForDoctor(Long doctorId, Long userId) {
        // 医生查看特定用户的报告列表
        List<HealthReport> reports = healthReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HealthReportResponse getReportDetailForDoctor(Long doctorId, Long reportId) {
        // 医生查看报告详情
        HealthReport report = healthReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("报告不存在"));
        return mapToResponse(report);
    }

    @Override
    public HealthReportResponse addDoctorComment(Long doctorId, Long reportId, String comment) {
        // 医生添加评论
        HealthReport report = healthReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("报告不存在"));
        report.setDoctorComment(comment);
        report.setDoctorCommentTime(LocalDateTime.now());
        HealthReport savedReport = healthReportRepository.save(report);
        return mapToResponse(savedReport);
    }

    @Override
    public byte[] getReportTemplate() {
        // 返回报告模板（此处为示例，实际应从文件或资源加载）
        return "报告模板内容".getBytes();
    }

    @Override
    public byte[] generateReportDocx(Long doctorId, GenerateReportRequest request) {
        // 生成 Docx 报告（此处为示例，实际应调用 Docx 生成逻辑）
        return ("Docx 报告内容 for user " + request.userId()).getBytes();
    }

    @Override
    public byte[] generateReportPdf(Long doctorId, GenerateReportRequest request) {
        // 生成 Pdf 报告（此处为示例，实际应调用 Pdf 生成逻辑）
        return ("Pdf 报告内容 for user " + request.userId()).getBytes();
    }

    @Override
    public byte[] generateBatchReportZip(Long doctorId, GenerateBatchReportRequest request) {
        // 批量生成报告 Zip（此处为示例，实际应调用批量生成逻辑）
        return "Batch Zip 内容".getBytes();
    }

    @Override
    public ReportGenerationPreviewResponse generateReportPreview(Long doctorId, GenerateReportRequest request) {
        // 生成报告预览（此处为示例，实际应调用 AI 生成预览内容）
        return new ReportGenerationPreviewResponse("报告预览内容草稿", Collections.emptyList());
    }

    @Override
    public Flux<String> streamReportPreview(Long doctorId, GenerateReportRequest request) {
        Long patientId = request.userId();
        String diagnosis = request.diagnosis();

        // 1. 获取患者最近 14 天的健康日志
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(14);
        List<HealthLog> logs = healthLogRepository.findByUser_IdAndLogDateBetween(patientId, startDate, endDate);

        StringBuilder healthData = new StringBuilder();
        if (logs.isEmpty()) {
            healthData.append("近期无健康监测数据。");
        } else {
            healthData.append("近期健康监测数据如下：\n");
            logs.forEach(log -> {
                healthData.append(String.format("- %s [%s]: %s (异常: %s)\n",
                        log.getLogDate(), log.getType(), log.getContentJson(),
                        Boolean.TRUE.equals(log.getIsAbnormal()) ? "是" : "否"));
            });
        }

        // 2. 构建 Prompt
        String promptText = String.format("""
                你是一位专业的医生助手。请根据以下信息，为患者生成一份详细的健康报告。
                
                【医生诊断意见】
                %s
                
                【患者近期健康数据（过去14天）】
                %s
                
                【生成要求】
                1. 结合医生的诊断意见和患者的健康数据进行分析。
                2. 如果有异常数据，请重点分析并给出建议。
                3. 报告结构清晰，包含：【健康分析】、【风险提示】、【生活建议】三个部分。
                4. 语气专业、亲切、客观。
                5. 直接输出报告正文，不要包含开场白。
                """, diagnosis, healthData.toString());

        // 3. 构建 Flux
        String metaJson = "{\"type\": \"meta\", \"evidences\": []}";
        
        Flux<String> aiStream = chatModel.stream(new Prompt(promptText))
                .map(chatResponse -> {
                    String content = chatResponse.getResult().getOutput().getContent();
                    if (content == null) return "";
                    try {
                        com.fasterxml.jackson.databind.node.ObjectNode json = objectMapper.createObjectNode();
                        json.put("type", "content");
                        json.put("text", content);
                        return objectMapper.writeValueAsString(json);
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(s -> !s.isEmpty());

        return Flux.concat(Flux.just(metaJson), aiStream);
    }

    private HealthReportResponse mapToResponse(HealthReport report) {
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

    @Override
    public String generateFamilyWeeklyReport(Long userId, Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("家庭不存在"));
        
        // 校验权限：只有家庭管理员（或创建者）可以生成周报
        FamilyMember currentUserMember = familyMemberRepository.findByFamilyAndUser(family, User.builder().id(userId).build())
                .orElseThrow(() -> new RuntimeException("你不是该家庭的成员"));
        
        boolean isAdmin = Boolean.TRUE.equals(currentUserMember.getAdmin());
        if (!isAdmin && !family.getOwner().getId().equals(userId)) {
            throw new RuntimeException("只有家庭管理员可以生成周报");
        }

        List<FamilyMember> members = familyMemberRepository.findByFamily(family);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        StringBuilder dataForAI = new StringBuilder();
        dataForAI.append(String.format("家庭名称：%s\n周报周期：%s 到 %s\n\n", family.getName(), startDate, endDate));

        for (FamilyMember member : members) {
            dataForAI.append(String.format("## 成员：%s (ID: %d)\n", member.getUser().getNickname(), member.getUser().getId()));
            List<HealthLog> logs = healthLogRepository.findByUser_IdAndLogDateBetween(member.getUser().getId(), startDate, endDate);

            if (logs.isEmpty()) {
                dataForAI.append("- 本周无健康数据记录。\n\n");
            } else {
                logs.forEach(log -> {
                    dataForAI.append(String.format("- 日期: %s, 类型: %s, 数据: %s, 是否异常: %s\n",
                            log.getLogDate(),
                            log.getType(),
                            log.getContentJson(),
                            Boolean.TRUE.equals(log.getIsAbnormal()) ? "是" : "否"));
                });
                dataForAI.append("\n");
            }
        }

        String finalPrompt = buildAIPrompt(dataForAI.toString());
        log.info("向AI发送的最终Prompt:\n{}", finalPrompt);

        try {
            Prompt prompt = new Prompt(new UserMessage(finalPrompt));
            String reportContent = chatModel.call(prompt).getResult().getOutput().getContent();
            log.info("AI生成的周报内容:\n{}", reportContent);
            return reportContent;
        } catch (Exception e) {
            log.error("调用AI生成周报失败", e);
            throw new RuntimeException("AI服务调用异常，生成周报失败");
        }
    }

    private boolean checkAbnormal(String valueStr, String referenceStr) {
        if (valueStr == null || valueStr.isEmpty() || referenceStr == null || referenceStr.isEmpty()) {
            return false; // 无法判断，默认正常
        }
        try {
            double value = parseDouble(valueStr);
            Range range = parseRange(referenceStr);
            if (range == null) return false;
            
            // 如果 referenceStr 包含 < 或 ≤，则 range.min 是 Double.MIN_VALUE (负无穷)，range.max 是上限
            // 如果 referenceStr 包含 > 或 ≥，则 range.min 是下限，range.max 是 Double.MAX_VALUE
            // 如果 referenceStr 是范围，则 range.min 和 range.max 都是有效值
            
            return value < range.min || value > range.max;
        } catch (Exception e) {
            // 解析失败，忽略
            return false;
        }
    }

    private String checkHighLow(String valueStr, String referenceStr) {
        try {
            double value = parseDouble(valueStr);
            Range range = parseRange(referenceStr);
            if (range == null) return "";
            
            if (value < range.min) return "偏低";
            if (value > range.max) return "偏高";
            return "正常";
        } catch (Exception e) {
            return "";
        }
    }

    private double parseDouble(String str) {
        // 移除非数字字符（保留小数点和负号）
        // 增强处理：有些OCR结果可能包含空格，或者像 "1.10" 这种
        if (str == null) return 0.0;
        String clean = str.trim().replaceAll("[^0-9.\\-]", "");
        if (clean.isEmpty()) return 0.0;
        // 如果有多个小数点，只保留第一个（极其罕见的OCR错误）
        int firstDot = clean.indexOf('.');
        if (firstDot != -1) {
             String integerPart = clean.substring(0, firstDot);
             String fractionalPart = clean.substring(firstDot + 1).replaceAll("\\.", "");
             clean = integerPart + "." + fractionalPart;
        }
        return Double.parseDouble(clean);
    }

    private static class Range {
        double min;
        double max;
        
        Range(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }

    private Range parseRange(String ref) {
        if (ref == null) return null;
        ref = ref.trim().replaceAll(";", "").replaceAll("；", ""); // 清理常见分隔符
        try {
            // Case 1: "min-max" or "min~max" (e.g. "0.75-1.2", "11-14.5")
            // 注意：要先判断有没有可能是 < 或 >，因为有些参考范围写成 "0-5 (<10)" 这种复杂格式，简单split可能出错
            // 这里只处理最标准的格式
            
            // Case 2: "< max" or "≤ max" (e.g. "<5.0", "≤0.5")
            if (ref.startsWith("<") || ref.startsWith("≤") || ref.startsWith("&lt;") || ref.contains("<") || ref.contains("≤")) {
                String valStr = ref.replaceAll("[^0-9.]", "");
                if (valStr.isEmpty()) return null;
                // 处理可能有多个小数点的情况（如OCR误识别）
                int firstDot = valStr.indexOf('.');
                if (firstDot != -1) {
                    String integerPart = valStr.substring(0, firstDot);
                    String fractionalPart = valStr.substring(firstDot + 1).replaceAll("\\.", "");
                    valStr = integerPart + "." + fractionalPart;
                }
                double max = Double.parseDouble(valStr);
                return new Range(Double.NEGATIVE_INFINITY, max); 
            }
            
            // Case 3: "> min" or "≥ min"
             if (ref.startsWith(">") || ref.startsWith("≥") || ref.startsWith("&gt;") || ref.contains(">") || ref.contains("≥")) {
                String valStr = ref.replaceAll("[^0-9.]", "");
                if (valStr.isEmpty()) return null;
                int firstDot = valStr.indexOf('.');
                if (firstDot != -1) {
                    String integerPart = valStr.substring(0, firstDot);
                    String fractionalPart = valStr.substring(firstDot + 1).replaceAll("\\.", "");
                    valStr = integerPart + "." + fractionalPart;
                }
                double min = Double.parseDouble(valStr);
                return new Range(min, Double.POSITIVE_INFINITY);
            }

            if (ref.contains("-") || ref.contains("~") || ref.contains("～")) {
                String[] parts = ref.split("[-~～]");
                if (parts.length >= 2) {
                    // 取最后两个看起来像数字的部分（防止 "男: 1-5" 这种前缀）
                    // 使用 parseDouble 内部逻辑来清理空格
                    double min = parseDouble(parts[0]);
                    double max = parseDouble(parts[1]);
                    return new Range(min, max);
                }
            }

        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private String buildAIPrompt(String weeklyData) {
        return "你是一名专业的家庭健康管家AI，你的任务是为家庭医生和家庭成员生成一份关于家庭成员过去一周健康状况的周报。\n" +
                "**重要限制条件:**\n" +
                "1. **禁止虚构成员**: 报告必须且只能针对下面“原始数据”部分中明确列出的成员进行分析。严禁生成数据中不存在的成员（例如：严禁使用“小红”等预设人名，除非她在数据中出现）。\n" +
                "2. **严禁断句**: “发现风险”部分的描述必须是完整且有意义的句子。严禁出现诸如“**连续”这种不完整的、含义不明的片段。**\n\n" +
                "**原始数据:**\n" +
                "```\n" +
                weeklyData +
                "```\n\n" +
                "**报告要求:**\n" +
                "1.  **总体概览**: 对整个家庭本周的健康状况给出一个简短的总结。\n" +
                "2.  **成员逐一分析**: \n" +
                "    *   为每一位在原始数据中出现的成员生成一个独立的分析段落，标题应使用其真实昵称（如：'### [成员昵称]分析'）。\n" +
                "    *   **识别亮点**: 基于本周记录，找出表现良好的方面（例如：坚持运动、数据稳定等）。\n" +
                "    *   **发现风险**: 明确指出需要关注的风险点。描述必须具体、完整，如“本周有3天睡眠时长不足6小时”。如果数据不足或无风险，请说明“本周记录显示其健康状态平稳”。\n" +
                "    *   **数据洞察**: 尝试发现数据间的关联（例如：'心率上升可能与情绪记录中的压力有关'）。\n" +
                "3.  **行动建议**: \n" +
                "    *   为每个有风险点的成员提供1-2条具体、可执行的建议。建议必须针对其特定的健康情况。\n" +
                "4.  **格式要求**: \n" +
                "    *   使用标准的 Markdown 格式，层级清晰。\n" +
                "    *   语言专业、严谨且带有温度。\n\n" +
                "请立即开始根据提供的真实数据生成报告。";
    }
}
