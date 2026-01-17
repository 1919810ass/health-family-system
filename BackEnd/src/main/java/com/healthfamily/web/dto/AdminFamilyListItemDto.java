package com.healthfamily.web.dto;

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

