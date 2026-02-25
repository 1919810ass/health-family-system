package com.healthfamily.web.dto;

/**
 * OptimizeInputRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotBlank;

public record OptimizeInputRequest(
        @NotBlank(message = "文本不能为空")
        String text,
        @NotBlank(message = "类型不能为空")
        String type // DIET / SLEEP / SPORT / MOOD / VITALS
) {}

