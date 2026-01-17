package com.healthfamily.web.dto;

import java.time.Instant;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        long timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "成功", data, Instant.now().toEpochMilli());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data, Instant.now().toEpochMilli());
    }

    public static <T> ApiResponse<T> failure(int code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now().toEpochMilli());
    }
}

