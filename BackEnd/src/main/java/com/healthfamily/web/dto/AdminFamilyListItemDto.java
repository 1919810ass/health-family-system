package com.healthfamily.web.dto;

/**
 * 管理员家庭ListItemDto
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.time.LocalDateTime;

public record AdminFamilyListItemDto(
        Long id,
        String name,
        String ownerName,
        String ownerAvatar,
        Long memberCount,
        Long healthLogCount,
        Long assessmentCount,
        Integer status,
        LocalDateTime createdAt
) {}

