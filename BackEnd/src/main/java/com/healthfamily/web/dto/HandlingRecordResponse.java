package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 异常处理记录响应DTO
 */
public record HandlingRecordResponse(
        Long id,
        
        Long alertId,           // 关联的异常ID
        String alertTitle,      // 异常标题
        String alertMetric,     // 异常指标
        Double alertValue,      // 异常值
        
        Long doctorId,          // 处理医生ID
        String doctorName,      // 处理医生姓名
        
        Long patientId,         // 患者ID
        String patientName,     // 患者姓名
        
        String handlingAction,   // 处理动作：notify, call, referral
        String handlingContent,  // 处理内容
        String handlingNote,     // 处理备注
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime handledAt,  // 处理时间
        
        Boolean followUpRequired,  // 是否需要后续跟踪
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime followUpTime, // 跟踪时间
        String followUpResult      // 跟踪结果
) {
}