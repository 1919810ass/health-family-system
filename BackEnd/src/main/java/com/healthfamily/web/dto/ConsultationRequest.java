package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsultationRequest(
        @NotBlank(message = "问题不能为空")
        String question,
        String sessionId  // 会话ID，用于上下文对话
) {
}

