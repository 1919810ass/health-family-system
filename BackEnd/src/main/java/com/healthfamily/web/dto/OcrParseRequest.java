package com.healthfamily.web.dto;

/**
 * OcrParseRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotBlank;

public record OcrParseRequest(
        @NotBlank(message = "图片数据不能为空")
        String imageBase64
) {
}

