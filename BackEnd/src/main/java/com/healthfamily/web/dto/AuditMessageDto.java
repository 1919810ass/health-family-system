package com.healthfamily.web.dto;

import java.time.LocalDateTime;

public record AuditMessageDto(
    Long id,
    String senderType,
    String senderName,
    String content,
    String messageType,
    LocalDateTime createdAt,
    Boolean isBadCase
) {}
