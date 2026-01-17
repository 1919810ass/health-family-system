package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 监测预警DTO（用于前端列表展示）
 */
public record MonitoringAlertDto(
        Long id,
        
        String type,  // ALERT, REMINDER
        
        String title,
        String description,
        
        Long memberUserId,
        String memberName,
        String memberAvatar,
        
        Long familyId,
        String familyName,
        
        String severity,  // HIGH, MEDIUM, LOW, CRITICAL（仅用于ALERT）
        String status,  // PENDING, HANDLED, ESCALATED
        
        String metric,  // 指标名称（仅用于ALERT）
        Double value,  // 指标值（仅用于ALERT）
        Double threshold,  // 阈值（仅用于ALERT）
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime handledAt,
        
        // 原始数据ID（用于标记已处理）
        Long alertId,  // HealthAlert的ID（如果type=ALERT）
        Long reminderId  // HealthReminder的ID（如果type=REMINDER）
) {
}

