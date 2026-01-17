package com.healthfamily.web.dto;

/**
 * 医生通知设置（细分策略）
 */
public record DoctorNotificationSettings(
        // 系统通知（系统消息、账户相关等）
        NotificationChannels system,
        // 随访提醒（健康计划、随访任务等）
        NotificationChannels followup,
        // 预警通知（健康预警、异常数据等）
        NotificationChannels alert
) {
    /**
     * 通知渠道配置
     */
    public record NotificationChannels(
            Boolean inApp,  // APP 内通知
            Boolean email,  // 邮件通知
            Boolean sms     // 短信通知
    ) {}
}

