package com.healthfamily.web.dto;

import java.time.LocalDateTime;
/**
 * Constitution趋势Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record ConstitutionTrendResponse(
    Boolean hasData,
    Map<String, String> trends,
    Map<String, java.util.List<Double>> historyScores,
    java.util.List<String> dates,
    String summary,
    Map<String, Object> insights,
    LocalDateTime analyzedAt
) {}