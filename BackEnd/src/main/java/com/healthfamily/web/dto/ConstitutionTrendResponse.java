package com.healthfamily.web.dto;

import java.time.LocalDateTime;
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