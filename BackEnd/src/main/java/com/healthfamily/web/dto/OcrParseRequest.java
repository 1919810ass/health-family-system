package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;

public record OcrParseRequest(
        @NotBlank(message = "图片数据不能为空")
        String imageBase64
) {
}

