package com.healthfamily.web.dto;

import java.time.LocalDateTime;

public record AuditSessionDto(
    Long id,
    String title,
    String patientName,
    String doctorName,
    LocalDateTime lastMessageAt,
    Integer messageCount,
    Boolean hasRisk,
    String triageSummary
) {}
