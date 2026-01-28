package com.healthfamily.web.dto;

import java.util.List;

/**
 * 智能病例报告生成的预览结果。
 */
public record ReportGenerationPreviewResponse(
        String draftContent,
        List<RagEvidenceDto> evidences
) {}

