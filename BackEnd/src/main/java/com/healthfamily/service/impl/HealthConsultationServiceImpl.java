package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.HealthConsultation;
import com.healthfamily.domain.entity.Profile;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.HealthConsultationRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.HealthConsultationService;
import com.healthfamily.service.HealthDataAiService;
import com.healthfamily.ai.OllamaLegacyClient;
import com.healthfamily.web.dto.ConsultationRequest;
import com.healthfamily.web.dto.ConsultationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.io.IOException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthConsultationServiceImpl implements HealthConsultationService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    private final HealthConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final HealthDataAiService healthDataAiService;
    private final com.healthfamily.service.HealthAiToolService toolService;
    private final OllamaLegacyClient ollamaLegacyClient;

    @Override
    @Transactional
    public ConsultationResponse consult(Long userId, ConsultationRequest request) {
        User user = loadUser(userId);
        
        // 生成或使用会话ID
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        // 获取用户健康档案
        Map<String, Object> userContext = buildUserContext(user);

        // 获取历史对话（上下文）
        List<HealthConsultation> history = consultationRepository
                .findByUser_IdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
        String conversationHistory = buildConversationHistory(history);

        // 构建系统提示词
        String systemPrompt = buildSystemPrompt(userContext);

        // 构建用户问题（包含上下文）
        String userPrompt = buildUserPrompt(request.question(), conversationHistory, userContext);

        // 检测是否需要工具调用
        List<String> toolsUsed = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        String answer;
        
        // 检查问题中是否包含需要工具调用的关键词
        if (needsToolCall(request.question())) {
            answer = consultWithTools(request.question(), userContext, conversationHistory, toolsUsed, sources);
        } else {
            // 普通对话
            String promptText = systemPrompt + "\n\n用户问题：" + userPrompt;
            answer = callTextWithFallback(promptText);
            
            // 提取知识来源
            sources = extractSources(answer);
        }

        // 保存咨询记录
        HealthConsultation consultation = HealthConsultation.builder()
                .user(user)
                .sessionId(sessionId)
                .question(request.question())
                .answer(answer)
                .contextJson(writeJsonSafely(userContext))
                .toolsUsed(toolsUsed.isEmpty() ? null : writeJsonSafely(toolsUsed))
                .sources(sources.isEmpty() ? null : writeJsonSafely(sources))
                .feedback(-1)
                .build();
        
        consultation = consultationRepository.save(consultation);

        return toResponse(consultation);
    }

    @Override
    public Flux<String> consultStream(Long userId, ConsultationRequest request) {
        User user = loadUser(userId);
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        Map<String, Object> userContext = buildUserContext(user);
        List<HealthConsultation> history = consultationRepository
                .findByUser_IdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
        String conversationHistory = buildConversationHistory(history);
        String systemPrompt = buildSystemPrompt(userContext);
        String userPrompt = buildUserPrompt(request.question(), conversationHistory, userContext);

        List<String> toolsUsed = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        String promptText;

        if (needsToolCall(request.question())) {
            StringBuilder toolAnswer = new StringBuilder();
            String lowerQuestion = request.question().toLowerCase();
            if (lowerQuestion.contains("药品") || lowerQuestion.contains("药")) {
                String drugName = extractDrugName(request.question());
                if (drugName != null) {
                    Map<String, Object> drugInfo = toolService.queryDrugInfo(drugName);
                    toolsUsed.add("queryDrugInfo");
                    toolAnswer.append("根据药品信息库查询，").append(drugName).append("的信息如下：\n");
                    toolAnswer.append("适应症：").append(drugInfo.get("indications")).append("\n");
                    toolAnswer.append("用法用量：").append(drugInfo.get("dosage")).append("\n");
                    if (drugInfo.containsKey("contraindications")) {
                        toolAnswer.append("禁忌：").append(drugInfo.get("contraindications")).append("\n");
                    }
                    sources.add("药品知识库");
                }
            }
            if (lowerQuestion.contains("医院") || lowerQuestion.contains("科室")) {
                String location = extractLocation(request.question());
                String department = extractDepartment(request.question());
                Map<String, Object> hospitalInfo = toolService.getNearbyHospitals(
                        location != null ? location : "当前位置",
                        department != null ? department : "");
                toolsUsed.add("getNearbyHospitals");
                toolAnswer.append("为您查询到以下医院信息：\n");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> hospitals = (List<Map<String, Object>>) hospitalInfo.get("hospitals");
                for (Map<String, Object> hospital : hospitals) {
                    toolAnswer.append(hospital.get("name")).append(" - ");
                    toolAnswer.append(hospital.get("address")).append("，距离").append(hospital.get("distance")).append("\n");
                }
                sources.add("医院数据库");
            }
            if (lowerQuestion.contains("知识") || lowerQuestion.contains("指南") ||
                lowerQuestion.contains("怎么办") || lowerQuestion.contains("如何")) {
                String keyword = extractKeyword(request.question());
                Map<String, Object> knowledge = toolService.queryHealthKnowledge(keyword, null);
                toolsUsed.add("queryHealthKnowledge");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> documents = (List<Map<String, Object>>) knowledge.get("documents");
                if (!documents.isEmpty()) {
                    toolAnswer.append("根据健康知识库，为您找到以下相关信息：\n");
                    for (int i = 0; i < Math.min(3, documents.size()); i++) {
                        Map<String, Object> doc = documents.get(i);
                        toolAnswer.append(doc.get("title")).append("：").append(doc.get("content")).append("\n");
                    }
                    sources.add("健康知识库");
                }
            }
            if (toolAnswer.length() == 0) {
                promptText = systemPrompt + "\n\n用户问题：" + userPrompt;
            } else {
                promptText = "基于以下信息，为用户提供一个简洁、专业的回答：\n" +
                        toolAnswer + "\n\n用户问题：" + request.question();
            }
        } else {
            promptText = systemPrompt + "\n\n用户问题：" + userPrompt;
        }

        StringBuilder answerBuilder = new StringBuilder();
        String finalSessionId = sessionId;
        return callStreamWithFallback(promptText)
                .doOnNext(chunk -> {
                    if (chunk != null) {
                        answerBuilder.append(chunk);
                    }
                })
                .doOnComplete(() -> {
                    String answer = answerBuilder.toString();
                    List<String> finalSources = sources.isEmpty() ? extractSources(answer) : sources;
                    HealthConsultation consultation = HealthConsultation.builder()
                            .user(user)
                            .sessionId(finalSessionId)
                            .question(request.question())
                            .answer(answer)
                            .contextJson(writeJsonSafely(userContext))
                            .toolsUsed(toolsUsed.isEmpty() ? null : writeJsonSafely(toolsUsed))
                            .sources(finalSources.isEmpty() ? null : writeJsonSafely(finalSources))
                            .feedback(-1)
                            .build();
                    consultationRepository.save(consultation);
                });
    }

    private String callTextWithFallback(String prompt) {
        try {
            ChatClient client = chatClientBuilder.build();
            Prompt promptObj = new Prompt(prompt);
            var response = client.prompt(promptObj).call();
            return response.content();
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

    private Map<String, Object> buildUserContext(User user) {
        Map<String, Object> context = new HashMap<>();
        
        // 获取用户画像
        Optional<Profile> profileOpt = profileRepository.findById(user.getId());
        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            try {
                if (profile.getHealthTags() != null && !profile.getHealthTags().isEmpty()) {
                    List<String> healthTags = objectMapper.readValue(
                            profile.getHealthTags(), 
                            new TypeReference<List<String>>() {}
                    );
                    context.put("healthTags", healthTags);
                }
                if (profile.getAllergies() != null && !profile.getAllergies().isEmpty()) {
                    List<String> allergies = objectMapper.readValue(
                            profile.getAllergies(), 
                            new TypeReference<List<String>>() {}
                    );
                    context.put("allergies", allergies);
                }
                context.put("sex", profile.getSex());
                context.put("birthday", profile.getBirthday());
                context.put("heightCm", profile.getHeightCm());
                context.put("weightKg", profile.getWeightKg());
            } catch (Exception e) {
                log.warn("解析用户画像失败: {}", e.getMessage());
            }
        }
        
        return context;
    }

    private String buildConversationHistory(List<HealthConsultation> history) {
        if (history.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder("历史对话：\n");
        for (HealthConsultation c : history) {
            sb.append("问：").append(c.getQuestion()).append("\n");
            sb.append("答：").append(c.getAnswer()).append("\n\n");
        }
        return sb.toString();
    }

    private String buildSystemPrompt(Map<String, Object> userContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的家庭健康咨询助手，专门为家庭成员提供健康建议和医疗知识解答。\n\n");
        
        // 添加用户健康标签
        if (userContext.containsKey("healthTags")) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) userContext.get("healthTags");
            if (!tags.isEmpty()) {
                prompt.append("用户健康标签：").append(String.join("、", tags)).append("\n");
            }
        }
        
        // 添加过敏史
        if (userContext.containsKey("allergies")) {
            @SuppressWarnings("unchecked")
            List<String> allergies = (List<String>) userContext.get("allergies");
            if (!allergies.isEmpty()) {
                prompt.append("用户过敏史：").append(String.join("、", allergies)).append("\n");
            }
        }
        
        prompt.append("\n");
        prompt.append("回答要求：\n");
        prompt.append("1. 基于医学知识和指南提供准确、专业的建议\n");
        prompt.append("2. 结合用户的健康标签和过敏史，提供个性化建议\n");
        prompt.append("3. 对于紧急情况，建议及时就医\n");
        prompt.append("4. 回答要简洁明了，易于理解\n");
        prompt.append("5. 如果使用了工具查询（如药品信息），请在回答中说明\n");
        
        return prompt.toString();
    }

    private String buildUserPrompt(String question, String conversationHistory, Map<String, Object> userContext) {
        StringBuilder prompt = new StringBuilder();
        
        if (!conversationHistory.isEmpty()) {
            prompt.append(conversationHistory);
        }
        
        prompt.append("当前问题：").append(question);
        
        return prompt.toString();
    }

    private boolean needsToolCall(String question) {
        String lowerQuestion = question.toLowerCase();
        return lowerQuestion.contains("药品") || 
               lowerQuestion.contains("药") ||
               lowerQuestion.contains("医院") ||
               lowerQuestion.contains("科室") ||
               lowerQuestion.contains("知识") ||
               lowerQuestion.contains("指南");
    }

    private String consultWithTools(String question, Map<String, Object> userContext, 
                                     String conversationHistory, List<String> toolsUsed, 
                                     List<String> sources) {
        StringBuilder answerBuilder = new StringBuilder();
        String lowerQuestion = question.toLowerCase();
        String systemPrompt = buildSystemPrompt(userContext);
        String userPrompt = buildUserPrompt(question, conversationHistory, userContext);
        
        // 药品查询
        if (lowerQuestion.contains("药品") || lowerQuestion.contains("药")) {
            String drugName = extractDrugName(question);
            if (drugName != null) {
                Map<String, Object> drugInfo = toolService.queryDrugInfo(drugName);
                toolsUsed.add("queryDrugInfo");
                answerBuilder.append("根据药品信息库查询，").append(drugName).append("的信息如下：\n");
                answerBuilder.append("适应症：").append(drugInfo.get("indications")).append("\n");
                answerBuilder.append("用法用量：").append(drugInfo.get("dosage")).append("\n");
                if (drugInfo.containsKey("contraindications")) {
                    answerBuilder.append("禁忌：").append(drugInfo.get("contraindications")).append("\n");
                }
                sources.add("药品知识库");
            }
        }
        
        // 医院查询
        if (lowerQuestion.contains("医院") || lowerQuestion.contains("科室")) {
            String location = extractLocation(question);
            String department = extractDepartment(question);
            Map<String, Object> hospitalInfo = toolService.getNearbyHospitals(
                    location != null ? location : "当前位置", 
                    department != null ? department : "");
            toolsUsed.add("getNearbyHospitals");
            answerBuilder.append("为您查询到以下医院信息：\n");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> hospitals = (List<Map<String, Object>>) hospitalInfo.get("hospitals");
            for (Map<String, Object> hospital : hospitals) {
                answerBuilder.append(hospital.get("name")).append(" - ");
                answerBuilder.append(hospital.get("address")).append("，距离").append(hospital.get("distance")).append("\n");
            }
            sources.add("医院数据库");
        }
        
        // 健康知识查询
        if (lowerQuestion.contains("知识") || lowerQuestion.contains("指南") || 
            lowerQuestion.contains("怎么办") || lowerQuestion.contains("如何")) {
            String keyword = extractKeyword(question);
            Map<String, Object> knowledge = toolService.queryHealthKnowledge(keyword, null);
            toolsUsed.add("queryHealthKnowledge");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> documents = (List<Map<String, Object>>) knowledge.get("documents");
            if (!documents.isEmpty()) {
                answerBuilder.append("根据健康知识库，为您找到以下相关信息：\n");
                for (int i = 0; i < Math.min(3, documents.size()); i++) {
                    Map<String, Object> doc = documents.get(i);
                    answerBuilder.append(doc.get("title")).append("：").append(doc.get("content")).append("\n");
                }
                sources.add("健康知识库");
            }
        }
        
        // 如果工具调用没有返回足够信息，使用AI补充
        if (answerBuilder.length() == 0) {
            ChatClient client = chatClientBuilder.build();
            Prompt aiPrompt = new Prompt(systemPrompt + "\n\n用户问题：" + userPrompt);
            var response = client.prompt(aiPrompt).call();
            answerBuilder.append(response.content());
        } else {
            // 工具调用有结果，用AI总结
            ChatClient client = chatClientBuilder.build();
            String summaryPrompt = "基于以下信息，为用户提供一个简洁、专业的回答：\n" + 
                    answerBuilder.toString() + "\n\n用户问题：" + question;
            Prompt aiPrompt = new Prompt(summaryPrompt);
            var response = client.prompt(aiPrompt).call();
            answerBuilder = new StringBuilder(response.content());
        }
        
        return answerBuilder.toString();
    }

    private String extractDrugName(String question) {
        // 简单的药品名称提取（实际可以使用NLP或正则）
        String[] drugKeywords = {"降压药", "退烧药", "感冒药", "止痛药"};
        for (String keyword : drugKeywords) {
            if (question.contains(keyword)) {
                return keyword;
            }
        }
        // 尝试提取药品名称（简单实现）
        if (question.contains("什么药") || question.contains("哪种药")) {
            return "相关药品";
        }
        return null;
    }

    private String extractLocation(String question) {
        // 简单提取位置信息
        if (question.contains("附近") || question.contains("周边")) {
            return "当前位置";
        }
        return null;
    }

    private String extractDepartment(String question) {
        // 提取科室信息
        String[] departments = {"心内科", "内分泌科", "儿科", "急诊科"};
        for (String dept : departments) {
            if (question.contains(dept)) {
                return dept;
            }
        }
        return null;
    }

    private String extractKeyword(String question) {
        // 提取关键词用于知识库搜索
        String[] keywords = {"发烧", "高血压", "糖尿病", "感冒", "咳嗽", "头痛"};
        for (String keyword : keywords) {
            if (question.contains(keyword)) {
                return keyword;
            }
        }
        return question.substring(0, Math.min(10, question.length()));
    }

    private List<String> extractSources(String answer) {
        // 提取知识来源
        List<String> sources = new ArrayList<>();
        if (answer.contains("指南") || answer.contains("防治指南")) {
            sources.add("医学指南");
        }
        if (answer.contains("知识库")) {
            sources.add("健康知识库");
        }
        return sources;
    }

    private ConsultationResponse toResponse(HealthConsultation consultation) {
        Map<String, Object> context = null;
        List<String> toolsUsed = null;
        List<String> sources = null;
        
        try {
            if (consultation.getContextJson() != null) {
                context = objectMapper.readValue(
                        consultation.getContextJson(), 
                        new TypeReference<Map<String, Object>>() {}
                );
            }
            if (consultation.getToolsUsed() != null) {
                toolsUsed = objectMapper.readValue(
                        consultation.getToolsUsed(), 
                        new TypeReference<List<String>>() {}
                );
            }
            if (consultation.getSources() != null) {
                sources = objectMapper.readValue(
                        consultation.getSources(), 
                        new TypeReference<List<String>>() {}
                );
            }
        } catch (Exception e) {
            log.warn("解析咨询记录JSON失败: {}", e.getMessage());
        }
        
        return new ConsultationResponse(
                consultation.getId(),
                consultation.getSessionId(),
                consultation.getQuestion(),
                consultation.getAnswer(),
                context,
                toolsUsed != null ? toolsUsed : List.of(),
                sources != null ? sources : List.of(),
                consultation.getFeedback(),
                consultation.getCreatedAt()
        );
    }

    @Override
    public List<ConsultationResponse> getHistory(Long userId, String sessionId) {
        User user = loadUser(userId);
        List<HealthConsultation> consultations;
        
        if (sessionId != null && !sessionId.isEmpty()) {
            consultations = consultationRepository.findByUser_IdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
        } else {
            consultations = consultationRepository.findByUserOrderByCreatedAtDesc(user);
        }
        
        return consultations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void feedback(Long consultationId, Long userId, Integer feedback) {
        HealthConsultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("咨询记录不存在"));
        
        User user = consultation.getUser();
        if (user == null) {
            throw new RuntimeException("咨询记录对应的用户信息不存在");
        }
        if (!user.getId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        
        consultation.setFeedback(feedback);
        consultationRepository.save(consultation);
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private String writeJsonSafely(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("序列化JSON失败: {}", e.getMessage());
            return "{}";
        }
    }
}

