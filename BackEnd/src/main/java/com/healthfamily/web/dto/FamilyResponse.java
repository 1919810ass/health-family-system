package com.healthfamily.web.dto;

public record FamilyResponse(
        Long id,
        String name,
        Long ownerId,
        String inviteCode
) {
}

