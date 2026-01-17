package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 医生认证审核请求DTO
 */
public record DoctorCertificationRequest(
        @NotNull(message = "审核结果不能为空")
        Boolean approved,          // true-通过，false-拒绝

        String rejectReason        // 拒绝原因（拒绝时必填）
) {
}



