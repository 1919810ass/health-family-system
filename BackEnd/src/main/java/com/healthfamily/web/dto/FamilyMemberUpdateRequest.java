package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotNull;

public record FamilyMemberUpdateRequest(
        String relation,
        String role,
        Boolean shareToFamily
) {
}

