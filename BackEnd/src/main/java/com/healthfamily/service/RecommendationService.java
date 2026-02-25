package com.healthfamily.service;

import com.healthfamily.domain.constant.RecommendationCategory;
import com.healthfamily.web.dto.RecommendationFeedbackRequest;
import com.healthfamily.web.dto.RecommendationGenerateRequest;
import com.healthfamily.web.dto.RecommendationGenerateResponse;
import com.healthfamily.web.dto.RecommendationResponse;

import java.time.LocalDate;
/**
 * 推荐服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.List;

public interface RecommendationService {

    RecommendationGenerateResponse generate(Long userId, RecommendationGenerateRequest request);

    List<RecommendationResponse> list(Long userId, LocalDate date, RecommendationCategory category);

    RecommendationResponse feedback(Long userId, Long recommendationId, RecommendationFeedbackRequest request);
}

