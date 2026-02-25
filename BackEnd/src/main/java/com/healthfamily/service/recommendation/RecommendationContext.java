package com.healthfamily.service.recommendation;

import com.healthfamily.domain.entity.HealthLog;

/**
 * 推荐Context服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.List;

public record RecommendationContext(
        String profile,
        String assessment,
        String logsSummary,
        String preferences,
        String logsStructured,
        List<HealthLog> recentLogs
) {
    /**
     * 执行业务操作
     * @return 业务返回结果
     */
    public static RecommendationContext empty() {
        return new RecommendationContext("无", "无", "无", "无", "[]", List.of());
    }
}

