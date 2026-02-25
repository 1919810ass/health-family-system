package com.healthfamily.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
/**
 * SeasonalWellnessDTO
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
@AllArgsConstructor
public class SeasonalWellnessDTO {
    private String solarTerm;
    private String constitution;
    private String advice;
    private String imageUrl;
}
