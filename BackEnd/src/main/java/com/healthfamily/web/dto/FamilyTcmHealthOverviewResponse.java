package com.healthfamily.web.dto;

import java.time.LocalDate;
import java.util.List;
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