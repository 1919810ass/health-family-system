package com.healthfamily.web.dto;

import java.time.LocalDate;
import java.util.List;

public record MemberStatusResponse(
        Long userId,
        String nickname,
        String avatar,
        String summary,
        Boolean abnormal,
        LocalDate logDate,
        List<MetricDetail> metrics
) {}
