package com.healthfamily.web.dto;

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
