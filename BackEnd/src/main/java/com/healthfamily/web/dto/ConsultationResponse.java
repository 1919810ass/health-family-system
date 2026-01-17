package com.healthfamily.web.dto;

import java.time.LocalDateTime;
import java.util.List;
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

