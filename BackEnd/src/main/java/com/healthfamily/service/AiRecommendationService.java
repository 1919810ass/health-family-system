package com.healthfamily.service;

import com.healthfamily.web.dto.AiRecommendationRequest;
import com.healthfamily.web.dto.AiRecommendationResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * AI个性化建议服务
 */
public interface AiRecommendationService {

    /**
     * 生成个性化建议
     */
    AiRecommendationResponse generateRecommendation(Long userId, AiRecommendationRequest request);

    /**
     * 获取用户的建议列表
     */
    List<AiRecommendationResponse> getUserRecommendations(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 反馈建议质量
     */
    void feedback(Long recommendationId, Long userId, Integer feedback);
}

