package com.healthfamily.web.dto;

/**
 * 医生RatingResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.time.LocalDateTime;

public record DoctorRatingResponse(
    Long id,
    String userName,
    String userAvatar,
    Integer rating,
    String comment,
    LocalDateTime createdAt,
    String reply,
    LocalDateTime repliedAt
) {}