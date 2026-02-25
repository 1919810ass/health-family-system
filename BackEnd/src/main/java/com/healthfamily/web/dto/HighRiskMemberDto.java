package com.healthfamily.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 高风险患者DTO
 */
public record HighRiskMemberDto(
        Long userId,                 // 用户ID
        Long familyId,               // 家庭ID（用于拉取详情）
        String nickname,             // 姓名
        String familyName,           // 家庭名称
        List<String> tags,           // 疾病/风险标签
        LocalDateTime lastAbnormalTime,  // 最近异常时间（兼容旧版）
        String avatar,               // 头像

        // 下面为新版高风险详情字段，用于前端精细化展示

        String riskType,             // 风险类型: VITALS_WARNING / TCM_IMBALANCE / 其他
        String riskDescription,      // 风险具体描述，例如: "收缩压持续偏高 (165mmHg)"
        String riskLevel,            // 风险等级: CRITICAL / WARNING / NOTICE
        LocalDateTime riskTime,      // 风险触发时间
        boolean requiresImmediateAction // 是否需要立即干预
) {}


