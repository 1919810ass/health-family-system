package com.healthfamily.web.dto;

import java.util.Map;

public record HomeStatusDistributionResponse(
        Long familyId,
        Map<String, Integer> distribution
) {}
