package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;

public record DietOptimizeRequest(
        @NotBlank(message = "文本不能为空")
        String text
) {}

