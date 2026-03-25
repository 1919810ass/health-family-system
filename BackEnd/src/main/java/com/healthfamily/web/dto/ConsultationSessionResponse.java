package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 咨询会话响应DTO
 */
public record ConsultationSessionResponse(
        Long id,
        
        Long patientUserId,
        String patientName,
        String patientAvatar,
        List<String> patientHealthTags,
        Map<String, Object> latestMetrics,

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
        LocalDateTime updatedAt,
        
        Boolean isAiTriaged,
        String triageSummary,
        String patientSymptoms
) {
}

