package com.healthfamily.modules.recommendationv2.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyMember;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FamilyTcmHealthService {
    
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final ConstitutionAssessmentRepository constitutionRepo;
    private final ObjectMapper objectMapper;
    
    public FamilyTcmHealthService(FamilyRepository familyRepository,
                                 FamilyMemberRepository familyMemberRepository,
                                 UserRepository userRepository,
                                 ConstitutionAssessmentRepository constitutionRepo,
                                 ObjectMapper objectMapper) {
        this.familyRepository = familyRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.userRepository = userRepository;
        this.constitutionRepo = constitutionRepo;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 获取家庭中医健康概览
     */
    public Map<String, Object> getFamilyHealthOverview(Long familyId, Long userId) {
        // 验证用户是否为家庭成员
        Family family = familyRepository.findById(familyId)
            .orElseThrow(() -> new RuntimeException("家庭不存在"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 校验成员权限
        familyMemberRepository.findByFamilyAndUser(family, user)
            .orElseThrow(() -> new RuntimeException("无权访问该家庭"));
        
        List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        
        List<Map<String, Object>> memberInfos = new ArrayList<>();
        Map<String, Integer> constitutionDistribution = new HashMap<>();
        
        for (FamilyMember member : members) {
            User memberUser = member.getUser();
            
            // 获取成员体质信息
            List<ConstitutionAssessment> assessments = constitutionRepo.findByUserOrderByCreatedAtDesc(memberUser);
            
            Map<String, Object> info = new HashMap<>();
            info.put("userId", memberUser.getId());
            info.put("userName", memberUser.getNickname());
            info.put("relationship", member.getRelation());
            info.put("hasConstitutionData", false);
            info.put("scores", Collections.emptyMap());
            
            if (!assessments.isEmpty()) {
                ConstitutionAssessment latest = assessments.get(0);
                info.put("primaryConstitution", latest.getPrimaryType());
                info.put("hasConstitutionData", true);
                info.put("scores", parseScores(latest.getScoreVector()));
                info.put("lastAssessmentDate", latest.getCreatedAt());
                
                // 统计体质分布
                constitutionDistribution.merge(latest.getPrimaryType(), 1, Integer::sum);
            } else {
                info.put("primaryConstitution", null);
            }
            
            memberInfos.add(info);
        }
        
        // 生成家庭中医健康建议
        String familyRecommendation = generateFamilyRecommendation(constitutionDistribution, memberInfos);
        
        // 生成家庭健康报告
        Map<String, Object> overview = new HashMap<>();
        overview.put("familyId", familyId);
        overview.put("familyName", family.getName());
        overview.put("memberCount", memberInfos.size());
        overview.put("members", memberInfos);
        overview.put("constitutionDistribution", constitutionDistribution);
        overview.put("familyRecommendation", familyRecommendation);
        overview.put("generatedAt", LocalDate.now());
        overview.put("memberWithConstitution", (int) memberInfos.stream()
            .filter(m -> (Boolean) m.get("hasConstitutionData"))
            .count());
        
        return overview;
    }
    
    /**
     * 获取家庭成员体质趋势对比
     */
    public Map<String, Object> getFamilyConstitutionTrends(Long familyId, Long userId) {
        // 验证用户是否为家庭成员
        Family family = familyRepository.findById(familyId)
            .orElseThrow(() -> new RuntimeException("家庭不存在"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 校验成员权限
        familyMemberRepository.findByFamilyAndUser(family, user)
            .orElseThrow(() -> new RuntimeException("无权访问该家庭"));
        
        List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        
        Map<String, Object> trends = new HashMap<>();
        List<Map<String, Object>> memberTrends = new ArrayList<>();
        
        for (FamilyMember member : members) {
            User memberUser = member.getUser();
            
            // 获取成员最近的体质测评
            List<ConstitutionAssessment> assessments = constitutionRepo.findByUserOrderByCreatedAtDesc(memberUser)
                .stream()
                .limit(5) // 获取最近5次测评
                .collect(Collectors.toList());
            
            if (!assessments.isEmpty()) {
                Map<String, Object> memberTrend = new HashMap<>();
                memberTrend.put("userId", memberUser.getId());
                memberTrend.put("userName", memberUser.getNickname());
                memberTrend.put("relationship", member.getRelation());
                
                // 分析体质变化趋势
                List<Map<String, Object>> trendData = new ArrayList<>();
                for (ConstitutionAssessment assessment : assessments) {
                    Map<String, Object> trendPoint = new HashMap<>();
                    trendPoint.put("date", assessment.getCreatedAt());
                    trendPoint.put("primaryType", assessment.getPrimaryType());
                    trendPoint.put("scores", parseScores(assessment.getScoreVector()));
                    trendData.add(0, trendPoint); // 反向添加以保持时间顺序
                }
                
                memberTrend.put("trendData", trendData);
                memberTrend.put("currentConstitution", assessments.get(0).getPrimaryType());
                
                memberTrends.add(memberTrend);
            }
        }
        
        trends.put("familyId", familyId);
        trends.put("familyName", family.getName());
        trends.put("memberTrends", memberTrends);
        trends.put("trendAnalysis", analyzeFamilyTrends(memberTrends));
        
        return trends;
    }
    
    /**
     * 分析家庭体质趋势
     */
    private Map<String, Object> analyzeFamilyTrends(List<Map<String, Object>> memberTrends) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 统计家庭成员体质类型分布
        Map<String, Long> constitutionTypes = memberTrends.stream()
            .collect(Collectors.groupingBy(
                mt -> (String) mt.get("currentConstitution"),
                Collectors.counting()
            ));
        
        // 分析年龄结构与体质关系
        Map<String, List<String>> ageGroups = new HashMap<>();
        // 这里可以根据用户年龄进行分组分析
        
        analysis.put("constitutionDistribution", constitutionTypes);
        analysis.put("commonTrends", findCommonTrends(memberTrends));
        analysis.put("familyHealthStatus", evaluateFamilyHealthStatus(constitutionTypes));
        
        return analysis;
    }
    
    /**
     * 查找共同趋势
     */
    private List<String> findCommonTrends(List<Map<String, Object>> memberTrends) {
        List<String> commonTrends = new ArrayList<>();
        
        // 查找家庭成员中共同的体质类型
        Map<String, Long> constitutionCounts = memberTrends.stream()
            .map(mt -> (String) mt.get("currentConstitution"))
            .collect(Collectors.groupingBy(
                c -> c,
                Collectors.counting()
            ));
        
        // 如果多个成员有相同的体质类型，添加到共同趋势
        for (Map.Entry<String, Long> entry : constitutionCounts.entrySet()) {
            if (entry.getValue() > 1) {
                commonTrends.add(entry.getKey() + "体质在" + entry.getValue() + "名家庭成员中出现");
            }
        }
        
        return commonTrends;
    }
    
    /**
     * 评估家庭健康状态
     */
    private String evaluateFamilyHealthStatus(Map<String, Long> constitutionDistribution) {
        int totalMembers = constitutionDistribution.values().stream().mapToInt(Long::intValue).sum();
        if (totalMembers == 0) {
            return "暂无体质数据，无法评估家庭健康状态";
        }
        
        // 计算平和质比例
        long balancedCount = constitutionDistribution.getOrDefault("BALANCED", 0L);
        double balancedRatio = (double) balancedCount / totalMembers;
        
        if (balancedRatio >= 0.6) {
            return "家庭整体体质状况良好，多数成员体质平和";
        } else if (balancedRatio >= 0.3) {
            return "家庭体质状况中等，部分成员需要调理";
        } else {
            return "家庭体质状况需要关注，建议加强养生调理";
        }
    }
    
    /**
     * 生成家庭中医健康建议
     */
    private String generateFamilyRecommendation(Map<String, Integer> constitutionDistribution, 
                                               List<Map<String, Object>> members) {
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
            .filter(m -> ((String) m.get("relationship")).contains("老人") || 
                         ((String) m.get("relationship")).contains("父母"))
            .count();
        
        if (elderlyCount > 0) {
            sb.append("家庭中有老年成员，特别注意饮食调理和适度运动，避免过度劳累。");
        }
        
        return sb.toString();
    }
    
    private Map<String, Object> parseScores(String scoreVector) {
        try {
            return objectMapper.readValue(scoreVector, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
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
            case "BALANCED" -> "平和";
            default -> "未知";
        };
    }
}