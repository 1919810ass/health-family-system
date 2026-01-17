package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送咨询消息请求DTO
 */
public record ConsultationMessageRequest(
        @NotNull(message = "会话ID不能为空")
        Long sessionId,
        
        @NotBlank(message = "消息内容不能为空")
        String content,
        
        String messageType,  // TEXT 或 TEMPLATE
        
        String templateId  // 模板ID（如果是模板回复）
) {
}

