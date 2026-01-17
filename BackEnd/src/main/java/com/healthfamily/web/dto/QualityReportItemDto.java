package com.healthfamily.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QualityReportItemDto {
    private String metric;
    private String description;
    private String value;
    private String status;
}