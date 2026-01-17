package com.healthfamily.web.dto;

import java.util.List;

public record HomeTrendResponse(
        Long familyId,
        String metric,
        String period,
        List<HomeTrendPoint> series
) {}
