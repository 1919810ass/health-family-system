package com.healthfamily.web.dto;

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

