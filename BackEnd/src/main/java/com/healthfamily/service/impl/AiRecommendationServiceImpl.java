package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.constant.RecommendationCategory;
import com.healthfamily.domain.constant.RecommendationPriority;
import com.healthfamily.domain.entity.AiRecommendation;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.Profile;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.AiRecommendationRepository;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.AiRecommendationService;
import com.healthfamily.service.SecurityService;
import com.healthfamily.ai.OllamaLegacyClient;
import com.healthfamily.web.dto.AiRecommendationRequest;
import com.healthfamily.web.dto.AiRecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.converter.BeanOutputConverter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    private final AiRecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final HealthLogRepository healthLogRepository;
    private final SecurityService securityService;
    private final OllamaLegacyClient ollamaLegacyClient;

    // 定义结构化输出记录
    record RecommendationResult(
        String title,
        String content,
        String reasoning,
        String priority
    ) {}

    @Override
    @Transactional
    public AiRecommendationResponse generateRecommendation(Long userId, AiRecommendationRequest request) {
        // Privacy Check
        String scope = mapCategoryToScope(request.category());
        if (!securityService.isAiAnalysisAllowed(userId, scope)) {
            throw new RuntimeException("用户隐私设置已禁止AI分析此类数据");
        }

        User user = loadUser(userId);
        LocalDate forDate = request.forDate() != null ? request.forDate() : LocalDate.now();
        
        // 获取用户健康数据
        Map<String, Object> healthData = buildHealthDataContext(user, forDate, request.category());
        
        // 准备结构化输出转换器
        BeanOutputConverter<RecommendationResult> converter = new BeanOutputConverter<>(RecommendationResult.class);
        
        // 构建AI提示词
        String promptText = buildRecommendationPrompt(request.category(), healthData) + "\n\n" + converter.getFormat();
        
        // 调用AI生成建议
        ChatClient client = chatClientBuilder.build();
        RecommendationResult recommendation;
        try {
             recommendation = client.prompt()
                .user(promptText)
                .call()
                .entity(RecommendationResult.class);
        } catch (WebClientResponseException.NotFound e) {
            String content = ollamaLegacyClient.generate(promptText, null, null);
            recommendation = parseRecommendationResult(content);
        } catch (Exception e) {
            log.error("AI 生成建议失败", e);
            // 降级处理
            recommendation = new RecommendationResult("健康建议", "暂时无法生成建议，请稍后再试。", "系统繁忙", "MEDIUM");
        }
        
        // 保存建议
        RecommendationPriority priority = RecommendationPriority.MEDIUM;
        try {
            if (recommendation.priority() != null) {
                priority = RecommendationPriority.valueOf(recommendation.priority().toUpperCase());
            }
        } catch (Exception ignored) {}

        AiRecommendation aiReco = AiRecommendation.builder()
                .user(user)
                .forDate(forDate)
                .category(request.category())
                .title(recommendation.title() != null ? recommendation.title() : "健康建议")
                .content(recommendation.content() != null ? recommendation.content() : "无内容")
                .reasoning(recommendation.reasoning())
                .priority(priority)
                .dataSources(writeJsonSafely(healthData))
                .isAccepted(false)
                .aiModel("qwen2.5:7b")
                .promptVersion("v1")
                .build();
        
        aiReco = recommendationRepository.save(aiReco);
        return toResponse(aiReco);
    }

    private Map<String, Object> buildHealthDataContext(User user, LocalDate forDate, RecommendationCategory category) {
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
                context.put("sex", profile.getSex());
                context.put("birthday", profile.getBirthday());
                context.put("heightCm", profile.getHeightCm());
                context.put("weightKg", profile.getWeightKg());
            } catch (Exception e) {
                log.warn("解析用户画像失败: {}", e.getMessage());
            }
        }
        
        // 根据建议类别获取相应类型的健康日志
        HealthLogType logType = mapCategoryToLogType(category);
        List<HealthLog> recentLogs = healthLogRepository
                .findByUser_IdAndTypeOrderByLogDateDesc(user.getId(), logType)
                .stream()
                .filter(log -> !log.getLogDate().isAfter(forDate))
                .limit(10)
                .collect(Collectors.toList());
        
        context.put("recentLogs", formatLogsForAi(recentLogs));
        
        // 获取相应类型的异常数据
        List<HealthLog> abnormalLogs = healthLogRepository
                .findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(user.getId())
                .stream()
                .filter(log -> log.getType() == logType) // 只获取与类别匹配的异常数据
                .filter(log -> !log.getLogDate().isAfter(forDate))
                .limit(5)
                .collect(Collectors.toList());
        
        context.put("abnormalLogs", formatLogsForAi(abnormalLogs));
        
        return context;
    }

    private String formatLogsForAi(List<HealthLog> logs) {
        if (logs.isEmpty()) {
            return "无";
        }
        
        StringBuilder sb = new StringBuilder();
        for (HealthLog log : logs) {
            sb.append(log.getLogDate()).append(" - ").append(log.getType().name());
            try {
                Map<String, Object> content = objectMapper.readValue(
                        log.getContentJson(), 
                        new TypeReference<Map<String, Object>>() {}
                );
                sb.append(": ").append(content);
            } catch (Exception e) {
                sb.append(": ").append(log.getContentJson());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private RecommendationResult parseRecommendationResult(String content) {
        try {
            if (content != null) {
                int jsonStartIndex = content.indexOf("{");
                int jsonEndIndex = content.lastIndexOf("}");
                if (jsonStartIndex != -1 && jsonEndIndex != -1 && jsonEndIndex > jsonStartIndex) {
                    String json = content.substring(jsonStartIndex, jsonEndIndex + 1);
                    return objectMapper.readValue(json, RecommendationResult.class);
                }
            }
        } catch (Exception ignored) {
        }
        return new RecommendationResult("健康建议", "暂时无法生成建议，请稍后再试。", "系统繁忙", "MEDIUM");
    }

    private String buildRecommendationPrompt(RecommendationCategory category, Map<String, Object> healthData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("基于以下用户健康数据，生成一条个性化的").append(category.getDisplayName()).append("建议。\n\n");
        
        prompt.append("用户健康标签：");
        if (healthData.containsKey("healthTags")) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) healthData.get("healthTags");
            prompt.append(String.join("、", tags));
        } else {
            prompt.append("无");
        }
        prompt.append("\n\n");
        
        prompt.append("最近健康日志：\n");
        prompt.append(healthData.getOrDefault("recentLogs", "无"));
        prompt.append("\n\n");
        
        if (healthData.containsKey("abnormalLogs")) {
            prompt.append("异常数据：\n");
            prompt.append(healthData.get("abnormalLogs"));
            prompt.append("\n\n");
        }
        
        prompt.append("请生成一条具体、可操作的建议，要求：\n");
        prompt.append("1. 结合用户的健康标签和最近数据\n");
        prompt.append("2. 建议要具体、可执行\n");
        prompt.append("3. 如果有异常数据，要重点关注\n");
        prompt.append("4. 语气温和、专业\n");
        // JSON格式要求由 Converter 自动添加
        
        return prompt.toString();
    }

    @Override
    public List<AiRecommendationResponse> getUserRecommendations(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = loadUser(userId);
        List<AiRecommendation> recommendations;
        
        if (startDate != null && endDate != null) {
            recommendations = recommendationRepository.findByUser_IdAndForDateBetween(
                    userId, startDate, endDate);
        } else {
            recommendations = recommendationRepository.findByUserOrderByForDateDesc(user);
        }
        
        return recommendations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void feedback(Long recommendationId, Long userId, Integer feedback) {
        AiRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("建议不存在"));
        
        User user = recommendation.getUser();
        if (user == null) {
            throw new RuntimeException("建议对应的用户信息不存在");
        }
        if (!user.getId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        
        recommendation.setFeedback(feedback);
        recommendationRepository.save(recommendation);
    }

    private AiRecommendationResponse toResponse(AiRecommendation recommendation) {
        List<String> dataSources = null;
        try {
            if (recommendation.getDataSources() != null) {
                Map<String, Object> sources = objectMapper.readValue(
                        recommendation.getDataSources(), 
                        new TypeReference<Map<String, Object>>() {}
                );
                dataSources = new ArrayList<>();
                if (sources.containsKey("recentLogs")) {
                    dataSources.add("健康日志");
                }
                if (sources.containsKey("abnormalLogs")) {
                    dataSources.add("异常数据");
                }
                if (sources.containsKey("healthTags")) {
                    dataSources.add("健康标签");
                }
            }
        } catch (Exception e) {
            log.warn("解析数据来源失败: {}", e.getMessage());
        }
        
        return new AiRecommendationResponse(
                recommendation.getId(),
                recommendation.getForDate(),
                recommendation.getCategory().name(),
                recommendation.getTitle(),
                recommendation.getContent(),
                recommendation.getReasoning(),
                recommendation.getPriority().name(),
                dataSources != null ? dataSources : List.of(),
                recommendation.getIsAccepted(),
                recommendation.getFeedback(),
                recommendation.getCreatedAt()
        );
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private HealthLogType mapCategoryToLogType(RecommendationCategory category) {
        return switch (category) {
            case DIET -> HealthLogType.DIET;
            case REST -> HealthLogType.SLEEP;
            case SPORT -> HealthLogType.SPORT;
            case EMOTION -> HealthLogType.MOOD;
            case VITALS -> HealthLogType.VITALS;
            case LIFESTYLE -> HealthLogType.DIET; // 生活方式默认映射到饮食类型
        };
    }
    
    private String mapCategoryToScope(RecommendationCategory category) {
        return switch (category) {
            case DIET -> "diet";
            case VITALS -> "vitals";
            case SPORT -> "sport";
            // Map others to null (global check only) or specific keys if added to frontend
            default -> null; 
        };
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

