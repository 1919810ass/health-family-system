package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.HealthLogType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 健康日志Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record HealthLogResponse(
        Long id,
        LocalDate logDate,
        HealthLogType type,
        Map<String, Object> content,
        BigDecimal score,
        LocalDateTime createdAt
) {
}

