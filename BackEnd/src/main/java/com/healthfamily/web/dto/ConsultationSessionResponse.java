package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 咨询会话响应DTO
 */
public record ConsultationSessionResponse(
        Long id,
        
        Long patientUserId,
        String patientName,
        String patientAvatar,
        
        Long familyId,
        String familyName,
        
        Long doctorId,
        String doctorName,
        String doctorAvatar,
        
        String title,
        String status,  // ACTIVE, CLOSED
        
        Integer unreadCountDoctor,
        Integer unreadCountPatient,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastMessageAt,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}

