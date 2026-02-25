package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
/**
 * 家庭InviteRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.Size;

public record FamilyInviteRequest(
        @NotBlank(message = "邀请码不能为空")
        @Size(min = 8, max = 8, message = "邀请码必须是8位")
        @Pattern(regexp = "^[A-Z0-9]{8}$", message = "邀请码格式不正确")
        String inviteCode
) {
}

