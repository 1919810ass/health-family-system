package com.healthfamily.web.dto;

/**
 * RAG 检索到的证据片段（用于前端展示与报告溯源）。
 */
public record RagEvidenceDto(
        Long fragmentId,
        String title,
        String source,
        String snippet,
        String content
) {}

