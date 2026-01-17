package com.healthfamily.web.dto;

public record AdminFamilyUpsertRequest(
        String name,
        Long ownerId,
        Integer status
) {}

