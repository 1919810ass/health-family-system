package com.healthfamily.web.dto;

/**
 * 用户画像Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record UserProfileResponse(
        Long id,
        String phone,
        String nickname,
        String email,
        String avatar,
        String role,
        Map<String, Object> notifications
) {}

