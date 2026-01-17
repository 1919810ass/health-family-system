package com.healthfamily.web.dto;

/**
 * 医生设置响应DTO
 */
public record DoctorSettingsResponse(
        DoctorNotificationSettings notifications,  // 通知设置
        
        WorkingHours workingHours  // 工作时间设置
) {
    /**
     * 工作时间设置
     */
    public record WorkingHours(
            java.util.List<String> workDays,  // 工作日列表
            String startTime,  // 开始时间
            String endTime     // 结束时间
    ) {}
}

