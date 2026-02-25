package com.healthfamily.web.dto;

/**
 * Result
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.time.Instant;

public record Result<T>(
        int code,
        String message,
        T data,
        long timestamp
) {

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "成功", data, Instant.now().toEpochMilli());
    }

    public static Result<Void> success() {
        return new Result<>(0, "成功", null, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> failure(int code, String message) {
        return new Result<>(code, message, null, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data, Instant.now().toEpochMilli());
    }
}

