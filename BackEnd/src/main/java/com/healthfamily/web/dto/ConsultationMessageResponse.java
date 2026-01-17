package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 咨询消息响应DTO
 */
public record ConsultationMessageResponse(
        Long id,
        
        Long sessionId,
        
        Long senderId,
        String senderName,
        String senderType,  // DOCTOR, FAMILY_MEMBER, MEMBER
        
        String content,
        
        String messageType,  // TEXT, TEMPLATE
        String templateId,
        
        Boolean readByDoctor,
        Boolean readByPatient,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
}

