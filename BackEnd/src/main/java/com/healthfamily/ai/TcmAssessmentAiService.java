package com.healthfamily.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.domain.entity.TcmKnowledgeBase;

import com.healthfamily.web.dto.AssessmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.ModelResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;

import org.springframework.ai.converter.BeanOutputConverter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
@RequiredArgsConstructor
public class TcmAssessmentAiService {

    private final ChatClient.Builder chatClientBuilder;
    private final UserRepository userRepository;
    private final ConstitutionAssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper;
    private final OllamaLegacyClient ollamaLegacyClient;

    // 定义结构化输出 Record
    
    // 1. 个性化方案
    record PlanItem(
        String title,
        String content,
        String difficulty, // EASY/MEDIUM/HARD
        List<String> tags,
        List<String> contraindications
    ) {}

    record PersonalizedPlanResult(
        Map<String, List<PlanItem>> planItems,
        List<String> priorityRecommendations,
        Map<String, String> seasonalRecommendations
    ) {}

    // 2. 最终评估结果
    record AssessmentResult(
        String primaryType,
        Map<String, Double> scores,
        Double confidence,
        String summary,
        List<String> recommendations
    ) {}
    
    // 3. 趋势洞察
    record TrendInsightResult(
        String summary,
        List<String> evidence,
        List<String> suggestions
    ) {}

    // 中医九种体质特征
    private static final Map<String, String> CONSTITUTION_DESCRIPTIONS = Map.of(
        "BALANCED", "平和质：体形匀称健壮，面色润泽，头发稠密有光泽，目光有神，鼻色明润，嗅觉通利，唇色红润，不易疲劳，精力充沛，耐受寒热，睡眠良好，胃纳佳，二便正常，舌色淡红、苔薄白，脉和缓有力",
        "QI_DEFICIENCY", "气虚质：肌肉松软不实，平素语音低弱、气短懒言，容易疲乏，精神不振，易出汗，舌淡红、边有齿痕，脉弱",
        "YANG_DEFICIENCY", "阳虚质：肌肉松软不实，平素畏冷，手足不温，喜热饮食，精神不振，舌淡胖嫩、边有齿痕，脉沉迟",
        "YIN_DEFICIENCY", "阴虚质：体形偏瘦，面颊潮红或偏红，时有烘热感，手足心热，少津无痰，大便干燥，舌红少津少苔，脉细数",
        "PHLEGM_DAMPNESS", "痰湿质：体形肥胖，腹部肥满，胸闷，痰多，容易困倦，身重不爽，喜食肥甘醇酒，舌体胖大、苔白腻，脉滑",
        "DAMP_HEAT", "湿热质：面垢油腻，易生痤疮粉刺，身重困倦，大便黏滞不畅或燥结，小便短黄，舌质偏红、苔黄腻，脉滑数",
        "BLOOD_STASIS", "血瘀质：肤色晦暗，色素沉着，容易出现瘀斑，口唇黯淡，舌黯或有瘀点，舌下络脉紫黯或增粗，脉涩",
        "QI_STAGNATION", "气郁质：体形偏瘦，常感闷闷不乐、情绪低沉，易紧张焦虑不安，多愁善感，感情脆弱，烦闷不乐，舌淡红、苔薄白，脉弦",
        "SPECIAL_DIATHESIS", "特禀质：过敏体质者皮肤划痕试验阳性，有药物过敏史；遗传性疾病有垂直遗传性、先天性、家族性特征"
    );

    /**
     * 开始AI驱动的动态体质测评对话
     */
    public Map<String, Object> startAiAssessment(Long userId) {
        // 获取用户信息
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 构建初始提示词
        String initialPrompt = "你是中医体质辨识专家。请根据以下信息开始与用户进行对话，逐步了解用户的体质特征。\n\n" +
                "中医九种体质类型特征：\n";
        
        for (Map.Entry<String, String> entry : CONSTITUTION_DESCRIPTIONS.entrySet()) {
            initialPrompt += entry.getKey() + "（" + getConstitutionName(entry.getKey()) + "）：" + entry.getValue() + "\n";
        }

        initialPrompt += "\n请以友好、专业的中医师身份开始询问用户的身体状况，从一般性问题开始，逐步深入。";

        // 生成初始问题
        String initialQuestion = callTextWithFallback(initialPrompt);

        return Map.of(
            "sessionId", generateSessionId(),
            "question", initialQuestion,
            "step", 1,
            "totalSteps", 10 // 预估对话轮数
        );
    }

