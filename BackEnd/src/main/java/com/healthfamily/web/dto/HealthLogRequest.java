package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.HealthLogType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * 健康日志Request
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record HealthLogRequest(
        @NotNull(message = "日志日期不能为空")
        LocalDate logDate,
        @NotNull(message = "日志类型不能为空")
        HealthLogType type,
        @NotNull(message = "日志内容不能为空")
        Map<String, Object> content,
        BigDecimal score
) {
}

