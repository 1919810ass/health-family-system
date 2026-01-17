package com.healthfamily.modules.recommendationv2.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.ConstitutionTrendRecord;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.ConstitutionTrendRecordRepository;
import com.healthfamily.modules.recommendationv2.service.model.ConstitutionFeatures;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TcmTrendAnalysisService {
    
    private final ConstitutionAssessmentRepository assessmentRepository;
    private final ConstitutionTrendRecordRepository trendRecordRepository;
    private final ObjectMapper objectMapper;
    
    public TcmTrendAnalysisService(ConstitutionAssessmentRepository assessmentRepository,
                                   ConstitutionTrendRecordRepository trendRecordRepository,
                                   ObjectMapper objectMapper) {
        this.assessmentRepository = assessmentRepository;
        this.trendRecordRepository = trendRecordRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 分析用户体质变化趋势
     */
    public Map<String, Object> analyzeTrend(Long userId, int lookbackDays) {
        List<ConstitutionAssessment> assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(
            com.healthfamily.domain.entity.User.builder().id(userId).build()
        ).stream()
        .filter(a -> a.getCreatedAt().isAfter(LocalDateTime.now().minusDays(lookbackDays)))
        .sorted(Comparator.comparing(ConstitutionAssessment::getCreatedAt))
        .collect(Collectors.toList());
        
        if (assessments.size() < 2) {
            return Map.of(
                "hasData", false,
                "message", "暂无足够的历史数据进行趋势分析"
            );
        }
        
        // 分析各体质得分变化
        Map<String, List<Double>> scoresOverTime = new HashMap<>();
        for (ConstitutionAssessment assessment : assessments) {
            Map<String, Object> vec = parseScores(assessment.getScoreVector());
            for (Map.Entry<String, Object> entry : vec.entrySet()) {
                scoresOverTime.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                    .add(norm(entry.getValue()));
            }
        }
        
        // 计算趋势
        Map<String, String> trends = new HashMap<>();
        Map<String, Object> currentScoreObjects = parseScores(assessments.get(assessments.size() - 1).getScoreVector());
        Map<String, Object> previousScoreObjects = parseScores(assessments.get(0).getScoreVector());
        
        Map<String, Double> currentScores = convertToDoubleMap(currentScoreObjects);
        Map<String, Double> previousScores = convertToDoubleMap(previousScoreObjects);
        
        for (Map.Entry<String, Double> entry : currentScores.entrySet()) {
            String constitution = entry.getKey();
            Double currentScore = entry.getValue();
            Double previousScore = previousScores.get(constitution);
            
            if (previousScore != null) {
                double diff = currentScore - previousScore;
                if (diff > 5) {
                    trends.put(constitution, "上升");
                } else if (diff < -5) {
                    trends.put(constitution, "下降");
                } else {
                    trends.put(constitution, "稳定");
                }
            }
        }
        
        // 生成趋势分析报告
        String summary = generateTrendSummary(trends, assessments.get(assessments.size() - 1).getPrimaryType());
        
        // 保存趋势记录
        saveTrendRecord(userId, assessments.get(assessments.size() - 1).getId(), currentScores, trends);
        
        return Map.of(
            "hasData", true,
            "trends", trends,
            "summary", summary,
            "currentPrimaryType", assessments.get(assessments.size() - 1).getPrimaryType(),
            "assessmentCount", assessments.size(),
            "dateRange", Map.of(
                "from", assessments.get(0).getCreatedAt(),
                "to", assessments.get(assessments.size() - 1).getCreatedAt()
            )
        );
    }
    
    /**
     * 保存趋势记录
     */
    private void saveTrendRecord(Long userId, Long assessmentId, Map<String, Double> currentScores, Map<String, String> trends) {
        try {
            ConstitutionTrendRecord record = ConstitutionTrendRecord.builder()
                .userId(userId)
                .assessmentId(assessmentId)
                .constitutionScores(objectMapper.writeValueAsString(currentScores))
                .primaryType("")
                .trendAnalysis(objectMapper.writeValueAsString(trends))
                .createdAt(LocalDateTime.now())
                .build();
            
            trendRecordRepository.save(record);
        } catch (Exception e) {
            // 记录错误但不中断主要流程
            e.printStackTrace();
        }
    }
    
    private Map<String, Object> parseScores(String scoreVector) {
        try {
            return objectMapper.readValue(scoreVector, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
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
    
    private Map<String, Double> convertToDoubleMap(Map<String, Object> source) {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            result.put(entry.getKey(), norm(entry.getValue()));
        }
        return result;
    }
    
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
}