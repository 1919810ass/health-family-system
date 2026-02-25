package com.healthfamily.web.dto;

/**
 * RefreshTokenRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "token不能为空")
        String token
) {
}

