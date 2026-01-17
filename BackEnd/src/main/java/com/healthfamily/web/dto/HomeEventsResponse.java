package com.healthfamily.web.dto;

import java.util.List;

public record HomeEventsResponse(
        Long familyId,
        List<HomeEventItem> items
) {}
