package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
/**
 * ChangePasswordRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 8) String newPassword
) {}

