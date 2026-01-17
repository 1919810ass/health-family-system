package com.healthfamily.web.dto;

import java.time.LocalDateTime;

/**
 * 异常事件DTO
 */
public record AbnormalEventDto(
        String id,             // 唯一标识
        String type,           // 类型：LOG, ALERT, REMINDER
        String title,          // 标题
        String content,        // 详细内容
        String level,          // 等级
        LocalDateTime time,    // 时间
        String status,         // 状态
        Long memberUserId      // 关联成员用户ID
) {}
