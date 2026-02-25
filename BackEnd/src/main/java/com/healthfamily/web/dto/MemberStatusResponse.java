package com.healthfamily.web.dto;

import java.time.LocalDate;
/**
 * MemberStatusResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.List;

public record MemberStatusResponse(
        Long userId,
        String nickname,
        String avatar,
        String summary,
        Boolean abnormal,
        LocalDate logDate,
        List<MetricDetail> metrics
) {}
