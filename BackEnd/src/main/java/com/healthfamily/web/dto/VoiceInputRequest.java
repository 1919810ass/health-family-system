package com.healthfamily.web.dto;

/**
 * VoiceInputRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotBlank;

public record VoiceInputRequest(
        @NotBlank(message = "语音文本不能为空")
        String voiceText,
        String dataType
) {
}

