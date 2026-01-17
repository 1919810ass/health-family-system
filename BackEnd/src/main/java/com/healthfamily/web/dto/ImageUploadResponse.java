package com.healthfamily.web.dto;

public record ImageUploadResponse(
        String url,
        String recognizedFood,
        Double confidence,
        Long timestamp
) {
}
