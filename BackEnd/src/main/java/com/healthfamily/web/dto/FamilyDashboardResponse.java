package com.healthfamily.web.dto;

import java.util.List;

public record FamilyDashboardResponse(
        Long familyId,
        List<MemberStatusResponse> members
) {}

