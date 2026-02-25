package com.healthfamily.service.recommendation;

/**
 * AI推荐Request服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import com.healthfamily.domain.constant.RecommendationCategory;

public record AiRecommendationRequest(
        RecommendationCategory category,
        RecommendationContext context,
        int maxItems,
        boolean strictMode,
        String modelOverride
) {
}


