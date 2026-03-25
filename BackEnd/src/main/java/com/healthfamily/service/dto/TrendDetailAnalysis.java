package com.healthfamily.service.dto;

/**
 * AI趋势详情分析结果 DTO
 * @param trend 趋势 ("上升", "下降", "稳定")
 * @param analysis AI生成的分析文本
 */
public record TrendDetailAnalysis(String trend, String analysis) {
}
