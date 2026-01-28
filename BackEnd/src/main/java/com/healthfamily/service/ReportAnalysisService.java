package com.healthfamily.service;

import java.util.Map;

/**
 * 体检/化验报告图片的OCR与解读服务。
 * <p>
 * 说明：当前实现基于本地大模型（如 Ollama Vision），用于从上传的报告图片中提取结构化指标并生成解读。
 */
public interface ReportAnalysisService {

    /**
     * 对报告图片进行OCR与结构化提取。
     *
     * @param imageUrl 上传后的图片访问路径（例如：/api/files/{date}/{filename}）
     * @return 结构化OCR结果（包含 items / interpretation 等字段）
     */
    Map<String, Object> performOcr(String imageUrl);

    /**
     * 对OCR结果进行进一步解读（如需要二次模型推理，可在实现中扩展）。
     *
     * @param ocrData performOcr 的返回值
     * @return 解读结果（建议包含 summary / details）
     */
    Map<String, Object> analyzeReport(Map<String, Object> ocrData);
}

