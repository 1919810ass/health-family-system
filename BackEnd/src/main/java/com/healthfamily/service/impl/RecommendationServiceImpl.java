package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.config.RecommendationProperties;
import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.constant.RecommendationCategory;
import com.healthfamily.domain.constant.RecommendationStatus;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.Profile;
import com.healthfamily.domain.entity.Recommendation;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.domain.repository.RecommendationRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.RecommendationService;
import com.healthfamily.service.recommendation.AiRecommendationRequest;
import com.healthfamily.service.recommendation.AiRecommendationResult;
import com.healthfamily.service.recommendation.RecommendationAiClient;
import com.healthfamily.service.recommendation.RecommendationAiException;
import com.healthfamily.service.recommendation.RecommendationContext;
import com.healthfamily.web.dto.RecommendationFeedbackRequest;
import com.healthfamily.web.dto.RecommendationGenerateRequest;
import com.healthfamily.web.dto.RecommendationGenerateResponse;
import com.healthfamily.web.dto.RecommendationItemDto;
import com.healthfamily.web.dto.RecommendationResponse;
import com.healthfamily.web.dto.RecommendationSafetyDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final TypeReference<List<RecommendationItemDto>> ITEMS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<String>> STRINGS_TYPE = new TypeReference<>() {
    };
    private static final String VERSION = "v2";

    private final RecommendationRepository recommendationRepository;
    private final ConstitutionAssessmentRepository assessmentRepository;
    private final HealthLogRepository healthLogRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RecommendationAiClient aiClient;
    private final RecommendationProperties properties;

    @Override
    @Transactional
    public RecommendationGenerateResponse generate(Long userId, RecommendationGenerateRequest request) {
        User user = loadUser(userId);
        LocalDate targetDate = Optional.ofNullable(request.date()).orElse(LocalDate.now());
        List<RecommendationCategory> categories = resolveCategories(request);
        List<RecommendationResponse> responses = new ArrayList<>();

        for (RecommendationCategory category : categories) {
            RecommendationCategory safeCategory = category != null ? category : RecommendationCategory.DIET;
            RecommendationContext categoryContext = buildContext(user, targetDate, safeCategory); // 为每个类别构建特定上下文
            responses.add(generateSingle(user, targetDate, safeCategory, categoryContext, request));
        }

        return new RecommendationGenerateResponse(responses);
    }

    @Override
    public List<RecommendationResponse> list(Long userId, LocalDate date, RecommendationCategory category) {
        User user = loadUser(userId);
        LocalDate targetDate = Optional.ofNullable(date).orElse(LocalDate.now());
        return recommendationRepository.findByUserAndForDate(user, targetDate).stream()
                .filter(rec -> category == null || rec.getCategory() == category)
                .sorted(Comparator.comparing(Recommendation::getCreatedAt).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecommendationResponse feedback(Long userId, Long recommendationId, RecommendationFeedbackRequest request) {
        User user = loadUser(userId);
        Recommendation recommendation = recommendationRepository.findByIdAndUser(recommendationId, user)
                .orElseThrow(() -> new BusinessException(40422, "建议不存在"));
        recommendation.setAccepted(request.accepted());
        Recommendation saved = recommendationRepository.save(recommendation);
        return toResponse(saved);
    }

    private RecommendationResponse generateSingle(User user,
                                                  LocalDate targetDate,
                                                  RecommendationCategory category,
                                                  RecommendationContext context,
                                                  RecommendationGenerateRequest request) {
        int limit = Optional.ofNullable(request.maxItems()).orElse(properties.getAi().getPerCategoryLimit());
        boolean strictMode = Optional.ofNullable(request.strictMode()).orElse(properties.getAi().isStrictModeDefault());

        AiRecommendationResult result;
        RecommendationStatus status = RecommendationStatus.READY;
        try {
            result = aiClient.generate(new AiRecommendationRequest(category, context, limit, strictMode, request.model()));
        } catch (RecommendationAiException ex) {
            log.warn("AI生成{}类别建议失败，使用兜底: {}", category, ex.getMessage());
            status = RecommendationStatus.FALLBACK;
            result = fallbackResult(category, context);
        }

        AiRecommendationResult validated = validateAndCorrect(category, result);
        debugCompareItems(result, validated);

        List<RecommendationItemDto> items = mapItems(validated.items(), category, limit);
        List<String> evidence = validated.evidence() != null ? validated.evidence() : List.of();
        RecommendationSafetyDto safety = toSafetyDto(validated.safety());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("summary", result.summary());
        metadata.put("safety", safety);
        if (StringUtils.hasText(result.raw())) {
            metadata.put("raw", result.raw());
        }

        Recommendation existing = recommendationRepository.findByUserAndForDateAndCategory(user, targetDate, category).orElse(null);
        Recommendation entity;
        if (existing != null) {
            existing.setItemsJson(toJson(items));
            existing.setEvidenceJson(toJson(evidence));
            existing.setStatus(status);
            existing.setAiModel(resolveModel(request));
            existing.setPromptVersion(properties.getAi().getPromptVersion());
            existing.setMetadataJson(toJson(metadata));
            existing.setVersion(VERSION);
            if (existing.getCreatedAt() == null) {
                existing.setCreatedAt(LocalDateTime.now());
            }
            existing.setAccepted(Boolean.FALSE);
            entity = existing;
        } else {
            entity = Recommendation.builder()
                    .user(user)
                    .forDate(targetDate)
                    .category(category)
                    .itemsJson(toJson(items))
                    .evidenceJson(toJson(evidence))
                    .version(VERSION)
                    .status(status)
                    .aiModel(resolveModel(request))
                    .promptVersion(properties.getAi().getPromptVersion())
                    .metadataJson(toJson(metadata))
                    .accepted(Boolean.FALSE)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        Recommendation saved = recommendationRepository.save(entity);
        return toResponse(saved, items, evidence, safety);
    }

    private AiRecommendationResult validateAndCorrect(RecommendationCategory category, AiRecommendationResult result) {
        String safeTitle = defaultString(result.title(), category.getDisplayName() + "建议");
        String safeSummary = defaultString(result.summary(), "");
        List<String> evidence = result.evidence() != null ? result.evidence() : List.of();
        AiRecommendationResult.Safety safety = result.safety() != null ? result.safety() : new AiRecommendationResult.Safety(false, null);

        List<AiRecommendationResult.Item> srcItems = result.items() != null ? result.items() : List.of();
        List<AiRecommendationResult.Item> fixedItems = srcItems.stream().map(it -> {
            String title = defaultString(it.title(), category.getDisplayName() + "建议");
            String content = defaultString(it.content(), "保持良好的生活方式，遵循医生建议。");
            String priority = normalizePriority(it.priority());
            List<String> sourceTags = it.sourceTags() != null && !it.sourceTags().isEmpty() ? it.sourceTags() : List.of();
            Double confidence = it.confidence() != null ? clamp(it.confidence(), 0.0, 1.0) : 0.5;

            if (!sourceTags.contains("ai")) {
                sourceTags = new ArrayList<>(sourceTags);
                sourceTags.add("ai");
            }
            if (!evidence.isEmpty() && !sourceTags.contains("logs")) {
                sourceTags = new ArrayList<>(sourceTags);
                sourceTags.add("logs");
            }

            boolean matches = matchesEvidence(category, content, evidence);
            if (!matches) {
                confidence = Math.min(confidence, 0.5);
                priority = "MEDIUM";
                if (!sourceTags.contains("mismatch")) {
                    sourceTags = new ArrayList<>(sourceTags);
                    sourceTags.add("mismatch");
                }
            } else {
                confidence = Math.max(confidence, category == RecommendationCategory.EMOTION ? 0.6 : 0.65);
            }

            if (category == RecommendationCategory.EMOTION) {
                if (containsAny(evidence, List.of("焦虑", "压力", "紧张", "心情低落"))) {
                    priority = "HIGH";
                }
            }
            if (category == RecommendationCategory.DIET) {
                if (containsAny(evidence, List.of("油炸", "重盐", "高糖", "夜宵", "甜食", "饮料"))) {
                    priority = "HIGH";
                }
            }

            return new AiRecommendationResult.Item(title, content, priority, sourceTags, confidence);
        }).toList();

        return new AiRecommendationResult(safeTitle, safeSummary, fixedItems, evidence, safety, result.raw());
    }

    private void debugCompareItems(AiRecommendationResult before, AiRecommendationResult after) {
        try {
            Map<String, Object> b = Map.of(
                    "title", before.title(),
                    "summary", before.summary(),
                    "items", before.items(),
                    "evidence", before.evidence(),
                    "safety", before.safety()
            );
            Map<String, Object> a = Map.of(
                    "title", after.title(),
                    "summary", after.summary(),
                    "items", after.items(),
                    "evidence", after.evidence(),
                    "safety", after.safety()
            );
            log.debug("AI建议结构校验前: {}", objectMapper.writeValueAsString(b));
            log.debug("AI建议结构校验后: {}", objectMapper.writeValueAsString(a));
            List<?> beforeItems = before.items() != null ? before.items() : List.of();
            List<?> afterItems = after.items() != null ? after.items() : List.of();
            log.debug("AI建议校验差异: items数量 {} -> {}", beforeItems.size(), afterItems.size());
        } catch (Exception e) {
            log.debug("AI建议结构校验日志输出失败: {}", e.getMessage());
        }
    }

    private String normalizePriority(String p) {
        if (!StringUtils.hasText(p)) return "MEDIUM";
        String up = p.toUpperCase();
        return switch (up) {
            case "HIGH", "MEDIUM", "LOW" -> up;
            default -> "MEDIUM";
        };
    }

    private Double clamp(Double v, double min, double max) {
        if (v == null) return min;
        return Math.max(min, Math.min(max, v));
    }

    private boolean matchesEvidence(RecommendationCategory category, String content, List<String> evidence) {
        if (evidence == null || evidence.isEmpty()) return true;
        String c = defaultString(content, "").toLowerCase();
        List<String> keys = category == RecommendationCategory.DIET
                ? List.of("油", "盐", "糖", "夜宵", "甜", "炸", "煎", "饮料", "碳水", "米饭", "肉", "蔬菜", "水果")
                : List.of("焦虑", "紧张", "压力", "放松", "冥想", "正念", "情绪", "心情", "睡眠");
        boolean contentHas = keys.stream().anyMatch(k -> c.contains(k));
        boolean evidenceHas = containsAny(evidence, keys);
        return contentHas || evidenceHas;
    }

    private boolean containsAny(List<String> lines, List<String> keys) {
        if (lines == null || lines.isEmpty()) return false;
        return lines.stream().anyMatch(line -> {
            String l = defaultString(line, "");
            return keys.stream().anyMatch(l::contains);
        });
    }

    private List<RecommendationCategory> resolveCategories(RecommendationGenerateRequest request) {
        if (request.categories() == null || request.categories().isEmpty()) {
            return List.of(RecommendationCategory.values());
        }
        return request.categories();
    }

    private String resolveModel(RecommendationGenerateRequest request) {
        if (request.model() != null && !request.model().isBlank()) {
            return request.model();
        }
        String configured = properties.getAi().getModel();
        return StringUtils.hasText(configured) ? configured : null;
    }

    private List<RecommendationItemDto> mapItems(List<AiRecommendationResult.Item> source,
                                                 RecommendationCategory category,
                                                 int limit) {
        if (source == null || source.isEmpty()) {
            return List.of(defaultFallbackItem(category));
        }
        return source.stream()
                .filter(Objects::nonNull)
                .limit(limit)
                .map(item -> new RecommendationItemDto(
                        defaultString(item.title(), category.getDisplayName() + "建议"),
                        defaultString(item.content(), "保持良好的生活方式，遵循医生建议。"),
                        item.priority(),
                        item.sourceTags() != null && !item.sourceTags().isEmpty() ? item.sourceTags() : List.of("AI"),
                        item.confidence()
                ))
                .toList();
    }

    private RecommendationItemDto defaultFallbackItem(RecommendationCategory category) {
        String title = category.getDisplayName() + "基础建议";
        String content = "暂未生成个性化建议，请保持良好习惯，如规律作息、均衡饮食，若有不适请咨询专业医生。";
        return new RecommendationItemDto(title, content, "MEDIUM", List.of("fallback"), 0.3);
    }

    private RecommendationSafetyDto toSafetyDto(AiRecommendationResult.Safety safety) {
        if (safety == null) {
            return RecommendationSafetyDto.safe();
        }
        return new RecommendationSafetyDto(safety.refuse(), safety.message());
    }

    private AiRecommendationResult fallbackResult(RecommendationCategory category, RecommendationContext context) {
        RuleBasedSuggestion suggestion = buildSuggestionFromLogs(category, context);
        List<AiRecommendationResult.Item> items = suggestion.items().stream()
                .map(item -> new AiRecommendationResult.Item(
                        item.title(),
                        item.content(),
                        item.priority(),
                        item.sourceTags(),
                        item.confidence()
                ))
                .toList();
        String summary = suggestion.summary();
        return new AiRecommendationResult(
                summary,
                summary,
                items,
                suggestion.evidence(),
                new AiRecommendationResult.Safety(false, null),
                summary
        );
    }

    private RuleBasedSuggestion buildSuggestionFromLogs(RecommendationCategory category, RecommendationContext context) {
        List<HealthLog> logs = context.recentLogs() != null ? context.recentLogs() : List.of();
        HealthLogType focusType = mapCategoryToLogType(category);
        List<HealthLog> relevant = logs.stream()
                .filter(log -> focusType == null || log.getType() == focusType)
                .sorted(Comparator.comparing(HealthLog::getLogDate).reversed())
                .limit(5)
                .toList();
        if (relevant.isEmpty()) {
            RecommendationItemDto item = defaultFallbackItem(category);
            return new RuleBasedSuggestion(item.content(), List.of(item), List.of("系统兜底提示，建议保持基础健康习惯"));
        }

        List<String> evidence = relevant.stream()
                .map(log -> {
                    Map<String, Object> content = readContent(log.getContentJson());
                    String note = defaultString(String.valueOf(content.getOrDefault("note", "未记录感受")), "未记录感受");
                    String time = defaultString(String.valueOf(content.getOrDefault("time", "--:--")), "--:--");
                    return log.getLogDate() + " " + time + "：" + note;
                })
                .toList();
        String detail = String.join("；", evidence);
        String advice = buildAdvice(category, detail);
        RecommendationItemDto item = new RecommendationItemDto(
                category.getDisplayName() + "建议",
                advice,
                "MEDIUM",
                List.of("logs"),
                0.6
        );
        return new RuleBasedSuggestion(advice, List.of(item), evidence);
    }

    private String buildAdvice(RecommendationCategory category, String detail) {
        return switch (category) {
            case DIET -> "近期饮食记录显示：" + detail + "。建议保持清淡饮食，规律三餐，并适当补充蔬菜水果。";
            case REST -> "近期睡眠记录显示：" + detail + "。建议保持规律作息，睡前远离电子设备，营造良好睡眠环境。";
            case SPORT -> "近期运动记录显示：" + detail + "。建议根据自身状态安排温和有氧运动，并注意热身与拉伸。";
            case EMOTION -> "近期情绪记录显示：" + detail + "。建议适当调节压力，进行放松训练，与家人朋友多沟通。";
            case VITALS -> "近期体征数据显示：" + detail + "。建议关注身体指标变化，如有异常及时就医。";
            case LIFESTYLE -> "近期生活方式记录显示：" + detail + "。建议保持健康的生活习惯，注意劳逸结合。";
        };
    }

    private Map<String, Object> readContent(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException ex) {
            log.warn("解析日志内容失败: {}", ex.getMessage());
            return Map.of();
        }
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

    private RecommendationResponse toResponse(Recommendation recommendation) {
        List<RecommendationItemDto> items = parseItems(recommendation.getItemsJson());
        List<String> evidence = parseEvidence(recommendation.getEvidenceJson());
        RecommendationSafetyDto safety = extractSafety(recommendation.getMetadataJson());
        return toResponse(recommendation, items, evidence, safety);
    }

    private RecommendationResponse toResponse(Recommendation recommendation,
                                              List<RecommendationItemDto> items,
                                              List<String> evidence,
                                              RecommendationSafetyDto safety) {
        return new RecommendationResponse(
                recommendation.getId(),
                recommendation.getForDate(),
                recommendation.getCategory(),
                recommendation.getCategory().getDisplayName(),
                recommendation.getCategory().getTagType(),
                items,
                evidence,
                safety,
                recommendation.getAiModel(),
                recommendation.getPromptVersion(),
                recommendation.getVersion(),
                recommendation.getStatus() != null ? recommendation.getStatus().name() : RecommendationStatus.READY.name(),
                recommendation.getAccepted(),
                recommendation.getCreatedAt()
        );
    }

    private RecommendationContext buildContext(User user, LocalDate targetDate, RecommendationCategory category) {
        Profile profile = profileRepository.findById(user.getId()).orElse(null);
        
        // 隐私检查：是否允许AI分析
        if (profile != null && !canShareToAi(profile, category)) {
            log.info("用户禁止AI分析该类型数据: userId={}, category={}", user.getId(), category);
            return new RecommendationContext(
                "用户隐私设置禁止AI访问详细档案", 
                "隐私保护中", 
                "隐私保护中", 
                "隐私保护中", 
                "[]", 
                Collections.emptyList()
            );
        }

        String profileSummary = formatProfile(user, profile);
        String assessmentSummary = assessmentRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .findFirst()
                .map(this::formatAssessment)
                .orElse("最近无体质评估记录");
        
        // 根据建议类别获取相应类型的健康日志
        HealthLogType logType = mapCategoryToLogType(category);
        List<HealthLog> recentLogs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(user.getId(), logType)
                .stream()
                .filter(log -> !log.getLogDate().isBefore(targetDate.minusDays(6)))
                .filter(log -> !log.getLogDate().isAfter(targetDate))
                .collect(Collectors.toList());
        
        String logsSummary = recentLogs.isEmpty()
                ? "最近7天无" + category.getDisplayName() + "相关健康日志记录"
                : recentLogs.stream()
                .sorted(Comparator.comparing(HealthLog::getLogDate).reversed())
                .limit(20)
                .map(this::formatLog)
                .collect(Collectors.joining("\n"));
        String preferenceSummary = formatPreferences(profile);
        String logsStructured = toJsonQuiet(transformLogs(recentLogs));
        
        // 调试日志：已脱敏，仅记录摘要长度
        log.debug("构建AI上下文: profileLen={}, assessmentLen={}, logsCount={}", 
                profileSummary.length(), assessmentSummary.length(), recentLogs.size());
        
        return new RecommendationContext(profileSummary, assessmentSummary, logsSummary, preferenceSummary, logsStructured, recentLogs);
    }

    private boolean canShareToAi(Profile profile, RecommendationCategory category) {
        if (profile.getPreferences() == null || profile.getPreferences().isBlank()) return true; // 默认允许
        try {
            Map<String, Object> prefs = objectMapper.readValue(profile.getPreferences(), new TypeReference<Map<String, Object>>() {});
            Object shareToAi = prefs.get("shareToAi");
            if (shareToAi instanceof Boolean && !(Boolean)shareToAi) {
                return false; // 全局禁止
            }
            
            // 细粒度检查
            Object scopesObj = prefs.get("aiScopes");
            if (scopesObj instanceof Map) {
                Map<String, Boolean> scopes = (Map<String, Boolean>) scopesObj;
                String scopeKey = getScopeKey(category);
                // 只有明确设置为 false 才禁止，否则默认允许
                if (scopes.containsKey(scopeKey)) {
                    Object val = scopes.get(scopeKey);
                    if (val instanceof Boolean && !(Boolean)val) return false;
                }
            }
            return true;
        } catch (Exception e) {
            return true; // 解析失败默认允许，避免阻断服务
        }
    }
    
    private String getScopeKey(RecommendationCategory category) {
        return switch (category) {
            case DIET -> "diet";
            case VITALS -> "vitals";
            case SPORT -> "sport";
            case REST -> "sleep";
            case EMOTION -> "mood";
            default -> "other";
        };
    }

    private String formatProfile(User user, Profile profile) {
        List<String> parts = new ArrayList<>();
        parts.add("昵称:" + defaultString(user.getNickname(), "未填写"));
        if (profile == null) {
            parts.add("暂无完善的个人档案");
            return String.join("\n", parts);
        }

        // 基础信息
        parts.add("\n【基础信息】");
        if (profile.getSex() != null) {
            parts.add("性别:" + (profile.getSex().name().equals("M") ? "男" : profile.getSex().name().equals("F") ? "女" : "其他"));
        }
        if (profile.getBirthday() != null) {
            int age = Period.between(profile.getBirthday(), LocalDate.now()).getYears();
            parts.add("年龄:" + age + "岁");
        }
        if (profile.getHeightCm() != null) {
            parts.add("身高:" + profile.getHeightCm() + "cm");
        }
        if (profile.getWeightKg() != null) {
            parts.add("体重:" + profile.getWeightKg() + "kg");
            // 计算BMI
            if (profile.getHeightCm() != null && profile.getHeightCm().doubleValue() > 0) {
                double heightM = profile.getHeightCm().doubleValue() / 100.0;
                double bmi = profile.getWeightKg().doubleValue() / (heightM * heightM);
                parts.add("BMI:" + String.format("%.1f", bmi));
            }
        }

        // 健康标签（重要：必须考虑这些疾病）
        if (StringUtils.hasText(profile.getHealthTags())) {
            try {
                List<String> healthTags = objectMapper.readValue(profile.getHealthTags(), new TypeReference<List<String>>() {});
                if (!healthTags.isEmpty()) {
                    parts.add("\n【健康标签 - 重要：用户已确诊的疾病，建议必须考虑这些因素】");
                    parts.add("慢性疾病:" + String.join("、", healthTags));
                }
            } catch (JsonProcessingException ex) {
                log.warn("解析健康标签失败: {}", ex.getMessage());
            }
        }

        // 过敏史（重要：必须避免相关风险）
        if (StringUtils.hasText(profile.getAllergies())) {
            try {
                List<String> allergies = objectMapper.readValue(profile.getAllergies(), new TypeReference<List<String>>() {});
                if (!allergies.isEmpty()) {
                    parts.add("\n【过敏史 - 重要：必须避免相关过敏原】");
                    parts.add("过敏原:" + String.join("、", allergies));
                }
            } catch (JsonProcessingException ex) {
                log.warn("解析过敏史失败: {}", ex.getMessage());
            }
        }

        // 生活习惯（重要：建议需符合用户的生活习惯）
        if (StringUtils.hasText(profile.getLifestyle())) {
            try {
                Map<String, Object> lifestyle = objectMapper.readValue(profile.getLifestyle(), new TypeReference<Map<String, Object>>() {});
                List<String> lifestyleParts = new ArrayList<>();
                if (lifestyle.containsKey("diet") && lifestyle.get("diet") != null && !lifestyle.get("diet").toString().isEmpty()) {
                    lifestyleParts.add("饮食习惯:" + lifestyle.get("diet"));
                }
                if (lifestyle.containsKey("exercise") && lifestyle.get("exercise") != null && !lifestyle.get("exercise").toString().isEmpty()) {
                    lifestyleParts.add("运动习惯:" + lifestyle.get("exercise"));
                }
                if (lifestyle.containsKey("sleep") && lifestyle.get("sleep") != null && !lifestyle.get("sleep").toString().isEmpty()) {
                    lifestyleParts.add("睡眠习惯:" + lifestyle.get("sleep"));
                }
                if (lifestyle.containsKey("other") && lifestyle.get("other") != null && !lifestyle.get("other").toString().isEmpty()) {
                    lifestyleParts.add("其他习惯:" + lifestyle.get("other"));
                }
                if (!lifestyleParts.isEmpty()) {
                    parts.add("\n【生活习惯 - 重要：建议需符合用户的实际生活习惯】");
                    parts.addAll(lifestyleParts);
                }
            } catch (JsonProcessingException ex) {
                log.warn("解析生活习惯失败: {}", ex.getMessage());
            }
        }

        // 健康目标（重要：建议需帮助用户达成这些目标）
        if (StringUtils.hasText(profile.getGoals())) {
            try {
                Map<String, Object> goals = objectMapper.readValue(profile.getGoals(), new TypeReference<Map<String, Object>>() {});
                if (!goals.isEmpty()) {
                    List<String> goalList = goals.keySet().stream().collect(Collectors.toList());
                    parts.add("\n【健康目标 - 重要：建议需帮助用户达成这些目标】");
                    parts.add("目标:" + String.join("、", goalList));
                }
            } catch (JsonProcessingException ex) {
                log.warn("解析健康目标失败: {}", ex.getMessage());
            }
        }

        return String.join("\n", parts);
    }

    private String formatAssessment(ConstitutionAssessment assessment) {
        return "主要体质:" + assessment.getPrimaryType() + "；评估时间:" + assessment.getCreatedAt();
    }

    private String formatPreferences(Profile profile) {
        if (profile == null || !StringUtils.hasText(profile.getPreferences())) {
            return "暂无记录";
        }
        try {
            Map<String, Object> map = objectMapper.readValue(profile.getPreferences(), new TypeReference<Map<String, Object>>() {
            });
            return map.entrySet().stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.joining("；"));
        } catch (JsonProcessingException ex) {
            log.warn("解析用户偏好失败: {}", ex.getMessage());
            return "暂无记录";
        }
    }

    private String formatLog(HealthLog log) {
        StringBuilder builder = new StringBuilder();
        builder.append("日期:").append(log.getLogDate())
               .append(" 类型:").append(log.getType().name());
        if (log.getScore() != null) {
            builder.append(" 评分:").append(log.getScore());
        }
        try {
            Map<String, Object> content = fromJson(log.getContentJson());
            if (!content.isEmpty()) {
                builder.append(" 详情:");
                content.forEach((key, value) -> {
                    if (value != null && !value.toString().isEmpty()) {
                        builder.append(" ").append(key).append("=").append(value);
                    }
                });
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return builder.toString();
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException ex) {
            log.warn("解析日志内容失败: {}", ex.getMessage());
            return Map.of();
        }
    }

    private RecommendationSafetyDto extractSafety(String metadataJson) {
        if (!StringUtils.hasText(metadataJson)) {
            return RecommendationSafetyDto.safe();
        }
        try {
            JsonNode node = objectMapper.readTree(metadataJson).get("safety");
            if (node == null || node.isNull()) {
                return RecommendationSafetyDto.safe();
            }
            return objectMapper.treeToValue(node, RecommendationSafetyDto.class);
        } catch (Exception ex) {
            log.warn("解析安全信息失败: {}", ex.getMessage());
            return RecommendationSafetyDto.safe();
        }
    }

    private List<RecommendationItemDto> parseItems(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, ITEMS_TYPE);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(50022, "建议条目解析失败", ex);
        }
    }

    private List<String> parseEvidence(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRINGS_TYPE);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(50022, "建议证据解析失败", ex);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(50021, "建议数据序列化失败", ex);
        }
    }

    private User loadUser(Long userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        }
        throw new BusinessException(40401, "用户ID不能为空");
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String abbreviate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }

    private record RuleBasedSuggestion(String summary, List<RecommendationItemDto> items, List<String> evidence) {
    }

    private List<Map<String, Object>> transformLogs(List<HealthLog> logs) {
        return logs.stream()
                .map(log -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", log.getLogDate());
                    map.put("type", log.getType());
                    map.put("score", log.getScore());
                    map.put("content", readContent(log.getContentJson()));
                    return map;
                })
                .toList();
    }

    private String toJsonQuiet(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }
}

