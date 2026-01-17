package com.healthfamily.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class UserActivityStatsDto {
    private Long dailyActiveUsers;
    private Long weeklyActiveUsers;
    private Long monthlyActiveUsers;
    private Long onlineUsers;
    private List<Map<String, Object>> onlineUsersList;
    private List<Map<String, Object>> loginFrequency;
    private List<Map<String, Object>> featureUsage;
    private List<Map<String, Object>> loginLogs;
    private Integer totalLoginLogs;
}