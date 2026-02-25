package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
/**
 * 家庭UpdateRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.Size;

public record FamilyUpdateRequest(
        @NotBlank(message = "家庭名称不能为空")
        @Size(max = 64, message = "家庭名称不能超过64个字符")
        String name
) {
}

