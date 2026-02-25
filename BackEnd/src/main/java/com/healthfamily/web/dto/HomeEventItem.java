package com.healthfamily.web.dto;

/**
 * HomeEventItem
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.time.LocalDateTime;

public record HomeEventItem(
        String type,
        String title,
        String content,
        Long userId,
        String nickname,
        LocalDateTime time
) {}
