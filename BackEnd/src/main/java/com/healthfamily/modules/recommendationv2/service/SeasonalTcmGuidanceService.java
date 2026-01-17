package com.healthfamily.modules.recommendationv2.service;

import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.TcmKnowledgeBase;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.TcmKnowledgeBaseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeasonalTcmGuidanceService {
    
    private final TcmKnowledgeBaseRepository knowledgeRepo;
    private final ConstitutionAssessmentRepository assessmentRepo;
    
    public SeasonalTcmGuidanceService(TcmKnowledgeBaseRepository knowledgeRepo,
                                      ConstitutionAssessmentRepository assessmentRepo) {
        this.knowledgeRepo = knowledgeRepo;
        this.assessmentRepo = assessmentRepo;
    }
    
    /**
     * 获取用户的季节性养生指导
     */
    public Map<String, Object> getSeasonalGuidance(Long userId) {
        // 获取用户体质信息
        var userAssessments = assessmentRepo.findByUserOrderByCreatedAtDesc(
            com.healthfamily.domain.entity.User.builder().id(userId).build());
        
        String constitutionType = "BALANCED"; // 默认平和质
        if (!userAssessments.isEmpty()) {
            constitutionType = userAssessments.get(0).getPrimaryType();
        }
        
        // 获取当前季节
        LocalDate now = LocalDate.now();
        String currentSeason = getCurrentSeason(now.getMonthValue(), now.getDayOfMonth());
        int solarTerm = getSolarTerm(now.getMonthValue(), now.getDayOfMonth());
        
        // 获取季节性养生知识
        List<TcmKnowledgeBase> seasonalKnowledge = knowledgeRepo.findByTypeAndConstitutionType(
            TcmKnowledgeBase.KnowledgeType.SEASONAL, constitutionType);
        
        // 根据当前季节获取特定指导
        Map<String, Object> guidance = new HashMap<>();
        guidance.put("currentSeason", currentSeason);
        guidance.put("solarTerm", getSolarTermName(solarTerm));
        guidance.put("constitutionType", constitutionType);
        guidance.put("seasonalRecommendations", getSeasonalRecommendations(constitutionType, currentSeason));
        guidance.put("solarTermGuidance", getSolarTermGuidance(constitutionType, solarTerm));
        guidance.put("dietaryGuidance", getDietaryGuidanceBySeason(constitutionType, currentSeason));
        guidance.put("exerciseGuidance", getExerciseGuidanceBySeason(constitutionType, currentSeason));
        guidance.put("lifestyleTips", getLifestyleTipsBySeason(constitutionType, currentSeason));
        
        return guidance;
    }
    
    /**
     * 获取当前季节
     */
    private String getCurrentSeason(int month, int day) {
        if (month >= 3 && month <= 5) {
            return "春季";
        } else if (month >= 6 && month <= 8) {
            return "夏季";
        } else if (month >= 9 && month <= 11) {
            return "秋季";
        } else {
            return "冬季";
        }
    }
    
    /**
     * 获取节气编号
     */
    private int getSolarTerm(int month, int day) {
        // 简化的节气计算，实际应用中可以使用更精确的算法
        if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) return 1; // 雨水
        if ((month == 3 && day >= 21) || (month == 4 && day <= 20)) return 2; // 春分
        if ((month == 4 && day >= 21) || (month == 5 && day <= 20)) return 3; // 谷雨
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) return 4; // 夏至
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) return 5; // 小暑
        if ((month == 7 && day >= 23) || (month == 8 && day <= 23)) return 6; // 大暑
        if ((month == 8 && day >= 24) || (month == 9 && day <= 23)) return 7; // 处暑
        if ((month == 9 && day >= 24) || (month == 10 && day <= 23)) return 8; // 秋分
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) return 9; // 霜降
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) return 10; // 冬至
        if ((month == 12 && day >= 22) || (month == 1 && day <= 20)) return 11; // 小寒
        if ((month == 1 && day >= 21) || (month == 2 && day <= 18)) return 12; // 大寒
        
        return 0; // 未知节气
    }
    
    /**
     * 获取节气名称
     */
    private String getSolarTermName(int solarTerm) {
        String[] terms = {"", "雨水", "春分", "谷雨", "夏至", "小暑", "大暑", 
                         "处暑", "秋分", "霜降", "冬至", "小寒", "大寒"};
        return solarTerm >= 0 && solarTerm < terms.length ? terms[solarTerm] : "未知节气";
    }
    
    /**
     * 获取季节性养生建议
     */
    private Map<String, String> getSeasonalRecommendations(String constitution, String season) {
        Map<String, String> recommendations = new HashMap<>();
        
        switch (constitution) {
            case "QI_DEFICIENCY":
                switch (season) {
                    case "春季":
                        recommendations.put("饮食", "春季宜食用温补之品，如大枣、山药、扁豆等，以助脾气生发");
                        recommendations.put("运动", "选择温和的运动方式，如太极拳、八段锦，避免过度出汗");
                        recommendations.put("起居", "早睡早起，注意保暖，避免受风");
                        break;
                    case "夏季":
                        recommendations.put("饮食", "夏季宜清淡，多食清热利湿之品，如绿豆、冬瓜、薏米等");
                        recommendations.put("运动", "选择清晨或傍晚运动，避免烈日下运动");
                        recommendations.put("起居", "避免贪凉，适当午休");
                        break;
                    case "秋季":
                        recommendations.put("饮食", "秋季宜滋阴润燥，多食梨、银耳、百合等");
                        recommendations.put("运动", "适当增加运动量，增强体质");
                        recommendations.put("起居", "早睡早起，注意保湿");
                        break;
                    case "冬季":
                        recommendations.put("饮食", "冬季宜温补，多食羊肉、牛肉、桂圆等温热食物");
                        recommendations.put("运动", "运动量适中，注意保暖");
                        recommendations.put("起居", "早睡晚起，避寒就温");
                        break;
                }
                break;
                
            case "YANG_DEFICIENCY":
                switch (season) {
                    case "春季":
                        recommendations.put("饮食", "春季宜温阳益气，多食韭菜、大蒜、生姜等");
                        recommendations.put("运动", "适当增加户外活动，多晒太阳");
                        recommendations.put("起居", "注意保暖，尤其是腰腹部");
                        break;
                    case "夏季":
                        recommendations.put("饮食", "避免过食寒凉，适当食用温性食物");
                        recommendations.put("运动", "选择温和运动，避免大量出汗");
                        recommendations.put("起居", "避免空调直吹，适当保暖");
                        break;
                    case "秋季":
                        recommendations.put("饮食", "多食温补食物，如羊肉、狗肉、核桃等");
                        recommendations.put("运动", "适度运动，增强阳气");
                        recommendations.put("起居", "注意保暖，避免受凉");
                        break;
                    case "冬季":
                        recommendations.put("饮食", "重在温阳补肾，多食羊肉、狗肉、鹿茸等");
                        recommendations.put("运动", "选择室内运动，避免受寒");
                        recommendations.put("起居", "充分保暖，早睡晚起");
                        break;
                }
                break;
                
            case "YIN_DEFICIENCY":
                switch (season) {
                    case "春季":
                        recommendations.put("饮食", "春季宜滋阴养血，多食桑葚、枸杞、黑芝麻等");
                        recommendations.put("运动", "选择舒缓运动，避免剧烈运动");
                        recommendations.put("起居", "晚睡早起，保持心情舒畅");
                        break;
                    case "夏季":
                        recommendations.put("饮食", "多食清热滋阴之品，如西瓜、黄瓜、苦瓜等");
                        recommendations.put("运动", "选择清晨或傍晚运动，避免暴晒");
                        recommendations.put("起居", "适当午休，保持室内凉爽");
                        break;
                    case "秋季":
                        recommendations.put("饮食", "重在滋阴润燥，多食梨、银耳、蜂蜜等");
                        recommendations.put("运动", "适度运动，避免大量出汗");
                        recommendations.put("起居", "早睡早起，注意保湿");
                        break;
                    case "冬季":
                        recommendations.put("饮食", "多食滋阴补肾之品，如黑芝麻、黑豆、桑葚等");
                        recommendations.put("运动", "选择温和运动，避免过度");
                        recommendations.put("起居", "早睡晚起，避寒就温");
                        break;
                }
                break;
                
            default:
                switch (season) {
                    case "春季":
                        recommendations.put("饮食", "春季宜辛甘发散，多食葱、姜、蒜、香菜等");
                        recommendations.put("运动", "多进行户外活动，舒展筋骨");
                        recommendations.put("起居", "晚睡早起，多接触阳光");
                        break;
                    case "夏季":
                        recommendations.put("饮食", "清淡为主，多食瓜果蔬菜，少食油腻");
                        recommendations.put("运动", "选择清晨或傍晚运动，避免中暑");
                        recommendations.put("起居", "晚睡早起，适当午休");
                        break;
                    case "秋季":
                        recommendations.put("饮食", "滋阴润燥，多食秋梨、银耳、蜂蜜等");
                        recommendations.put("运动", "适度运动，收敛阳气");
                        recommendations.put("起居", "早睡早起，收敛神气");
                        break;
                    case "冬季":
                        recommendations.put("饮食", "适当进补，多食温热食物");
                        recommendations.put("运动", "适度运动，保护阳气");
                        recommendations.put("起居", "早睡晚起，避寒就温");
                        break;
                }
        }
        
        return recommendations;
    }
    
    /**
     * 获取节气养生指导
     */
    private Map<String, String> getSolarTermGuidance(String constitution, int solarTerm) {
        Map<String, String> guidance = new HashMap<>();
        
        switch (solarTerm) {
            case 4: // 夏至
                guidance.put("养生要点", "夏至阳气最盛，应晚睡早起，适当午休");
                guidance.put("饮食建议", "多食清热解暑之品，如绿豆、西瓜、苦瓜等");
                break;
            case 8: // 秋分
                guidance.put("养生要点", "秋分阴阳平衡，应早睡早起，收敛神气");
                guidance.put("饮食建议", "滋阴润燥，多食秋梨、银耳、蜂蜜等");
                break;
            case 10: // 冬至
                guidance.put("养生要点", "冬至阴气最盛，应早睡晚起，保护阳气");
                guidance.put("饮食建议", "适当进补，多食温热食物");
                break;
            default:
                guidance.put("养生要点", "顺应自然规律，调和阴阳");
                guidance.put("饮食建议", "根据体质选择合适食物");
        }
        
        return guidance;
    }
    
    /**
     * 获取季节性饮食指导
     */
    private Map<String, Object> getDietaryGuidanceBySeason(String constitution, String season) {
        Map<String, Object> dietary = new HashMap<>();
        
        List<String> foods = new ArrayList<>();
        List<String> recipes = new ArrayList<>();
        
        switch (season) {
            case "春季":
                foods.addAll(List.of("韭菜", "香椿", "春笋", "菠菜", "荠菜"));
                recipes.addAll(List.of("韭菜炒鸡蛋", "香椿拌豆腐", "春笋炒肉丝"));
                break;
            case "夏季":
                foods.addAll(List.of("西瓜", "黄瓜", "苦瓜", "冬瓜", "绿豆"));
                recipes.addAll(List.of("绿豆汤", "冬瓜汤", "苦瓜炒蛋"));
                break;
            case "秋季":
                foods.addAll(List.of("梨", "银耳", "百合", "蜂蜜", "山药"));
                recipes.addAll(List.of("银耳莲子汤", "百合粥", "山药炖排骨"));
                break;
            case "冬季":
                foods.addAll(List.of("羊肉", "牛肉", "桂圆", "核桃", "黑芝麻"));
                recipes.addAll(List.of("羊肉汤", "当归生姜羊肉汤", "核桃粥"));
                break;
        }
        
        dietary.put("recommendedFoods", foods);
        dietary.put("recommendedRecipes", recipes);
        dietary.put("dietaryPrinciples", getDietaryPrincipleByConstitution(constitution));
        
        return dietary;
    }
    
    /**
     * 获取体质饮食原则
     */
    private String getDietaryPrincipleByConstitution(String constitution) {
        return switch (constitution) {
            case "QI_DEFICIENCY" -> "补气健脾，温补为宜";
            case "YANG_DEFICIENCY" -> "温阳补肾，多食温热";
            case "YIN_DEFICIENCY" -> "滋阴润燥，清淡为主";
            case "PHLEGM_DAMPNESS" -> "健脾祛湿，清淡饮食";
            case "DAMP_HEAT" -> "清热利湿，少食辛辣";
            case "BLOOD_STASIS" -> "活血化瘀，多食山楂";
            case "QI_STAGNATION" -> "理气解郁，多食柑橘";
            case "SPECIAL_DIATHESIS" -> "清淡饮食，避免过敏源";
            default -> "平衡膳食，顺应季节";
        };
    }
    
    /**
     * 获取季节性运动指导
     */
    private Map<String, Object> getExerciseGuidanceBySeason(String constitution, String season) {
        Map<String, Object> exercise = new HashMap<>();
        
        List<String> exercises = new ArrayList<>();
        String intensity = "";
        String duration = "";
        
        switch (season) {
            case "春季":
                exercises.addAll(List.of("太极拳", "八段锦", "散步", "踏青"));
                intensity = "中等";
                duration = "30-60分钟";
                break;
            case "夏季":
                exercises.addAll(List.of("游泳", "瑜伽", "太极", "清晨慢跑"));
                intensity = "中低";
                duration = "20-40分钟";
                break;
            case "秋季":
                exercises.addAll(List.of("登山", "慢跑", "太极", "广场舞"));
                intensity = "中等";
                duration = "30-50分钟";
                break;
            case "冬季":
                exercises.addAll(List.of("太极拳", "八段锦", "室内健身", "快走"));
                intensity = "中低";
                duration = "20-40分钟";
                break;
        }
        
        exercise.put("recommendedExercises", exercises);
        exercise.put("intensity", intensity);
        exercise.put("duration", duration);
        exercise.put("exercisePrinciples", getExercisePrincipleByConstitution(constitution));
        
        return exercise;
    }
    
    /**
     * 获取体质运动原则
     */
    private String getExercisePrincipleByConstitution(String constitution) {
        return switch (constitution) {
            case "QI_DEFICIENCY" -> "温和运动，避免过度";
            case "YANG_DEFICIENCY" -> "适度运动，温阳助气";
            case "YIN_DEFICIENCY" -> "缓和运动，避免出汗过多";
            case "PHLEGM_DAMPNESS" -> "有氧运动，促进运化";
            case "DAMP_HEAT" -> "中等运动，清热利湿";
            case "BLOOD_STASIS" -> "有氧运动，促进循环";
            case "QI_STAGNATION" -> "舒缓运动，调节情绪";
            case "SPECIAL_DIATHESIS" -> "温和运动，避免过敏";
            default -> "适度运动，平衡阴阳";
        };
    }
    
    /**
     * 获取季节性生活方式建议
     */
    private Map<String, Object> getLifestyleTipsBySeason(String constitution, String season) {
        Map<String, Object> lifestyle = new HashMap<>();
        
        String sleepPattern = "";
        String emotionalGuidance = "";
        String dailyRoutines = "";
        
        switch (season) {
            case "春季":
                sleepPattern = "晚睡早起";
                emotionalGuidance = "保持心情舒畅，避免抑郁";
                dailyRoutines = "多进行户外活动，舒展筋骨";
                break;
            case "夏季":
                sleepPattern = "晚睡早起，适当午休";
                emotionalGuidance = "保持心境平和，避免烦躁";
                dailyRoutines = "避免烈日暴晒，保持室内通风";
                break;
            case "秋季":
                sleepPattern = "早睡早起";
                emotionalGuidance = "保持内心宁静，收敛神气";
                dailyRoutines = "适度运动，滋阴润燥";
                break;
            case "冬季":
                sleepPattern = "早睡晚起";
                emotionalGuidance = "保持精神内守，避寒就温";
                dailyRoutines = "减少户外活动，注意保暖";
                break;
        }
        
        lifestyle.put("sleepPattern", sleepPattern);
        lifestyle.put("emotionalGuidance", emotionalGuidance);
        lifestyle.put("dailyRoutines", dailyRoutines);
        lifestyle.put("lifestylePrinciples", getLifestylePrincipleByConstitution(constitution));
        
        return lifestyle;
    }
    
    /**
     * 获取体质生活方式原则
     */
    private String getLifestylePrincipleByConstitution(String constitution) {
        return switch (constitution) {
            case "QI_DEFICIENCY" -> "规律作息，避免过度劳累";
            case "YANG_DEFICIENCY" -> "注意保暖，避免寒湿";
            case "YIN_DEFICIENCY" -> "避免熬夜，养阴安神";
            case "PHLEGM_DAMPNESS" -> "保持干燥，适度运动";
            case "DAMP_HEAT" -> "保持清爽，清热利湿";
            case "BLOOD_STASIS" -> "保持心情舒畅，适度运动";
            case "QI_STAGNATION" -> "调节情绪，多与人交流";
            case "SPECIAL_DIATHESIS" -> "避免过敏源，保持清洁";
            default -> "规律作息，平衡身心";
        };
    }
}