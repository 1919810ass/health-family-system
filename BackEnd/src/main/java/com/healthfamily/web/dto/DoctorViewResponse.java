package com.healthfamily.web.dto;

import java.util.List;
/**
 * 医生ViewResponse
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import java.util.Map;

public record DoctorViewResponse(
        Long familyId,
        String summary,
        Map<String, Object> telemetry,
        // 待办事项统计
        Integer pendingConsultationsCount,  // 待回复咨询数
        Integer pendingPlansCount,          // 待审核计划数
        Integer todayFollowupsCount,        // 今日需随访数
        // 异常事件列表
        List<AbnormalEventDto> abnormalEvents,
        // 高风险患者列表
        List<HighRiskMemberDto> highRiskMembers,

        // 数据简报
        Integer weeklyConsultations,
        Integer newPatients,
        Integer improvementRate
) {
    // 兼容旧版本的构造函数
    public DoctorViewResponse(Long familyId, String summary, Map<String, Object> telemetry) {
        this(familyId, summary, telemetry, 0, 0, 0, List.of(), List.of(), 0, 0, 0);
    }
}

