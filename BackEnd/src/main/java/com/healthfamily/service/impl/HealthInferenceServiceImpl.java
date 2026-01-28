package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.*;
import com.healthfamily.domain.repository.*;
import com.healthfamily.service.HealthInferenceService;
import com.healthfamily.ai.OllamaLegacyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthInferenceServiceImpl implements HealthInferenceService {

    private final ChatClient.Builder chatClientBuilder;
    private final ConstitutionAssessmentRepository assessmentRepo;
    private final HealthAlertRepository alertRepo;
    private final HealthLogRepository logRepo;
    private final HealthInferenceReportRepository reportRepo;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper;
    private final OllamaLegacyClient ollamaLegacyClient;

    @Override
    public String generateCrossDomainInference(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 1. 获取最新体质
        String constitution = "平和质 (暂无数据)";
        List<ConstitutionAssessment> assessments = assessmentRepo.findByUserOrderByCreatedAtDesc(user);
        if (!assessments.isEmpty()) {
            constitution = assessments.get(0).getPrimaryType();
        }

        // 2. 获取近3天异常警报
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<HealthAlert> recentAlerts = alertRepo.findByUserOrderByCreatedAtDesc(user).stream()
                .filter(a -> a.getCreatedAt().isAfter(threeDaysAgo))
                .collect(Collectors.toList());

        List<String> alertTexts = recentAlerts.stream()
                .map(a -> String.format("%s: %s (Value: %s) - %s", 
                        a.getCreatedAt().toLocalDate(), 
                        a.getMetric(),
                        a.getValue(),
                        a.getMessage()))
                .collect(Collectors.toList());
        
        String alertText = alertTexts.isEmpty() ? "近期无明显异常体征" : String.join("\n", alertTexts);

        // 3. 获取近3天关键日志 (饮食/睡眠等)
        LocalDate threeDaysAgoDate = LocalDate.now().minusDays(3);
        List<HealthLog> recentLogs = logRepo.findByUserAndLogDateBetweenOrderByLogDateDesc(
                user, threeDaysAgoDate, LocalDate.now());
        
        List<String> logTexts = recentLogs.stream()
                .map(l -> String.format("%s [%s]: %s", 
                        l.getLogDate(), 
                        l.getType(), // 假设 Type 是 Enum 或 String
                        l.getContentJson() != null ? l.getContentJson() : "记录")) // 使用 contentJson
                .limit(10) // 限制数量防止 Prompt 过长
                .collect(Collectors.toList());
        
        String logText = logTexts.isEmpty() ? "近期无详细记录" : String.join("\n", logTexts);


        // 4. 构建 Prompt
        PromptTemplate promptTemplate = new PromptTemplate(
                new ClassPathResource("prompts/cross_domain_inference.st")
        );

        Map<String, Object> map = Map.of(
                "constitution", constitution,
                "alerts", alertText,
                "logs", logText
        );

        // 5. 调用 AI 进行推理
        String aiResponse;
        try {
            ChatClient chatClient = chatClientBuilder.build();
            Prompt prompt = promptTemplate.create(map);
            aiResponse = chatClient.prompt(prompt).call().content();
        } catch (WebClientResponseException.NotFound ex) {
            String legacyPrompt = promptTemplate.create(map).getContents();
            aiResponse = ollamaLegacyClient.generate(legacyPrompt, null, null);
        }

        // 6. 异步保存到数据库
        try {
            Map<String, Object> summary = Map.of(
                "constitution", constitution,
                "alerts", alertTexts,
                "logs", logTexts
            );
            
            HealthInferenceReport report = HealthInferenceReport.builder()
                    .userId(userId)
                    .reportDate(LocalDate.now())
                    .constitutionSnapshot(constitution)
                    .inputSummary(objectMapper.writeValueAsString(summary))
                    .aiAnalysisResult(aiResponse)
                    .isViewed(false)
                    .build();
            
            reportRepo.save(report);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize input summary", e);
        }

        return aiResponse;
    }
}
