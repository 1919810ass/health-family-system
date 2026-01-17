package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 医生注册请求DTO（需要管理员审核）
 */
public record DoctorRegisterRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "密码不能为空")
        String password,

        @NotBlank(message = "姓名不能为空")
        String name,

        @NotBlank(message = "执业医院不能为空")
        String hospital,

        @NotBlank(message = "科室不能为空")
        String department,

        @NotBlank(message = "专业领域不能为空")
        String specialty,

        String title,              // 职称（可选）

        String bio,                // 简介（可选）

        String email,              // 邮箱（可选）

        @NotBlank(message = "执业证书编号不能为空")
        String licenseNumber,      // 执业证书编号

        String licenseImage,       // 执业证书图片路径（上传后返回）

        @NotBlank(message = "身份证号不能为空")
        String idCard,             // 身份证号

        String idCardFront,        // 身份证正面图片路径

        String idCardBack          // 身份证反面图片路径
) {
}



