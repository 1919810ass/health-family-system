package com.healthfamily.web.dto;

public record FamilyDoctorResponse(
        Long doctorUserId,
        String nickname,
        String phone,
        String avatar,
        String title,
        String hospital,
        String department,
        String bio,
        Double rating,
        Integer serviceCount
) {}

