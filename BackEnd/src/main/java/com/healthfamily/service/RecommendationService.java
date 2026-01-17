package com.healthfamily.service;

import com.healthfamily.domain.constant.RecommendationCategory;
import com.healthfamily.web.dto.RecommendationFeedbackRequest;
import com.healthfamily.web.dto.RecommendationGenerateRequest;
import com.healthfamily.web.dto.RecommendationGenerateResponse;
import com.healthfamily.web.dto.RecommendationResponse;

import java.time.LocalDate;
import java.util.List;

public interface RecommendationService {

    RecommendationGenerateResponse generate(Long userId, RecommendationGenerateRequest request);

    List<RecommendationResponse> list(Long userId, LocalDate date, RecommendationCategory category);

    RecommendationResponse feedback(Long userId, Long recommendationId, RecommendationFeedbackRequest request);
}

