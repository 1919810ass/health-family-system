package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康计划响应DTO
 */
public record HealthPlanResponse(
        Long id,
        
        Long doctorId,
        String doctorName,
        
        Long patientUserId,
        String patientName,
        
        Long familyId,
        String familyName,
        
        String type,
        String typeLabel,
        
        String title,
        String description,
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,
        
        String frequencyType,
        String frequencyTypeLabel,
        Integer frequencyValue,
        String frequencyDetail,
        
        String targetIndicators,
        String reminderStrategy,
        
        String status,
        String statusLabel,
        
        BigDecimal completionRate,
        BigDecimal complianceRate,
        
        String metadataJson,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}

