package com.healthfamily.service;

import java.util.Map;

/**
 * AI工具调用服务（Function Calling）
 */
public interface HealthAiToolService {

    /**
     * 查询药品信息
     */
    Map<String, Object> queryDrugInfo(String drugName);

    /**
     * 获取附近医院
     */
    Map<String, Object> getNearbyHospitals(String location, String department);

    /**
     * 查询健康知识
     */
    Map<String, Object> queryHealthKnowledge(String keyword, String category);
}

