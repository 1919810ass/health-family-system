package com.healthfamily.web.dto;

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

