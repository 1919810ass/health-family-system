/**
 * 家庭医生Response
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
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

