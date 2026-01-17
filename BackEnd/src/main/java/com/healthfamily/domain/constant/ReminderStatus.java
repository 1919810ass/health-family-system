package com.healthfamily.domain.constant;

/**
 * 提醒状态
 */
public enum ReminderStatus {
    PENDING,         // 待发送
    SENT,           // 已发送
    ACKNOWLEDGED,   // 已确认
    SKIPPED,        // 已跳过
    CANCELLED,      // 已取消
    COMPLETED       // 已完成 (用于任务)
}

