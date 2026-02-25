/**
 * MetricDetail
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
package com.healthfamily.web.dto;

public record MetricDetail(
    String label,
    String value,
    String unit,
    Boolean abnormal,
    String status // "Normal", "High", "Low" etc.
) {}
