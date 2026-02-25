/**
 * 推荐AIException服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
package com.healthfamily.service.recommendation;

public class RecommendationAiException extends RuntimeException {

    public RecommendationAiException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecommendationAiException(String message) {
        super(message);
    }
}


