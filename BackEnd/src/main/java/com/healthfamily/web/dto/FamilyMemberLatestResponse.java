package com.healthfamily.web.dto;

/**
 * 家庭MemberLatestResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
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
