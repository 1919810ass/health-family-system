package com.healthfamily.web.dto;

/**
 * 问诊Request
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotBlank;

public record ConsultationRequest(
        @NotBlank(message = "问题不能为空")
        String question,
        String sessionId  // 会话ID，用于上下文对话
) {
}

