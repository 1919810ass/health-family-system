package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FamilyCreateRequest(
        @NotBlank(message = "家庭名称不能为空")
        @Size(max = 64, message = "家庭名称不能超过64个字符")
        String name
) {
}

