package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;

public record OptimizeInputRequest(
        @NotBlank(message = "文本不能为空")
        String text,
        @NotBlank(message = "类型不能为空")
        String type // DIET / SLEEP / SPORT / MOOD / VITALS
) {}

