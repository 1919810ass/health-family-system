package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyMember;
import com.healthfamily.domain.entity.TcmKnowledgeBase;
import com.healthfamily.domain.entity.TcmPersonalizedPlan;
import com.healthfamily.domain.entity.ConstitutionTrendRecord;
import com.healthfamily.domain.entity.FamilyTcmHealthOverview;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.domain.repository.TcmKnowledgeBaseRepository;
import com.healthfamily.domain.repository.TcmPersonalizedPlanRepository;
import com.healthfamily.domain.repository.ConstitutionTrendRecordRepository;
import com.healthfamily.domain.repository.FamilyTcmHealthOverviewRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.AssessmentService;
import com.healthfamily.web.dto.AssessmentHistoryResponse;
import com.healthfamily.web.dto.AssessmentResponse;
import com.healthfamily.web.dto.AssessmentSchemaResponse;
import com.healthfamily.web.dto.AssessmentSubmitRequest;
import com.healthfamily.web.dto.FamilyMemberLatestResponse;
import com.healthfamily.web.dto.TcmPersonalizedPlanResponse;
import com.healthfamily.web.dto.ConstitutionTrendResponse;
import com.healthfamily.web.dto.FamilyTcmHealthOverviewResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {
    
    private final ConstitutionAssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ProfileRepository profileRepository;
    private final TcmKnowledgeBaseRepository tcmKnowledgeBaseRepository;
    private final TcmPersonalizedPlanRepository tcmPersonalizedPlanRepository;
    private final ConstitutionTrendRecordRepository constitutionTrendRecordRepository;
    private final FamilyTcmHealthOverviewRepository familyTcmHealthOverviewRepository;
    private final ObjectMapper objectMapper;
    private final com.healthfamily.ai.TcmAssessmentAiService aiService;

    private static final String DEFAULT_TYPE = "TCM_9";
    private static final TypeReference<Map<String, Double>> SCORE_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> REPORT_TYPE = new TypeReference<>() {};

    private static final List<Map<String, Object>> DEFAULT_DIMENSIONS = buildDefaultDimensions();

    @Override
    public AssessmentSchemaResponse getSchema() {
        return new AssessmentSchemaResponse(
                DEFAULT_TYPE,
                "中医九型体质测评",
                "基于《中医体质分类与判定》标准的九型体质问卷。",
                DEFAULT_DIMENSIONS
        );
    }

    @Override
    @Transactional
    public AssessmentResponse submitAssessment(Long userId, AssessmentSubmitRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));

        String type = Optional.ofNullable(request.type())
                .filter(StringUtils::hasText)
                .map(String::toUpperCase)
                .orElse(DEFAULT_TYPE);

        Map<String, Double> scores = calculateScores(request.answers());
        String primaryType = determinePrimaryType(scores);
        Map<String, Object> report = buildReport(primaryType, scores);

        String scoreJson = toJson(scores);
        String reportJson = toJson(report);

        ConstitutionAssessment assessment = ConstitutionAssessment.builder()
                .user(user)
                .type(type)
                .scoreVector(scoreJson)
                .primaryType(primaryType)
                .reportJson(reportJson)
                .assessmentSource("MANUAL")
                .confidenceScore(calculateConfidence(scores))
                .assessmentVersion("1.0")
                .isPrimary(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ConstitutionAssessment saved = assessmentRepository.save(assessment);
        return toResponse(saved, scores, report);
    }

    @Override
    public AssessmentResponse getAssessment(Long userId, Long assessmentId) {
        ConstitutionAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new BusinessException(40403, "测评不存在"));
        User assessmentUser = assessment.getUser();
        if (assessmentUser == null) {
            throw new BusinessException(40403, "测评对应的用户信息不存在");
        }
        if (!assessmentUser.getId().equals(userId)) {
            throw new BusinessException(40301, "无权查看该测评报告");
        }
        Map<String, Double> scores = fromJson(assessment.getScoreVector(), SCORE_TYPE);
        Map<String, Object> report = fromJson(assessment.getReportJson(), REPORT_TYPE);
        return toResponse(assessment, scores, report);
    }

    @Override
    public List<AssessmentHistoryResponse> listHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        return assessmentRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(assessment -> new AssessmentHistoryResponse(
                        assessment.getId(),
                        assessment.getType(),
                        assessment.getPrimaryType(),
                        fromJson(assessment.getScoreVector(), SCORE_TYPE),
                        assessment.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public java.util.List<FamilyMemberLatestResponse> listFamilyLatest(Long userId, Long familyId) {
        var family = familyRepository.findById(familyId).orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        var user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        // 校验成员权限
        familyMemberRepository.findByFamilyAndUser(family, user).orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        // 仅家庭管理员可查看他人报告，成员仅查看本人；他人需授权 shareToFamily=true
        boolean isAdmin = familyMemberRepository.findByFamilyAndUser(family, user).map(FamilyMember::getAdmin).orElse(false);
        java.util.List<FamilyMemberLatestResponse> result = new java.util.ArrayList<>();
        for (var member : familyMemberRepository.findByFamily(family)) {
            var targetUser = member.getUser();
            boolean isSelf = targetUser.getId().equals(userId);
            boolean allowed = isSelf || (isAdmin && readShareToFamily(targetUser.getId()));
            if (!allowed) continue;
            var list = assessmentRepository.findByUserOrderByCreatedAtDesc(targetUser);
            if (list.isEmpty()) continue;
            var latest = list.get(0);
            var scores = fromJson(latest.getScoreVector(), SCORE_TYPE);
            double conf = calculateConfidence(scores);
            String avatar = readAvatar(targetUser.getId());
            result.add(new FamilyMemberLatestResponse(
                    targetUser.getId(),
                    targetUser.getNickname(),
                    avatar,
                    latest.getId(),
                    latest.getPrimaryType(),
                    conf,
                    latest.getCreatedAt()
            ));
        }
        return result;
    }

    @Override
    public TcmPersonalizedPlanResponse getPersonalizedPlan(Long userId) {
        // 获取用户最新的体质测评
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        
        List<ConstitutionAssessment> assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(user);
        
        if (assessments.isEmpty()) {
            throw new BusinessException(40403, "用户暂无体质测评记录");
        }
        
        ConstitutionAssessment latest = assessments.get(0);
        String primaryConstitution = latest.getPrimaryType();
        
        // 获取对应的中医养生知识
        List<TcmKnowledgeBase> knowledgeList = tcmKnowledgeBaseRepository.findByConstitutionType(primaryConstitution);
        
        // 按类型分类养生建议（默认基于知识库）
        Map<String, List<TcmPersonalizedPlanResponse.PlanItemDto>> planItems = knowledgeList.stream()
            .collect(Collectors.groupingBy(
                kb -> kb.getType().name(),
                Collectors.mapping(kb -> new TcmPersonalizedPlanResponse.PlanItemDto(
                    kb.getTitle(),
                    kb.getContent(),
                    kb.getDifficulty().name(),
                    kb.getTags() != null ? fromJson(kb.getTags(), new TypeReference<List<String>>() {}) : Collections.emptyList(),
                    kb.getContraindications() != null ? fromJson(kb.getContraindications(), new TypeReference<List<String>>() {}) : Collections.emptyList()
                ), Collectors.toList())
            ));
        
        Map<String, String> seasonalRecommendations = new HashMap<>();
        List<String> priorityRecommendations = new ArrayList<>();
        
        // 尝试使用AI生成更个性化的方案
        Map<String, Object> aiPlan = aiService.generatePersonalizedPlan(userId, primaryConstitution, knowledgeList);
        if (aiPlan != null && !aiPlan.isEmpty()) {
            try {
                if (aiPlan.containsKey("planItems")) {
                    Map<String, List<Map<String, Object>>> aiItems = (Map<String, List<Map<String, Object>>>) aiPlan.get("planItems");
                    // 转换AI生成的planItems
                    for (Map.Entry<String, List<Map<String, Object>>> entry : aiItems.entrySet()) {
                        List<TcmPersonalizedPlanResponse.PlanItemDto> dtos = entry.getValue().stream().map(item -> new TcmPersonalizedPlanResponse.PlanItemDto(
                            (String) item.get("title"),
                            (String) item.get("content"),
                            (String) item.getOrDefault("difficulty", "MEDIUM"),
                            (List<String>) item.getOrDefault("tags", Collections.emptyList()),
                            (List<String>) item.getOrDefault("contraindications", Collections.emptyList())
                        )).collect(Collectors.toList());
                        planItems.put(entry.getKey(), dtos);
                    }
                }
                if (aiPlan.containsKey("seasonalRecommendations")) {
                    seasonalRecommendations = (Map<String, String>) aiPlan.get("seasonalRecommendations");
                }
                if (aiPlan.containsKey("priorityRecommendations")) {
                    priorityRecommendations = (List<String>) aiPlan.get("priorityRecommendations");
                }
            } catch (Exception e) {
                // AI生成失败或格式错误，回退到默认逻辑
                System.err.println("AI方案解析失败，回退到默认逻辑: " + e.getMessage());
            }
        }
        
        // 创建并返回响应
        return new TcmPersonalizedPlanResponse(
            null, // ID为null，因为这是实时生成的
            userId,
            primaryConstitution,
            LocalDate.now(),
            planItems,
            seasonalRecommendations,
            priorityRecommendations
        );
    }
    
    @Override
    public ConstitutionTrendResponse getConstitutionTrend(Long userId, int lookbackDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        
        List<ConstitutionAssessment> assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .filter(a -> a.getCreatedAt().isAfter(LocalDateTime.now().minusDays(lookbackDays)))
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .collect(Collectors.toList());
        
        if (assessments.size() < 2) {
            return new ConstitutionTrendResponse(
                false,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyList(),
                "暂无足够的历史数据进行趋势分析",
                Collections.emptyMap(),
                LocalDateTime.now()
            );
        }
        
        // 按时间升序排序，方便前端绘图
        Collections.reverse(assessments); // 变为升序：旧 -> 新
        
        List<String> dates = assessments.stream()
                .map(a -> a.getCreatedAt().toLocalDate().toString())
                .collect(Collectors.toList());

        // 分析体质变化趋势
        Map<String, List<Double>> scoresOverTime = new HashMap<>();
        for (ConstitutionAssessment assessment : assessments) {
            Map<String, Object> vec = fromJson(assessment.getScoreVector(), new TypeReference<Map<String, Object>>() {});
            for (Map.Entry<String, Object> entry : vec.entrySet()) {
                scoresOverTime.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                    .add(norm(entry.getValue()));
            }
        }
        
        Map<String, String> trends = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : scoresOverTime.entrySet()) {
            List<Double> values = entry.getValue();
            if (values.size() >= 2) {
                double oldest = values.get(0);
                double newest = values.get(values.size() - 1);
                if (newest > oldest + 5) {
                    trends.put(entry.getKey(), "上升");
                } else if (newest < oldest - 5) {
                    trends.put(entry.getKey(), "下降");
                } else {
                    trends.put(entry.getKey(), "稳定");
                }
            }
        }
        
        String summary = generateTrendSummary(trends, assessments.get(assessments.size() - 1).getPrimaryType());
        
        // 调用AI生成洞察
        Map<String, Object> insights = aiService.generateTrendInsights(userId, trends, assessments.get(assessments.size() - 1).getPrimaryType());
        
        return new ConstitutionTrendResponse(
            true,
            trends,
            scoresOverTime,
            dates,
            summary,
            insights,
            LocalDateTime.now()
        );
    }
    
    @Override
    public FamilyTcmHealthOverviewResponse getFamilyHealthOverview(Long familyId, Long userId) {
        // 验证用户是否为家庭成员
        var family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        
        // 校验成员权限
        familyMemberRepository.findByFamilyAndUser(family, user)
                .orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        
        List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        
        List<FamilyTcmHealthOverviewResponse.FamilyMemberHealthInfoDto> memberInfos = new ArrayList<>();
        Map<String, Integer> constitutionDistribution = new HashMap<>();
        
        for (FamilyMember member : members) {
            User memberUser = member.getUser();
            
            // 获取成员体质信息
            List<ConstitutionAssessment> assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(memberUser);
            
            FamilyTcmHealthOverviewResponse.FamilyMemberHealthInfoDto info = new FamilyTcmHealthOverviewResponse.FamilyMemberHealthInfoDto(
                memberUser.getId(),
                memberUser.getNickname(),
                member.getRelation(),
                null,
                false,
                Collections.emptyMap()
            );
            
            if (!assessments.isEmpty()) {
                ConstitutionAssessment latest = assessments.get(0);
                info = new FamilyTcmHealthOverviewResponse.FamilyMemberHealthInfoDto(
                    memberUser.getId(),
                    memberUser.getNickname(),
                    member.getRelation(),
                    latest.getPrimaryType(),
                    true,
                    fromJson(latest.getScoreVector(), SCORE_TYPE)
                );
                
                // 统计体质分布
                constitutionDistribution.merge(latest.getPrimaryType(), 1, Integer::sum);
            }
            
            memberInfos.add(info);
        }
        
        // 生成家庭中医健康建议
        String familyRecommendation = generateFamilyRecommendation(constitutionDistribution, memberInfos);
        
        return new FamilyTcmHealthOverviewResponse(
            familyId,
            family.getName(),
            memberInfos.size(),
            memberInfos,
            constitutionDistribution,
            familyRecommendation,
            LocalDate.now()
        );
    }
    
    /**
     * 获取季节性养生建议
     */
    private Map<String, String> getSeasonalRecommendations(String constitutionType) {
        Map<String, String> seasonalRecs = new HashMap<>();
        
        // 根据当前季节和体质类型生成建议
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        
        switch (constitutionType) {
            case "YANG_DEFICIENCY":
                if (month >= 10 || month <= 2) { // 冬季
                    seasonalRecs.put("冬季", "加强温阳保暖，可食用羊肉、韭菜等温热食物，适当艾灸关元、命门等穴位");
                } else { // 夏季
                    seasonalRecs.put("夏季", "避免过度贪凉，适量食用温性食物，注意保护阳气");
                }
                break;
            case "YIN_DEFICIENCY":
                if (month >= 4 && month <= 9) { // 夏秋季
                    seasonalRecs.put("夏秋季", "注重滋阴润燥，多食梨、银耳、百合等，避免熬夜");
                }
                break;
            case "PHLEGM_DAMPNESS":
                if (month >= 6 && month <= 8) { // 夏季
                    seasonalRecs.put("夏季", "注意祛湿健脾，少食生冷，多食薏米、冬瓜等利湿食物");
                }
                break;
            default:
                seasonalRecs.put("四季", "根据季节变化调整生活方式，顺应自然规律");
        }
        
        return seasonalRecs;
    }
    
    /**
     * 获取体质调理优先级建议
     */
    private List<String> getPriorityRecommendations(ConstitutionAssessment assessment) {
        List<String> priorities = new ArrayList<>();
        
        Map<String, Double> scores = fromJson(assessment.getScoreVector(), SCORE_TYPE);
        
        // 找出得分最高的非平衡体质
        scores.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("BALANCED"))
            .max(Map.Entry.comparingByValue())
            .ifPresent(highest -> {
                if (highest.getValue() > 40.0) { // 如果得分超过40分，需要调理
                    priorities.add("优先调理" + getConstitutionName(highest.getKey()) + "体质");
                    priorities.add("建议通过饮食、运动、穴位按摩等方式综合调理");
                }
            });
        
        // 如果兼夹体质得分较高，也建议调理
        scores.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("BALANCED") && !entry.getKey().equals(assessment.getPrimaryType()))
            .filter(entry -> entry.getValue() > 30.0) // 兼夹体质得分超过30分
            .forEach(entry -> {
                priorities.add("兼夹" + getConstitutionName(entry.getKey()) + "体质也需关注");
            });
        
        if (priorities.isEmpty()) {
            priorities.add("体质相对平衡，以维持为主");
        }
        
        return priorities;
    }
    
    private String getConstitutionName(String code) {
        return switch (code) {
            case "QI_DEFICIENCY" -> "气虚";
            case "YANG_DEFICIENCY" -> "阳虚";
            case "YIN_DEFICIENCY" -> "阴虚";
            case "PHLEGM_DAMP" -> "痰湿";
            case "DAMP_HEAT" -> "湿热";
            case "BLOOD_STASIS" -> "血瘀";
            case "QI_STAGNATION" -> "气郁";
            case "SPECIAL" -> "特禀";
            case "PHLEGM_DAMPNESS" -> "痰湿";
            default -> "未知";
        };
    }
    
    /**
     * 生成体质变化趋势总结
     */
    private String generateTrendSummary(Map<String, String> trends, String currentPrimaryType) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前主导体质: ").append(currentPrimaryType).append("。");
        
        List<String> changes = new ArrayList<>();
        for (Map.Entry<String, String> entry : trends.entrySet()) {
            if (!"稳定".equals(entry.getValue())) {
                changes.add(entry.getKey() + "体质呈" + entry.getValue() + "趋势");
            }
        }
        
        if (!changes.isEmpty()) {
            sb.append("变化趋势: ").append(String.join("，", changes)).append("。");
        } else {
            sb.append("各体质相对稳定。");
        }
        
        return sb.toString();
    }
    
    /**
     * 生成家庭中医健康建议
     */
    private String generateFamilyRecommendation(Map<String, Integer> constitutionDistribution, 
                                               List<FamilyTcmHealthOverviewResponse.FamilyMemberHealthInfoDto> members) {
        StringBuilder sb = new StringBuilder();
        
        // 分析家庭体质特点
        if (constitutionDistribution.size() > 0) {
            // 找出家庭中最多的体质类型
            String dominantConstitution = constitutionDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
            
            sb.append("家庭体质特点: 多数成员为").append(getConstitutionName(dominantConstitution)).append("体质，");
            
            // 根据家庭体质特点提供建议
            switch (dominantConstitution) {
                case "QI_DEFICIENCY":
                    sb.append("建议家庭饮食以健脾益气为主，可共同食用黄芪、山药、大枣等食材，适当进行家庭运动如太极拳。");
                    break;
                case "YANG_DEFICIENCY":
                    sb.append("建议家庭注意保暖，冬季可共同进行温阳食疗，如羊肉汤、姜枣茶等。");
                    break;
                case "YIN_DEFICIENCY":
                    sb.append("建议家庭饮食清淡，多食滋阴润燥食物，共同养成早睡早起的良好作息。");
                    break;
                case "PHLEGM_DAMPNESS":
                    sb.append("建议家庭饮食清淡少油腻，共同进行适量运动，注意居住环境通风除湿。");
                    break;
                case "DAMP_HEAT":
                    sb.append("建议家庭饮食清淡，少食辛辣油腻，共同保持居住环境干爽。");
                    break;
                case "BLOOD_STASIS":
                    sb.append("建议家庭成员共同进行有氧运动，促进血液循环，注意情绪调节。");
                    break;
                case "QI_STAGNATION":
                    sb.append("建议家庭营造轻松氛围，多进行户外活动，共同关注情绪健康。");
                    break;
                default:
                    sb.append("家庭成员体质多样，建议根据各自体质特点进行个性化调理。");
            }
        } else {
            sb.append("家庭成员暂未完成体质测评，建议先进行体质测评以获得个性化建议。");
        }
        
        // 考虑家庭成员年龄结构
        long elderlyCount = members.stream()
            .filter(m -> m.relationship().contains("老人") || m.relationship().contains("父母"))
            .count();
        
        if (elderlyCount > 0) {
            sb.append("家庭中有老年成员，特别注意饮食调理和适度运动，避免过度劳累。");
        }
        
        return sb.toString();
    }
    
    /**
     * 规范化数值
     */
    private double norm(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private boolean readShareToFamily(Long userId) {
        return profileRepository.findById(userId).map(p -> {
            String prefs = p.getPreferences();
            if (prefs == null || prefs.isBlank()) return false;
            try {
                java.util.Map<?, ?> map = objectMapper.readValue(prefs, java.util.Map.class);
                Object val = map.get("shareToFamily");
                return val instanceof Boolean ? (Boolean) val : false;
            } catch (IOException ex) {
                return false;
            }
        }).orElse(false);
    }

    private String readAvatar(Long userId) {
        return profileRepository.findById(userId).map(profile -> {
            String prefs = profile.getPreferences();
            if (prefs == null || prefs.isBlank()) return null;
            try {
                java.util.Map<?, ?> map = objectMapper.readValue(prefs, java.util.Map.class);
                Object avatar = map.get("avatar");
                return avatar != null ? avatar.toString() : null;
            } catch (IOException ex) {
                return null;
            }
        }).orElse(null);
    }

    private Map<String, Double> calculateScores(List<AssessmentSubmitRequest.AnswerItem> answers) {
        Map<String, List<Integer>> accumulator = new HashMap<>();
        for (AssessmentSubmitRequest.AnswerItem answer : answers) {
            accumulator.computeIfAbsent(answer.dimension().toUpperCase(), key -> new ArrayList<>())
                    .add(answer.score());
        }
        Map<String, Double> scores = new LinkedHashMap<>();
        accumulator.forEach((dimension, values) -> {
            double avg = values.stream().mapToInt(Integer::intValue).average().orElse(0);
            scores.put(dimension, Math.round(avg * 10d) / 10d);
        });
        return scores;
    }

    private String determinePrimaryType(Map<String, Double> scores) {
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    if (entry.getValue() >= 60) {
                        return entry.getKey();
                    }
                    return "BALANCED";
                })
                .orElse("UNKNOWN");
    }

    private Map<String, Object> buildReport(String primaryType, Map<String, Double> scores) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("primaryType", primaryType);
        report.put("scores", scores);
        report.put("summary", generateSummary(primaryType));
        report.put("recommendations", generateRecommendations(primaryType));
        report.put("confidence", calculateConfidence(scores));
        return report;
    }

    private double calculateConfidence(Map<String, Double> scores) {
        if (scores == null || scores.isEmpty()) return 0d;
        java.util.List<Double> vals = new java.util.ArrayList<>(scores.values());
        vals.sort(java.util.Comparator.reverseOrder());
        double top = vals.get(0);
        double second = vals.size() > 1 ? vals.get(1) : 0d;
        double gap = Math.max(0d, top - second); // 分差
        double gapFactor = Math.min(1d, gap / 30d); // 分差归一化
        double countFactor = Math.min(1d, vals.size() / 9d); // 题量有效性近似
        double conf = Math.min(1d, 0.6 * gapFactor + 0.4 * countFactor);
        return Math.round(conf * 100d) / 100d;
    }

    private String generateSummary(String primaryType) {
        return switch (primaryType) {
            case "BALANCED" -> "体质平衡，保持良好生活方式。";
            case "QI_DEFICIENCY" -> "气虚体质，需注意增强体能与免疫力。";
            case "YANG_DEFICIENCY" -> "阳虚体质，注意保暖与适度运动。";
            case "YIN_DEFICIENCY" -> "阴虚体质，关注补水与滋阴饮食。";
            case "PHLEGM_DAMPNESS" -> "痰湿体质，建议加强运动与调节饮食。";
            case "DAMP_HEAT" -> "湿热体质，少食辛辣油腻，保持清淡饮食。";
            case "BLOOD_STASIS" -> "血瘀体质，适合规律运动促进血液循环。";
            case "QI_STAGNATION" -> "气郁体质，保持情绪疏导，适当放松。";
            case "SPECIAL_DIATHESIS" -> "特禀体质，注意过敏源管理与防护。";
            default -> "体质类型暂不明确，请结合专业医生建议。";
        };
    }

    private List<String> generateRecommendations(String primaryType) {
        return switch (primaryType) {
            case "BALANCED" -> List.of(
                "保持规律作息",
                "坚持适量运动",
                "维持乐观心态",
                "注重四季调养，顺应自然规律"
            );
            case "QI_DEFICIENCY" -> List.of(
                "适当进行太极、散步等轻度运动",
                "饮食以温补为主，如黄芪、党参、山药、大枣",
                "保证充足睡眠，避免过度劳累",
                "可尝试艾灸关元、气海、足三里等穴位"
            );
            case "YANG_DEFICIENCY" -> List.of(
                "注意保暖，避免寒湿环境",
                "进行温和运动提升阳气，如八段锦、太极拳",
                "饮用温热性茶饮如姜枣茶、肉桂茶",
                "可尝试艾灸命门、肾俞、关元等穴位"
            );
            case "YIN_DEFICIENCY" -> List.of(
                "多食清润食物，如百合、银耳、枸杞、黑芝麻",
                "避免熬夜，保证睡眠，养阴安神",
                "进行缓和运动如瑜伽、静坐",
                "可尝试按摩太溪、三阴交、涌泉等穴位"
            );
            case "PHLEGM_DAMPNESS" -> List.of(
                "控制高脂饮食，增加蔬果摄入",
                "坚持有氧运动，促进水湿运化",
                "保持充足睡眠与体重管理",
                "可尝试按摩丰隆、阴陵泉、足三里等穴位"
            );
            case "DAMP_HEAT" -> List.of(
                "少食辛辣油腻，清淡饮食",
                "保持环境通风与体温平衡",
                "适量饮用菊花茶、决明子茶等清热饮品",
                "可尝试按摩曲池、阴陵泉、内庭等穴位"
            );
            case "BLOOD_STASIS" -> List.of(
                "规律进行耐力运动，促进血液循环",
                "保持情绪平稳，缓解压力",
                "饮食加入活血食材如山楂、红花、玫瑰花",
                "可尝试按摩血海、膈俞、三阴交等穴位"
            );
            case "QI_STAGNATION" -> List.of(
                "保持愉悦心情，及时疏导压力",
                "参加社交活动，增强正面情绪",
                "选择舒缓运动如游泳、慢跑",
                "可尝试按摩太冲、内关、膻中等穴位"
            );
            case "SPECIAL_DIATHESIS" -> List.of(
                "远离过敏源，做好环境防护",
                "保持卫生，避免感染",
                "遵循医师指导服用药物",
                "可尝试温和调理，避免刺激性疗法"
            );
            default -> List.of(
                "建议结合生活方式、饮食和运动综合调理",
                "可咨询中医师获得更专业的体质调理建议"
            );
        };
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException ex) {
            throw new BusinessException(50001, "数据序列化失败", ex);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException ex) {
            throw new BusinessException(50002, "数据解析失败", ex);
        }
    }

    private AssessmentResponse toResponse(ConstitutionAssessment assessment,
                                          Map<String, Double> scores,
                                          Map<String, Object> report) {
        return new AssessmentResponse(
                assessment.getId(),
                assessment.getType(),
                scores,
                assessment.getPrimaryType(),
                report,
                assessment.getCreatedAt()
        );
    }

    private static List<Map<String, Object>> buildDefaultDimensions() {
        List<Map<String, Object>> dimensions = new ArrayList<>();
        dimensions.add(dimension("BALANCED", "平和质", "整体健康，阴阳平衡"));
        dimensions.add(dimension("QI_DEFICIENCY", "气虚质", "气力不足，免疫下降"));
        dimensions.add(dimension("YANG_DEFICIENCY", "阳虚质", "畏寒肢冷，精神不足"));
        dimensions.add(dimension("YIN_DEFICIENCY", "阴虚质", "口干咽燥，易烦热"));
        dimensions.add(dimension("PHLEGM_DAMPNESS", "痰湿质", "形体肥胖，痰多油腻"));
        dimensions.add(dimension("DAMP_HEAT", "湿热质", "面垢油光，口苦口干"));
        dimensions.add(dimension("BLOOD_STASIS", "血瘀质", "肤色晦暗，易疼痛"));
        dimensions.add(dimension("QI_STAGNATION", "气郁质", "情绪抑郁，易感压抑"));
        dimensions.add(dimension("SPECIAL_DIATHESIS", "特禀质", "过敏哮喘等特殊体质"));
        return dimensions;
    }

    private static Map<String, Object> dimension(String code, String name, String description) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", code);
        map.put("name", name);
        map.put("description", description);
        return map;
    }
    
    @Override
    public Map<String, Object> startAiAssessment(Long userId) {
        return aiService.startAiAssessment(userId);
    }

    @Override
    public Map<String, Object> processAiAnswer(Long userId, String sessionId, String userAnswer) {
        return aiService.processAnswer(userId, sessionId, userAnswer);
    }
    
    @Override
    public Map<String, Object> generateFinalAiAssessment(Long userId, String sessionId, String finalAnswers) {
        // 生成AI分析结果
        Map<String, Object> aiResult = aiService.generateFinalAssessment(userId, sessionId, finalAnswers);
        
        // 保存评估结果到数据库
        var savedAssessment = aiService.saveAiAssessmentResult(userId, aiResult);
        
        // 将保存的ID添加到结果中
        aiResult.put("assessmentId", savedAssessment.getId());
        
        return aiResult;
    }
}

