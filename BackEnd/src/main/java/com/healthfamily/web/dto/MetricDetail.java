package com.healthfamily.web.dto;

public record MetricDetail(
    String label,
    String value,
    String unit,
    Boolean abnormal,
    String status // "Normal", "High", "Low" etc.
) {}
