package com.healthfamily.web.dto;

import java.util.List;
import java.util.Map;

/**
 * 健康监测数据响应DTO
 */
public record MonitoringDataResponse(
        // 图表数据（近30天异常事件趋势）
        List<AbnormalEventTrendPoint> trendData,
        
        // 异常事件类型统计（饼图数据）
        Map<String, Integer> eventTypeStats,
        
        // 预警列表
        List<MonitoringAlertDto> alerts,
        
        // 高风险患者列表
        List<HighRiskMemberDto> highRiskMembers
) {
    /**
     * 异常事件趋势点
     */
    public record AbnormalEventTrendPoint(
            String date,  // 日期（YYYY-MM-DD）
            Integer count,  // 异常事件数量
            Integer alertCount,  // Alert类型数量
            Integer reminderCount  // Reminder类型数量
    ) {}
}

