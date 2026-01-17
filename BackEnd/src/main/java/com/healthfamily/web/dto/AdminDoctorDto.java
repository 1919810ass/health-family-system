package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员端医生信息DTO
 */
public record AdminDoctorDto(
        Long id,
        String name,              // 姓名（nickname）
        String phone,             // 手机号
        String email,             // 邮箱
        String hospital,          // 执业医院
        String department,        // 科室
        String specialty,         // 专业领域
        String title,             // 职称
        String bio,               // 简介
        Boolean certified,        // 是否已认证
        String certificationStatus, // 认证状态：PENDING, APPROVED, REJECTED
        Integer status,           // 用户状态：1-启用，0-禁用
        BigDecimal rating,        // 评分
        Integer ratingCount,      // 评分人数
        Integer serviceCount,     // 服务用户数
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime certifiedAt,
        String rejectReason       // 拒绝原因
) {
}



