package com.healthfamily.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
/**
 * 用户ActivityStatsDto
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
@Builder
public class UserActivityStatsDto {
    private Long dailyActiveUsers;
    private Long weeklyActiveUsers;
    private Long monthlyActiveUsers;
    private Long onlineUsers;
    private Long todayVisits;
    private List<Map<String, Object>> onlineUsersList;
    private List<Map<String, Object>> loginFrequency;
    private List<Map<String, Object>> featureUsage;
    private List<Map<String, Object>> loginLogs;
    private Integer totalLoginLogs;
}
