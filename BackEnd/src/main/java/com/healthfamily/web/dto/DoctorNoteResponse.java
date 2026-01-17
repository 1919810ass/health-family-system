package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生病历记录响应DTO
 */
public record DoctorNoteResponse(
        Long id,
        
        Long doctorId,
        String doctorName,
        
        Long patientUserId,
        String patientName,
        
        Long familyId,
        String familyName,
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate consultationDate,
        
        String chiefComplaint,  // 主诉
        
        String pastHistory,  // 既往史
        
        String medication,  // 用药情况
        
        String lifestyleAssessment,  // 生活方式评估
        
        String diagnosisOpinion,  // 诊疗意见
        
        String followupSuggestion,  // 随访建议
        
        String content,  // Markdown格式的完整内容
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}

