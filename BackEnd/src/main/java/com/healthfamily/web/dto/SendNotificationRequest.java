package com.healthfamily.web.dto;

/**
 * 发送通知请求DTO
 */
public record SendNotificationRequest(
        Long userId,           // 接收通知的用户ID
        String title,          // 通知标题
        String content,        // 通知内容
        String channel,        // 通知渠道：APP, SMS, CALL
        String type           // 通知类型：ALERT, REMINDER, INFO
) {
}