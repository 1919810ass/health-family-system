package com.healthfamily.modules.recommendationv2.service;

import com.healthfamily.modules.recommendationv2.dto.RecommendationResponse;
import java.util.Map;

public interface AiClient {
  RecommendationResponse generate(Map<String,Object> input, int timeoutMs) throws Exception;
}
