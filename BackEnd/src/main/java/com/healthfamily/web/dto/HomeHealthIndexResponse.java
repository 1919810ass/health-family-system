package com.healthfamily.web.dto;

public record HomeHealthIndexResponse(
        Long familyId,
        Integer score,
        String rule
) {}
