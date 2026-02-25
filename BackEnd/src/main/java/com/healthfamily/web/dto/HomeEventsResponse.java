package com.healthfamily.web.dto;

/**
 * HomeEventsResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.List;

public record HomeEventsResponse(
        Long familyId,
        List<HomeEventItem> items
) {}
