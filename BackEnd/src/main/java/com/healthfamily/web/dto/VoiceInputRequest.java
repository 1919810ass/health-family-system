package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;

public record VoiceInputRequest(
        @NotBlank(message = "语音文本不能为空")
        String voiceText,
        String dataType
) {
}

