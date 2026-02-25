package com.healthfamily.web.dto;

/**
 * 健康日志StatisticsResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record HealthLogStatisticsResponse(
        TrendRange last7Days,
        TrendRange last14Days,
        TrendRange last30Days
) {

    public record TrendRange(
            String label,
            Map<String, java.util.List<TrendValue>> typeSeries
    ) {
    }

    public record TrendValue(
            java.time.LocalDate date,
            Double averageScore,
            Long count
    ) {
    }
}

