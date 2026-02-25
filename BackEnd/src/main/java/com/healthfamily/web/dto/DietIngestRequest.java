package com.healthfamily.web.dto;

/**
 * DietIngestRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.Size;

public record DietIngestRequest(
        Long userId,
        Long familyId,
        @Size(max = 512, message = "图片URL过长")
        String imageUrl,
        @Size(max = 1000, message = "描述过长")
        String description,
        @Size(max = 100, message = "份量描述过长")
        String quantity
) {}

