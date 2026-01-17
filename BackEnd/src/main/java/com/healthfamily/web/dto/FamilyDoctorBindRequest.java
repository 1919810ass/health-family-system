package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotNull;

public record FamilyDoctorBindRequest(
        @NotNull Long doctorUserId
) {}

