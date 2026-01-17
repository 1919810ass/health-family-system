package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度应在6-64位之间")
        String password,
        @Size(max = 64, message = "昵称不能超过64个字符")
        String nickname
) {
}

