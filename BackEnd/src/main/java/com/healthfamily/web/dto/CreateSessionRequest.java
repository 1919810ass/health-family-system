package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建咨询会话请求DTO
 */
public record CreateSessionRequest(
        @NotNull(message = "患者用户ID不能为空")
        Long patientUserId,
        
        @NotNull(message = "家庭ID不能为空")
        Long familyId,
        
        Long doctorId,  // 可选，如果为空则创建家庭成员之间的会话
        
        String title  // 可选，会话标题
) {
}

