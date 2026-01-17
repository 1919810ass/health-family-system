package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "token不能为空")
        String token
) {
}

