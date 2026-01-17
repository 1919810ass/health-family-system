package com.healthfamily.service;

import com.healthfamily.domain.entity.HealthReminder;
import com.healthfamily.web.dto.ReminderRequest;
import com.healthfamily.web.dto.ReminderResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 智能健康提醒服务
 */
public interface HealthReminderService {

    /**
     * 创建提醒
     */
    ReminderResponse createReminder(Long userId, ReminderRequest request);

    /**
     * 获取用户的提醒列表
     */
    List<ReminderResponse> getUserReminders(Long userId, String status);

    /**
     * 获取用户的待办提醒列表（包括待处理和已发送未确认的）
     */
    List<ReminderResponse> getUserTodoReminders(Long userId);

    /**
     * 更新提醒状态
     */
    ReminderResponse updateReminderStatus(Long userId, Long reminderId, String status);

    /**
     * 删除提醒
     */
    void deleteReminder(Long userId, Long reminderId);

    /**
     * 生成智能提醒（基于健康数据）
     */
    List<ReminderResponse> generateSmartReminders(Long userId);

    /**
     * 为指定用户生成智能提醒（基于健康数据）
     */
    List<ReminderResponse> generateSmartRemindersForUser(Long adminId, Long targetUserId);

    /**
     * 生成协作提醒（基于家庭协作任务）
     */
    List<ReminderResponse> generateSmartReminders(Long userId, Long familyId);

    /**
     * 获取家庭范围的提醒列表（协作任务）
     */
    List<ReminderResponse> getFamilyReminders(Long userId, Long familyId);
    
    // 管理员功能相关方法
    Map<String, Object> getAdminReminderList(Long userId, String reminderType, String status, String keyword, int page, int size, LocalDateTime startTime, LocalDateTime endTime);
    HealthReminder findById(Long id);
    HealthReminder create(HealthReminder reminder);
    HealthReminder update(Long id, HealthReminder reminder);
    boolean deleteById(Long id);
    boolean updateStatus(Long id, String status);
}