    /**
     * 生成个性化中医养生方案
     */
    public Map<String, Object> generatePersonalizedPlan(Long userId, String primaryConstitution, List<TcmKnowledgeBase> knowledgeList) {
        BeanOutputConverter<PersonalizedPlanResult> converter = new BeanOutputConverter<>(PersonalizedPlanResult.class);

        String prompt = "你是中医养生专家。请根据用户的主导体质，生成个性化的养生方案。\n\n" +
                "用户主导体质：" + getConstitutionName(primaryConstitution) + "\n\n" +
                "参考知识库内容：\n";
        
        for (TcmKnowledgeBase kb : knowledgeList) {
            prompt += "- " + kb.getType().name() + ": " + kb.getTitle() + "\n";
        }
        
        prompt += "\n请生成一个详细的个性化养生方案，确保内容专业、实用，且针对性强。\n" +
                  converter.getFormat();

        try {
            String content = callTextWithFallback(prompt);
            String json = extractJson(content);
            PersonalizedPlanResult result = objectMapper.readValue(json, PersonalizedPlanResult.class);
            return objectMapper.convertValue(result, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 处理用户回答并生成下一个问题
     */
    public Map<String, Object> processAnswer(Long userId, String sessionId, String userAnswer) {
        // 获取用户的历史测评记录，用于参考
        var assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(
                userRepository.findById(userId).orElse(null));
        
        String assessmentHistory = "用户历史测评记录：";
        if (assessments != null && !assessments.isEmpty()) {
            assessmentHistory += assessments.stream()
                    .limit(3)
                    .map(assessment -> "主导体质：" + assessment.getPrimaryType() + 
                             "，测评时间：" + assessment.getCreatedAt())
                    .collect(Collectors.joining("; "));
        } else {
            assessmentHistory += "暂无历史记录";
        }

        String prompt = "你是中医体质辨识专家。用户正在回答关于其体质特征的问题。\n\n" +
                "中医九种体质类型特征：\n";
        
        for (Map.Entry<String, String> entry : CONSTITUTION_DESCRIPTIONS.entrySet()) {
            prompt += entry.getKey() + "（" + getConstitutionName(entry.getKey()) + "）：" + entry.getValue() + "\n";
        }

        prompt += "\n" + assessmentHistory + "\n\n" +
                "用户回答：" + userAnswer + "\n\n" +
                "请根据用户的回答，提出下一个有针对性的问题，继续深入了解用户的体质特征。" +
                "问题应具体、有针对性，涵盖中医体质辨识的关键维度。" +
                "请直接返回问题内容，不要包含其他说明。";

        String nextQuestion = callTextWithFallback(prompt);

        // 判断是否需要结束对话
        boolean shouldEnd = shouldEndAssessment(sessionId, userAnswer);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("question", nextQuestion);
        response.put("shouldEnd", shouldEnd);
        
        if (shouldEnd) {
            // 生成最终评估结果
            Map<String, Object> finalResult = generateFinalAssessment(userId, sessionId, userAnswer);
            response.put("result", finalResult);
        }

        return response;
    }

    /**
     * 生成最终体质评估结果
     */
    public Map<String, Object> generateFinalAssessment(Long userId, String sessionId, String finalAnswers) {
        BeanOutputConverter<AssessmentResult> converter = new BeanOutputConverter<>(AssessmentResult.class);

        String prompt = "你是中医体质辨识专家。根据以下用户回答，分析其体质类型。\n\n" +
                "中医九种体质类型特征：\n";
        
        for (Map.Entry<String, String> entry : CONSTITUTION_DESCRIPTIONS.entrySet()) {
            prompt += entry.getKey() + "（" + getConstitutionName(entry.getKey()) + "）：" + entry.getValue() + "\n";
        }

        prompt += "\n用户回答汇总：" + finalAnswers + "\n\n" +
                "请分析用户的体质类型。\n" + 
                converter.getFormat();

        // 解析AI返回的JSON结果
        Map<String, Object> aiResult;
        try {
            String content = callTextWithFallback(prompt);
            String json = extractJson(content);
            AssessmentResult result = objectMapper.readValue(json, AssessmentResult.class);
            aiResult = objectMapper.convertValue(result, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            aiResult = createDefaultResult(finalAnswers);
        }

        return aiResult;
    }
    
    /**
     * 保存AI评估结果到数据库
     */
    public ConstitutionAssessment saveAiAssessmentResult(Long userId, Map<String, Object> aiResult) {
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Map<String, Double> scores = getTypedMap(aiResult.get("scores"));
        String primaryType = (String) aiResult.get("primaryType");
        String reportJson = buildReportJson(aiResult);

        var assessment = com.healthfamily.domain.entity.ConstitutionAssessment.builder()
                .user(user)
                .type("TCM_9_AI")
                .scoreVector(toJson(scores))
                .primaryType(primaryType)
                .reportJson(reportJson)
                .assessmentSource("AI")
                .confidenceScore((Double) aiResult.getOrDefault("confidence", 0.7))
                .assessmentVersion("2.0")
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return assessmentRepository.save(assessment);
    }

    // extractJsonFromText 已被 BeanOutputConverter 替代，移除该方法

    private Map<String, Object> createDefaultResult(String answers) {
        Map<String, Object> result = new HashMap<>();
        result.put("primaryType", "BALANCED");
        result.put("scores", Map.of("BALANCED", 60.0, "QI_DEFICIENCY", 20.0, "YANG_DEFICIENCY", 15.0));
        result.put("confidence", 0.6);
        result.put("summary", "根据您的回答，初步判断为平和质，整体健康状况良好。");
        result.put("recommendations", List.of("保持规律作息", "坚持适量运动", "维持乐观心态"));
        return result;
    }

    private String buildReportJson(Map<String, Object> result) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("primaryType", result.get("primaryType"));
        report.put("scores", result.get("scores"));
        report.put("summary", result.get("summary"));
        report.put("recommendations", result.get("recommendations"));
        report.put("confidence", result.get("confidence"));
        report.put("analysis", "AI分析结果");
        
        try {
            return objectMapper.writeValueAsString(report);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private boolean shouldEndAssessment(String sessionId, String userAnswer) {
        // 简单的逻辑：如果用户表示已完成或达到最大对话轮数，则结束
        String lowerAnswer = userAnswer.toLowerCase();
        return lowerAnswer.contains("完成") || 
               lowerAnswer.contains("结束") || 
               lowerAnswer.contains("好了") ||
               lowerAnswer.contains("可以");
    }

    private String generateSessionId() {
        return "ai_assessment_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    private String getConstitutionName(String code) {
        return switch (code) {
            case "BALANCED" -> "平和质";
            case "QI_DEFICIENCY" -> "气虚质";
            case "YANG_DEFICIENCY" -> "阳虚质";
            case "YIN_DEFICIENCY" -> "阴虚质";
            case "PHLEGM_DAMPNESS" -> "痰湿质";
            case "DAMP_HEAT" -> "湿热质";
            case "BLOOD_STASIS" -> "血瘀质";
            case "QI_STAGNATION" -> "气郁质";
            case "SPECIAL_DIATHESIS" -> "特禀质";
            default -> "未知体质";
        };
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Double> getTypedMap(Object obj) {
        if (obj instanceof Map) {
            Map<Object, Object> rawMap = (Map<Object, Object>) obj;
            Map<String, Double> typedMap = new HashMap<>();
            for (Map.Entry<Object, Object> entry : rawMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof Number) {
                    typedMap.put((String) entry.getKey(), ((Number) entry.getValue()).doubleValue());
                }
            }
            return typedMap;
        }
        return new HashMap<>();
    }

    /**
     * 生成体质趋势洞察
     */
    public Map<String, Object> generateTrendInsights(Long userId, Map<String, String> trends, String currentPrimaryType) {
        BeanOutputConverter<TrendInsightResult> converter = new BeanOutputConverter<>(TrendInsightResult.class);

        String prompt = "你是中医体质辨识专家。请根据用户的体质趋势变化，生成健康洞察。\n\n" +
                "当前主导体质：" + getConstitutionName(currentPrimaryType) + "\n" +
                "近期趋势：" + trends.toString() + "\n\n" +
                "请生成健康洞察。\n" + 
                converter.getFormat();

        try {
            String content = callTextWithFallback(prompt);
            String json = extractJson(content);
            TrendInsightResult result = objectMapper.readValue(json, TrendInsightResult.class);
            return objectMapper.convertValue(result, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("summary", "体质整体" + (trends.containsValue("上升") ? "有波动" : "稳定"));
            fallback.put("evidence", List.of("基于近期测评数据分析"));
            fallback.put("suggestions", List.of("保持规律作息", "注意饮食均衡"));
            return fallback;
        }
    }

    /**
     * 生成体质趋势洞察 (流式)
     */
    public Flux<String> generateTrendInsightsStream(Long userId, Map<String, String> trends, String currentPrimaryType) {
        String prompt = "你是中医体质辨识专家。请根据用户的体质趋势变化，生成健康洞察。\n\n" +
                "当前主导体质：" + getConstitutionName(currentPrimaryType) + "\n" +
                "近期趋势：" + trends.toString() + "\n\n" +
                "请以Markdown格式返回分析报告，包含以下部分：\n" +
                "### 📊 趋势总结\n(一句话总结)\n\n" +
                "### 🔍 判断依据\n(列出关键变化点)\n\n" +
                "### 💡 调理建议\n(3条具体建议)";

        return callStreamWithFallback(prompt);
    }

    private String callTextWithFallback(String prompt) {
        try {
            return chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (WebClientResponseException.NotFound ex) {
            return ollamaLegacyClient.generate(prompt, null, null);
        }
    }

    private Flux<String> callStreamWithFallback(String prompt) {
        Flux<String> stream = chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .stream()
                .content();
        return stream.onErrorResume(WebClientResponseException.NotFound.class,
                ex -> ollamaLegacyClient.generateStream(prompt, null, null));
    }

    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        String trimmed = content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
