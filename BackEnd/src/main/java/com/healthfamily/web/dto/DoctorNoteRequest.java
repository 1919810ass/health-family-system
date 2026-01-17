package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 医生病历记录请求DTO
 */
public record DoctorNoteRequest(
        @NotNull(message = "问诊日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate consultationDate,
        
        String chiefComplaint,  // 主诉
        
        String pastHistory,  // 既往史
        
        String medication,  // 用药情况
        
        String lifestyleAssessment,  // 生活方式评估
        
        String diagnosisOpinion,  // 诊疗意见
        
        String followupSuggestion,  // 随访建议
        
        String content  // Markdown格式的完整内容（可选，与上述字段二选一）
) {
}

