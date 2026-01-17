package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.HealthLogType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

