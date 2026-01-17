package com.healthfamily.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 医生统计响应DTO
 */
public record DoctorStatsResponse(
        // 患者结构统计
        PatientStructureStats patientStructure,
        
        // 管理效果统计
        ManagementEffectStats managementEffect,
        
        // 工作负载统计
        WorkloadStats workload
) {
    
    /**
     * 患者结构统计
     */
    public record PatientStructureStats(
            Map<String, Integer> ageDistribution,  // 年龄段分布：{"0-18": 5, "19-35": 10, ...}
            Map<String, Integer> genderDistribution,  // 性别分布：{"M": 15, "F": 10}
            Map<String, Integer> diseaseDistribution  // 疾病类别分布：{"高血压": 8, "糖尿病": 5, ...}
    ) {}
    
    /**
     * 管理效果统计
     */
    public record ManagementEffectStats(
            BloodPressureStats bloodPressure,  // 血压达标率
            WeightStats weight,  // 体重管理
            SleepStats sleep  // 睡眠改善
    ) {
        public record BloodPressureStats(
                Double complianceRate,  // 血压达标率（%）
                Integer totalCount,  // 总测量次数
                Integer compliantCount,  // 达标次数
                List<DateValue> trend  // 趋势数据（日期 -> 达标率）
        ) {}
        
        public record WeightStats(
                Double averageWeightChange,  // 平均体重变化（kg，负数为下降）
                Integer totalPatients,  // 参与体重管理的患者数
                Integer weightLossCount,  // 体重下降的患者数
                List<DateValue> trend  // 趋势数据（日期 -> 平均体重）
        ) {}
        
        public record SleepStats(
                Double averageSleepHours,  // 平均睡眠时长（小时）
                Double improvementRate,  // 改善率（%）
                Integer totalRecords,  // 总记录数
                List<DateValue> trend  // 趋势数据（日期 -> 平均睡眠时长）
        ) {}
    }
    
    /**
     * 工作负载统计
     */
    public record WorkloadStats(
            ConsultationStats consultation,  // 咨询统计
            FollowupStats followup,  // 随访统计
            ReminderStats reminder  // 提醒统计
    ) {
        public record ConsultationStats(
                Integer totalCount,  // 总咨询次数
                Integer activeSessions,  // 活跃会话数
                List<DateValue> trend  // 趋势数据（日期 -> 咨询次数）
        ) {}
        
        public record FollowupStats(
                Integer totalPlans,  // 总计划数
                Integer activePlans,  // 进行中的计划数
                Integer completedPlans,  // 已完成的计划数
                List<DateValue> trend  // 趋势数据（日期 -> 计划数）
        ) {}
        
        public record ReminderStats(
                Integer totalSent,  // 总发送数
                Integer completed,  // 已完成数
                Integer pending,  // 待处理数
                Double completionRate,  // 完成率（%）
                List<DateValue> trend  // 趋势数据（日期 -> 发送数）
        ) {}
    }
    
    /**
     * 日期-值对（用于趋势图）
     */
    public record DateValue(
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate date,
            Double value
    ) {}
}

