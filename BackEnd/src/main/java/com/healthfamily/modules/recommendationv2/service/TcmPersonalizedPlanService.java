package com.healthfamily.modules.recommendationv2.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.Profile;
import com.healthfamily.domain.entity.TcmKnowledgeBase;
import com.healthfamily.domain.entity.TcmPersonalizedPlan;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.domain.repository.TcmKnowledgeBaseRepository;
import com.healthfamily.domain.repository.TcmPersonalizedPlanRepository;
import com.healthfamily.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TcmPersonalizedPlanService {
    
    private final ConstitutionAssessmentRepository constitutionRepo;
    private final ProfileRepository profileRepo;
    private final TcmKnowledgeBaseRepository knowledgeRepo;
    private final TcmPersonalizedPlanRepository planRepo;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper;
    
    public TcmPersonalizedPlanService(ConstitutionAssessmentRepository constitutionRepo,
                                      ProfileRepository profileRepo,
                                      TcmKnowledgeBaseRepository knowledgeRepo,
                                      TcmPersonalizedPlanRepository planRepo,
                                      UserRepository userRepo,
                                      ObjectMapper objectMapper) {
        this.constitutionRepo = constitutionRepo;
        this.profileRepo = profileRepo;
        this.knowledgeRepo = knowledgeRepo;
        this.planRepo = planRepo;
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 生成个性化中医养生方案
     */
    public TcmPersonalizedPlan generatePlan(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取用户最新体质测评
        List<ConstitutionAssessment> assessments = constitutionRepo.findByUserOrderByCreatedAtDesc(user);
        if (assessments.isEmpty()) {
            throw new RuntimeException("用户暂无体质测评记录");
        }
        
        ConstitutionAssessment latestAssessment = assessments.get(0);
        String primaryConstitution = latestAssessment.getPrimaryType();
        
        // 获取对应的中医养生知识
        List<TcmKnowledgeBase> knowledgeList = knowledgeRepo.findByConstitutionType(primaryConstitution);
        
        // 按类型分类养生建议
        Map<String, List<Map<String, Object>>> planContent = knowledgeList.stream()
            .collect(Collectors.groupingBy(
                kb -> kb.getType().name(),
                Collectors.mapping(kb -> Map.of(
                    "id", kb.getId(),
                    "title", kb.getTitle(),
                    "content", kb.getContent(),
                    "difficulty", kb.getDifficulty().name(),
                    "tags", parseJsonField(kb.getTags(), new TypeReference<List<String>>() {}),
                    "contraindications", parseJsonField(kb.getContraindications(), new TypeReference<List<String>>() {}),
                    "evidenceLevel", kb.getEvidenceLevel().name()
                ), Collectors.toList())
            ));
        
        // 生成季节性建议
        Map<String, String> seasonalRecommendations = getSeasonalRecommendations(primaryConstitution);
        
        // 生成优先级建议
        List<String> priorityRecommendations = getPriorityRecommendations(latestAssessment);
        
        // 创建个性化方案
        TcmPersonalizedPlan plan = TcmPersonalizedPlan.builder()
            .userId(userId)
            .planName(primaryConstitution + "体质个性化养生方案")
            .primaryConstitution(primaryConstitution)
            .planContent(toJson(planContent))
            .seasonalRecommendations(toJson(seasonalRecommendations))
            .priorityRecommendations(toJson(priorityRecommendations))
            .generatedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(30)) // 30天后过期
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        return planRepo.save(plan);
    }
    
    /**
     * 获取用户最新个性化方案
     */
    public TcmPersonalizedPlan getLatestPlan(Long userId) {
        return planRepo.findTopByUserIdOrderByGeneratedAtDesc(userId);
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
        
        try {
            Map<String, Object> scores = objectMapper.readValue(assessment.getScoreVector(), 
                new TypeReference<Map<String, Object>>() {});
            
            // 找出得分最高的非平衡体质
            Optional<Map.Entry<String, Object>> highest = scores.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("BALANCED"))
                .max(Comparator.comparing(entry -> ((Number) entry.getValue()).doubleValue()));
            
            if (highest.isPresent()) {
                double score = ((Number) highest.get().getValue()).doubleValue();
                if (score > 40.0) { // 如果得分超过40分，需要调理
                    priorities.add("优先调理" + getConstitutionName(highest.get().getKey()) + "体质");
                    priorities.add("建议通过饮食、运动、穴位按摩等方式综合调理");
                }
            }
            
            // 如果兼夹体质得分较高，也建议调理
            for (Map.Entry<String, Object> entry : scores.entrySet()) {
                if (!entry.getKey().equals("BALANCED") && 
                    !entry.getKey().equals(assessment.getPrimaryType())) {
                    double score = ((Number) entry.getValue()).doubleValue();
                    if (score > 30.0) { // 兼夹体质得分超过30分
                        priorities.add("兼夹" + getConstitutionName(entry.getKey()) + "体质也需关注");
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
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
    
    private <T> T parseJsonField(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}