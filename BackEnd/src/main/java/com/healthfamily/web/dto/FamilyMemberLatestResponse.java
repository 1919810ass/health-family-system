package com.healthfamily.web.dto;

import java.time.LocalDateTime;

public record FamilyMemberLatestResponse(
        Long userId,
        String nickname,
        String avatar,
        Long assessmentId,
        String primaryType,
        Double confidence,
        LocalDateTime createdAt
) {
}
