package com.healthfamily.web.dto;

import java.time.LocalDateTime;
import java.util.List;
/**
 * 问诊Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record ConsultationResponse(
        Long id,
        String sessionId,
        String question,
        String answer,
        Map<String, Object> context,
        List<String> toolsUsed,
        List<String> sources,
        Integer feedback,
        LocalDateTime createdAt
) {
}

