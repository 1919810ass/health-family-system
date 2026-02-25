package com.healthfamily.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
/**
 * Quality报告ItemDto
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
@Builder
public class QualityReportItemDto {
    private String metric;
    private String description;
    private String value;
    private String status;
}