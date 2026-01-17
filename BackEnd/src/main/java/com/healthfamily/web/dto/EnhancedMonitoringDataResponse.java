package com.healthfamily.web.dto;

import java.util.List;
import java.util.Map;

/**
 * 增强的健康监测数据响应DTO
 * 包含更详细的异常数据和处理信息
 */
public record EnhancedMonitoringDataResponse(
        // 图表数据（近30天异常事件趋势）
        List<AbnormalEventTrendPoint> trendData,
        
        // 异常事件类型统计（饼图数据）
        Map<String, Integer> eventTypeStats,
        
        // 预警列表（按紧急程度排序）
        List<EnhancedMonitoringAlertDto> alerts,
        
        // 高风险患者列表
        List<HighRiskMemberDto> highRiskMembers,
        
        // 统计摘要
        MonitoringStatsSummary statsSummary
) {
    /**
     * 异常事件趋势点
     */
    public record AbnormalEventTrendPoint(
            String date,  // 日期（YYYY-MM-DD）
            Integer count,  // 异常事件总数量
            Integer criticalCount,  // 危急值数量
            Integer highCount,  // 高风险数量
            Integer mediumCount,  // 中风险数量
            Integer lowCount  // 低风险数量
    ) {}

    /**
     * 统计摘要
     */
    public record MonitoringStatsSummary(
            Integer totalAlerts,      // 总异常数
            Integer pendingAlerts,    // 待处理异常数
            Integer handledAlerts,    // 已处理异常数
            Integer criticalAlerts,   // 危急异常数
            Integer highAlerts,       // 高风险异常数
            Integer mediumAlerts,     // 中风险异常数
            Integer lowAlerts         // 低风险异常数
    ) {}
}