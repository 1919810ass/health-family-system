package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * 健康计划请求DTO
 */
public record HealthPlanRequest(
        @NotNull(message = "患者用户ID不能为空")
        Long patientUserId,
        
        @NotNull(message = "计划类型不能为空")
        String type,  // HealthPlanType 枚举值
        
        @NotBlank(message = "计划标题不能为空")
        String title,
        
        String description,
        
        @NotNull(message = "开始日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,
        
        @NotNull(message = "执行频率类型不能为空")
        String frequencyType,  // FrequencyType 枚举值
        
        Integer frequencyValue,
        
        String frequencyDetail,  // JSON字符串
        
        String targetIndicators,  // JSON字符串
        
        String reminderStrategy  // JSON字符串
) {
}

