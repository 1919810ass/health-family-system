package com.healthfamily.service;

import java.util.Map;

/**
 * 健康数据AI处理服务
 * 包括：数据清洗、异常检测、OCR解析、语音识别
 */
public interface HealthDataAiService {

    /**
     * 数据清洗与标准化
     * @param rawData 原始数据（可能是文本或Map）
     * @param dataType 数据类型（如：血压、血糖、体温等）
     * @return 清洗后的标准化数据
     */
    Map<String, Object> cleanAndNormalize(Object rawData, String dataType);

    /**
     * 异常数据检测
     * @param userId 用户ID
     * @param dataType 数据类型
     * @param value 当前值
     * @param historicalData 历史数据（用于对比）
     * @return 是否异常及异常原因
     */
    AnomalyResult detectAnomaly(Long userId, String dataType, Double value, Map<String, Object> historicalData);

    /**
     * OCR解析医疗数据
     * @param imageBase64 图片Base64编码
     * @return 解析后的结构化数据
     */
    Map<String, Object> parseMedicalDataFromImage(String imageBase64);

    /**
     * 语音录入解析
     * @param voiceText 语音转文字后的文本
     * @return 解析后的结构化数据
     */
    Map<String, Object> parseVoiceInput(String voiceText);

    /**
     * 饮食文本优化为结构化格式
     * @param text 手动录入的饮食文本
     * @return 包含 items 与 totalCalories 的结构化数据
     */
    Map<String, Object> optimizeDietText(String text);

    /**
     * 通用优化输入为结构化格式
     * @param type 类型：DIET/SLEEP/SPORT/MOOD/VITALS
     * @param text 文本内容
     * @return 结构化数据
     */
    Map<String, Object> optimizeInput(String type, String text);

    /**
     * 异常检测结果
     */
    record AnomalyResult(
            boolean isAnomaly,
            String reason,
            String severity,  // LOW, MEDIUM, HIGH
            String suggestion
    ) {}
}

