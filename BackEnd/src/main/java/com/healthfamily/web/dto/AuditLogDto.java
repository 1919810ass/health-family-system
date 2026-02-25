package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.AuditResult;
import com.healthfamily.domain.constant.SensitivityLevel;

/**
 * Audit日志Dto
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.time.LocalDateTime;

public record AuditLogDto(
    Long id,
    Long userId,
    String username,
    String userRole,
    String action,
    String resource,
    SensitivityLevel sensitivityLevel,
    AuditResult result,
    String ip,
    String userAgent,
    String extraJson,
    LocalDateTime createdAt
) {}
