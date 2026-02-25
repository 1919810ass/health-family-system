package com.healthfamily.web.dto;

/**
 * 家庭MemberResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.List;

public record FamilyMemberResponse(
        Long memberId,
        Long userId,
        String nickname,
        String relation,
        Boolean admin,
        String phone,
        String avatar,
        String role,
        List<String> tags,
        String lastActive
) {
}
