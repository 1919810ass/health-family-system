package com.healthfamily.web.dto;

/**
 * 家庭医生BindRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotNull;

public record FamilyDoctorBindRequest(
        @NotNull Long doctorUserId
) {}

