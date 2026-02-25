package com.healthfamily.web.dto;

/**
 * Home趋势Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.List;

public record HomeTrendResponse(
        Long familyId,
        String metric,
        String period,
        List<HomeTrendPoint> series
) {}
