package com.healthfamily.service.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.config.RecommendationProperties;
import com.healthfamily.domain.entity.SystemSetting;
import com.healthfamily.domain.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationAiClient {

    private static final TypeReference<AiRecommendationResult> RESULT_TYPE = new TypeReference<>() {};

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    private final RecommendationProperties properties;
    private final SystemSettingRepository systemSettingRepository;

    private PromptTemplate promptTemplate;

    // 添加初始化方法验证日志配置
    @PostConstruct
    public void init() {
        log.debug("=== RecommendationAiClient 初始化，DEBUG 日志已启用 ===");
        log.info("=== RecommendationAiClient 初始化，配置模型: {} ===", properties.getAi().getModel());
    }

    private PromptTemplate promptTemplate() {
        if (promptTemplate == null) {
            try {
                var resource = new ClassPathResource("prompts/recommendation.st");
                String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                promptTemplate = new PromptTemplate(template);
                log.debug("成功加载推荐提示词模板");
            } catch (IOException ex) {
                throw new RecommendationAiException("无法读取推荐提示词模板", ex);
            }
        }
        return promptTemplate;
    }

    public AiRecommendationResult generate(AiRecommendationRequest request) {
        // 1. Check Safety Switch
        String safetyEnabledStr = systemSettingRepository.findByKey("ai.safety.enabled")
                .map(SystemSetting::getValue).orElse("true");
        if ("false".equalsIgnoreCase(safetyEnabledStr)) {
            throw new RecommendationAiException("AI健康建议功能已由管理员禁用");
        }

        log.debug("开始生成 {} 类别的建议", request.category());
        Map<String, Object> variables = buildVariables(request, false);
        try {
            return invokeModel(request, variables);
        } catch (RecommendationAiException ex) {
            log.warn("AI初次生成{}类别建议失败，尝试重试: {}", request.category(), ex.getMessage());
            Map<String, Object> retryVariables = buildVariables(request, true);
            return invokeModel(request, retryVariables);
        }
    }

    private AiRecommendationResult invokeModel(AiRecommendationRequest request, Map<String, Object> variables) {
        try {
            Prompt prompt = promptTemplate().create(variables);
            ChatClient client = chatClientBuilder.build();
            var callSpec = client.prompt(prompt);

            String model = request.modelOverride() != null ? request.modelOverride() : properties.getAi().getModel();
            log.debug("使用模型: {}", model);

            if (model != null && !model.isBlank()) {
                callSpec = callSpec.options(ChatOptions.builder().model(model).build());
            }

            log.debug("开始调用 AI 模型...");
            var response = callSpec.call();
            String content = response.content();

            if (content == null || content.isBlank()) {
                throw new RecommendationAiException("模型未返回内容");
            }

            // 关键日志：显示原始返回内容
            log.debug("========================================");
            log.debug("AI模型 {} 返回原始内容:", model);
            log.debug("{}", content);
            log.debug("========================================");

            AiRecommendationResult payload = parseResult(content);
            log.debug("成功解析 AI 返回结果");

            return new AiRecommendationResult(
                    payload.title(),
                    payload.summary(),
                    payload.items(),
                    payload.evidence(),
                    payload.safety(),
                    content
            );
        } catch (RecommendationAiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("生成个性化建议时发生异常", ex);
            throw new RecommendationAiException("生成个性化建议失败", ex);
        }
    }

    private Map<String, Object> buildVariables(AiRecommendationRequest request, boolean retry) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("category", request.category().name());
        variables.put("categoryLabel", request.category().getDisplayName());
        variables.put("strictMode", request.strictMode());
        variables.put("maxItems", request.maxItems());

        RecommendationContext context = request.context();
        variables.put("profile", context.profile());
        variables.put("assessment", context.assessment());
        variables.put("logsSummary", context.logsSummary());
        variables.put("logsStructured", context.logsStructured());
        variables.put("preferences", context.preferences());
        variables.put("promptVersion", properties.getAi().getPromptVersion());
        variables.put("retryMessage", retry
                ? "\n[系统提醒]\n上一次回答未按 JSON 输出，请务必严格按照上方模板返回，不能包含多余文字。"
                : "");

        if (retry) {
            log.debug("这是重试请求，已添加重试提示");
        }

        return variables;
    }

    private AiRecommendationResult parseResult(String content) {
        try {
            return objectMapper.readValue(content, RESULT_TYPE);
        } catch (JsonProcessingException ex) {
            log.warn("AI输出不是合法JSON，将尝试从内容中提取: {}", content);
            String normalized = extractJson(content);
            log.debug("提取后的JSON: {}", normalized);
            try {
                return objectMapper.readValue(normalized, RESULT_TYPE);
            } catch (JsonProcessingException e) {
                log.error("解析失败，原始内容: {}", content);
                log.error("提取后的内容: {}", normalized);
                throw new RecommendationAiException("无法解析模型返回的内容", e);
            }
        }
    }

    private String extractJson(String content) {
        // 移除可能的 markdown 代码块标记
        content = content.trim();
        content = content.replaceAll("^```json\\s*", "");
        content = content.replaceAll("^```\\s*", "");
        content = content.replaceAll("```\\s*$", "");

        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }
}