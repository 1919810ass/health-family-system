package com.healthfamily.web.dto;

import java.util.List;

public record HomeAbnormalTodayResponse(
        Long familyId,
        Integer count,
        List<MemberStatusResponse> items
) {}
