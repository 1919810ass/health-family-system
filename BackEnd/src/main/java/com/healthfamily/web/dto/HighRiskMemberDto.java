package com.healthfamily.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 高风险患者DTO
 */
public record HighRiskMemberDto(
        Long userId,           // 用户ID
        String nickname,       // 姓名
        String familyName,     // 家庭名称
        List<String> tags,     // 疾病/风险标签
        LocalDateTime lastAbnormalTime,  // 最近异常时间
        String avatar          // 头像
) {}

