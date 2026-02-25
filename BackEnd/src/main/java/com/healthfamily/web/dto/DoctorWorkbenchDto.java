package com.healthfamily.web.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 医生工作台聚合数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWorkbenchDto {

    private Integer managedFamilies;
    private Integer totalPatients;
    private Integer pendingAlerts;
    private Integer monthlyReports;

    /**
     * 今日节气名称（用于前端展示“惊蛰”等）
     */
    private String seasonalSolarTerm;

    /**
     * 基础统计数据
     */
    private DoctorStatsResponse stats;

    /**
     * 最近异常事件列表（跨家庭汇总，按时间倒序，最多10条）
     */
    private List<AbnormalEventDto> abnormalEvents;

    /**
     * 高风险患者列表 (Top 5)
     * 使用 HighRiskMemberDto，以便前端展示详细风险原因
     */
    private List<HighRiskMemberDto> criticalPatients;

    /**
     * 待处理咨询/分诊 (Top 5)
     */
    private List<ConsultationTaskDto> pendingConsultations;

    /**
     * 今日中医时令建议
     */
    private String seasonalAdvice;
}
