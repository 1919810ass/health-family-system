package com.healthfamily.web.dto;

import jakarta.validation.Valid;

/**
 * 医生设置请求DTO
 * 注意：notifications 和 workingHours 可以为 null，表示不更新该部分设置
 */
public record DoctorSettingsRequest(
        @Valid
        DoctorNotificationSettings notifications,  // 通知设置（可选）
        
        WorkingHours workingHours  // 工作时间设置（可选）
) {
    /**
     * 工作时间设置
     */
    public record WorkingHours(
            java.util.List<String> workDays,  // 工作日列表：["MON", "TUE", ...]
            String startTime,  // 开始时间：HH:mm 格式，如 "09:00"
            String endTime     // 结束时间：HH:mm 格式，如 "18:00"
    ) {}
}

