package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
/**
 * GenerateBatch报告Item
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotNull;

public record GenerateBatchReportItem(
        @NotNull(message = "患者ID不能为空")
        Long userId,
        @NotBlank(message = "诊断意见不能为空")
        String diagnosis,
        String finalContent
) {}
