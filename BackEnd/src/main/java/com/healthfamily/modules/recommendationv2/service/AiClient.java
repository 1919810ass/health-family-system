package com.healthfamily.modules.recommendationv2.service;

import com.healthfamily.modules.recommendationv2.dto.RecommendationResponse;
/**
 * AIClient服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.Map;

public interface AiClient {
  RecommendationResponse generate(Map<String,Object> input, int timeoutMs) throws Exception;
}
