package com.healthfamily.web.dto;

import java.time.LocalDate;
import java.util.List;
/**
 * 家庭中医体质健康OverviewResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record FamilyTcmHealthOverviewResponse(
    Long familyId,
    String familyName,
    int memberCount,
    List<FamilyMemberHealthInfoDto> members,
    Map<String, Integer> constitutionDistribution,
    String familyRecommendation,
    LocalDate generatedAt
) {
    
    public record FamilyMemberHealthInfoDto(
        Long userId,
        String userName,
        String relationship,
        String primaryConstitution,
        Boolean hasConstitutionData,
        Map<String, Double> scores
    ) {}
}