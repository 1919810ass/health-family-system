package com.healthfamily.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
/**
 * GenerateBatch报告Request
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.List;

public record GenerateBatchReportRequest(
        @NotEmpty(message = "批量生成列表不能为空")
        @Valid List<GenerateBatchReportItem> items
) {}
