package com.healthfamily.service.recommendation;

/**
 * AI推荐Result服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.List;

public record AiRecommendationResult(
        String title,
        String summary,
        List<Item> items,
        List<String> evidence,
        Safety safety,
        String raw
) {
    public record Item(
            String title,
            String content,
            String priority,
            List<String> sourceTags,
            Double confidence
    ) {
    }

    public record Safety(
            boolean refuse,
            String message
    ) {
    }
}


