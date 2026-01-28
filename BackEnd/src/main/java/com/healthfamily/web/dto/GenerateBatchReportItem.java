package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateBatchReportItem(
        @NotNull(message = "患者ID不能为空")
        Long userId,
        @NotBlank(message = "诊断意见不能为空")
        String diagnosis,
        String finalContent
) {}
