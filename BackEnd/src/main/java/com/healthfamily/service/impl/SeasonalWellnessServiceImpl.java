package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.SeasonalWellnessService;
import com.healthfamily.ai.OllamaLegacyClient;
import com.healthfamily.utils.SolarTermUtil;
import com.healthfamily.web.dto.SeasonalWellnessDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonalWellnessServiceImpl implements SeasonalWellnessService {

    private final ConstitutionAssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OllamaLegacyClient ollamaLegacyClient;

    @Override
    public SeasonalWellnessDTO getWellnessAdvice(Long userId) {
        String todayDate = LocalDate.now().toString();
        String solarTerm = SolarTermUtil.getCurrentSolarTerm();
        boolean isSolarTermDay = SolarTermUtil.isSolarTermDate(LocalDate.now());
        
        // 尝试从Redis获取缓存
        String cacheKey = "wellness:uid:" + userId + ":date:" + todayDate;
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedValue != null) {
            try {
                return objectMapper.readValue(cachedValue, SeasonalWellnessDTO.class);
            } catch (JsonProcessingException e) {
                log.error("Error parsing cached wellness advice", e);
                // 缓存解析失败，继续走生成逻辑
            }
        }

        // 获取用户体质
        String constitution = getUserConstitution(userId);

        // 生成建议
        String advice = generateAiAdvice(solarTerm, constitution, isSolarTermDay);

        // 构建DTO
        SeasonalWellnessDTO dto = SeasonalWellnessDTO.builder()
                .solarTerm(solarTerm)
                .constitution(constitution)
                .advice(advice)
                .imageUrl(getSolarTermImage(solarTerm))
                .build();

        // 存入缓存
        cacheResult(cacheKey, dto);

        return dto;
    }

    @Override
    public Flux<ServerSentEvent<String>> getWellnessAdviceStream(Long userId) {
        String todayDate = LocalDate.now().toString();
        String solarTerm = SolarTermUtil.getCurrentSolarTerm();
        boolean isSolarTermDay = SolarTermUtil.isSolarTermDate(LocalDate.now());
        
        // 1. 获取用户体质
        String constitution = getUserConstitution(userId);
        log.info("Starting wellness advice stream for userId: {}, constitution: {}, solarTerm: {}", userId, constitution, solarTerm);
        
        // 2. 准备元数据
        String imageUrl = getSolarTermImage(solarTerm);
        Map<String, String> metadata = Map.of(
            "solarTerm", solarTerm,
            "constitution", constitution != null ? constitution : "",
            "imageUrl", imageUrl
        );
        
        // 3. 检查缓存
        // 使用 v2 前缀以强制刷新旧缓存
        String cacheKey = "wellness:v2:uid:" + userId + ":date:" + todayDate;
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedValue != null) {
            try {
                log.info("Cache hit for wellness advice, userId: {}", userId);
                SeasonalWellnessDTO cachedDto = objectMapper.readValue(cachedValue, SeasonalWellnessDTO.class);
                return Flux.just(
                    ServerSentEvent.<String>builder().event("meta").data(writeJson(metadata)).build(),
                    ServerSentEvent.<String>builder().event("data").data(cachedDto.getAdvice()).build(),
                    ServerSentEvent.<String>builder().event("complete").data("").build()
                );
            } catch (Exception e) {
                log.error("Cache parse error", e);
            }
        }
        
        // 4. AI 流式生成
        String promptText = buildPromptText(solarTerm, constitution, isSolarTermDay);
        log.info("Cache miss. Calling AI for wellness advice. Prompt: {}", promptText);
        StringBuilder accumulatedAdvice = new StringBuilder();
        
        return Flux.concat(
            Flux.just(ServerSentEvent.<String>builder().event("meta").data(writeJson(metadata)).build()),
            
            callStreamWithFallback(promptText)
                .map(chunk -> {
                    if (chunk != null) {
                        accumulatedAdvice.append(chunk);
                        log.debug("AI Stream chunk for userId {}: {}", userId, chunk);
                    }
                    return ServerSentEvent.<String>builder().event("data").data(chunk != null ? chunk : "").build();
                })
                .doOnError(e -> log.error("Error during AI streaming for userId " + userId, e))
                .doOnComplete(() -> log.info("AI streaming completed for userId: {}", userId)),
                
            Flux.just(ServerSentEvent.<String>builder().event("complete").data("").build())
                .doOnNext(signal -> {
                    // 完成后缓存
                    String finalAdvice = accumulatedAdvice.toString();
                    log.info("Caching AI generated advice for userId: {}, length: {}", userId, finalAdvice.length());
                    SeasonalWellnessDTO dto = SeasonalWellnessDTO.builder()
                            .solarTerm(solarTerm)
                            .constitution(constitution)
                            .advice(finalAdvice)
                            .imageUrl(imageUrl)
                            .build();
                    cacheResult(cacheKey, dto);
                })
        );
    }

    private String getUserConstitution(Long userId) {
        if (userId == null) return null;
        try {
            return userRepository.findById(userId)
                    .map(user -> assessmentRepository.findByUserOrderByCreatedAtDesc(user))
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0).getPrimaryType())
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Failed to get user constitution: {}", e.getMessage());
            return null;
        }
    }

    private String buildPromptText(String solarTerm, String constitution, boolean isSolarTermDay) {
        String dateDesc = isSolarTermDay ? "今天是" + solarTerm : "今天正处于" + solarTerm + "节气期间（非" + solarTerm + "当天）";
        if (constitution != null && !constitution.isEmpty()) {
            return String.format("%s，用户的中医体质是%s。请作为老中医，给出一段100字左右的饮食（推荐2种食材）和起居建议。不要说“今天是%s”，要说“时值%s”或“正值%s期间”。语气亲切，口语化。", dateDesc, constitution, solarTerm, solarTerm, solarTerm);
        } else {
            return String.format("%s。请作为老中医，给出一段100字左右的通用大众养生建议。不要说“今天是%s”，要说“时值%s”或“正值%s期间”。语气亲切，口语化。", dateDesc, solarTerm, solarTerm, solarTerm);
        }
    }

    private String generateAiAdvice(String solarTerm, String constitution, boolean isSolarTermDay) {
        String promptText = buildPromptText(solarTerm, constitution, isSolarTermDay);
        return callTextWithFallback(promptText);
    }

    private String callTextWithFallback(String prompt) {
        try {
            ChatClient client = chatClientBuilder.build();
            return client.prompt().user(prompt).call().content();
        } catch (WebClientResponseException.NotFound ex) {
            return ollamaLegacyClient.generate(prompt, null, null);
        }
    }

    private Flux<String> callStreamWithFallback(String prompt) {
        Flux<String> stream = chatClientBuilder.build().prompt().user(prompt).stream().content();
        return stream.onErrorResume(WebClientResponseException.NotFound.class,
                ex -> ollamaLegacyClient.generateStream(prompt, null, null));
    }

    private void cacheResult(String key, SeasonalWellnessDTO dto) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(dto), 24, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Error caching wellness advice", e);
        }
    }
    
    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String getSolarTermImage(String solarTerm) {
        // 简单映射，实际项目中可以更丰富
        // 这里返回一个相对路径，前端需要处理
        // 假设前端有 assets/solar-terms/ 目录
        // 简单起见，可以映射四季
        if (solarTerm.contains("春")) return "/assets/solar-terms/spring.jpg";
        if (solarTerm.contains("夏") || solarTerm.contains("满") || solarTerm.contains("暑")) return "/assets/solar-terms/summer.jpg";
        if (solarTerm.contains("秋") || solarTerm.contains("露") || solarTerm.contains("霜")) return "/assets/solar-terms/autumn.jpg";
        return "/assets/solar-terms/winter.jpg";
    }
}
