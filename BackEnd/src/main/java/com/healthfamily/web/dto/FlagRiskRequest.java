package com.healthfamily.web.dto;

public record FlagRiskRequest(
    Long sessionId,
    Long messageId,
    String question,
    String aiAnswer,
    String humanCorrection,
    String riskType
) {}
