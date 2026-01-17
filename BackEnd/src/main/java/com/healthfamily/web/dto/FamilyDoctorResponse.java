package com.healthfamily.web.dto;

public record FamilyDoctorResponse(
        Long doctorUserId,
        String nickname,
        String phone,
        String avatar
) {}

