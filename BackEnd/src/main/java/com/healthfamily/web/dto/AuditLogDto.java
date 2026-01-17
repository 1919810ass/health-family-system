package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.AuditResult;
import com.healthfamily.domain.constant.SensitivityLevel;

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
