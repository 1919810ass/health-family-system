package com.healthfamily.web.dto;

import java.time.LocalDateTime;

public record DoctorRatingResponse(
    Long id,
    String userName,
    String userAvatar,
    Integer rating,
    String comment,
    LocalDateTime createdAt
) {}