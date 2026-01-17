package com.healthfamily.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 64) String nickname,
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误") String phone,
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "邮箱格式错误") String email
) {}

