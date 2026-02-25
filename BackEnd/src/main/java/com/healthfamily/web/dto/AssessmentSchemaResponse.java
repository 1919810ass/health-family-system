package com.healthfamily.web.dto;

import java.util.List;
/**
 * 测评SchemaResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record AssessmentSchemaResponse(
        String type,
        String title,
        String description,
        List<Map<String, Object>> dimensions
) {
}

