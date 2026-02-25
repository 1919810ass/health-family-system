/**
 * Home健康IndexResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
package com.healthfamily.web.dto;

public record HomeHealthIndexResponse(
        Long familyId,
        Integer score,
        String rule
) {}
