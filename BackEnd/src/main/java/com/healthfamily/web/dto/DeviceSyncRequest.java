package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
/**
 * DeviceSyncRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record DeviceSyncRequest(
        @NotBlank(message = "设备ID不能为空")
        String deviceId,
        @NotBlank(message = "设备类型不能为空")
        String deviceType,
        @NotNull(message = "数据日期不能为空")
        LocalDate logDate,
        @NotNull(message = "数据内容不能为空")
        Map<String, Object> data
) {
}

