package com.healthfamily.web.dto;

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

