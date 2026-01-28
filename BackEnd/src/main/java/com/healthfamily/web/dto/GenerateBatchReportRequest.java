package com.healthfamily.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record GenerateBatchReportRequest(
        @NotEmpty(message = "批量生成列表不能为空")
        @Valid List<GenerateBatchReportItem> items
) {}
