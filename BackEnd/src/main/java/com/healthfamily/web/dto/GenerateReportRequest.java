package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateReportRequest(
    @NotNull(message = "患者ID不能为空")
    Long userId,
    
    @NotBlank(message = "诊断意见不能为空")
    String diagnosis,

    /**
     * 医生在“预览草稿”基础上编辑后的最终正文（可选）。
     * - 若为空：后端将基于 diagnosis + 患者健康数据 + RAG 检索结果调用大模型生成正文
     * - 若不为空：后端直接使用该正文生成 docx（仍会附加 RAG 证据列表，便于溯源）
     */
    String finalContent
) {}
