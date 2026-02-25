/**
 * 健康Inference服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
package com.healthfamily.service;

public interface HealthInferenceService {
    String generateCrossDomainInference(Long userId);
}
