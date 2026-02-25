package com.healthfamily.web.dto;

import lombok.Data;

/**
 * TriageChatRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
@Data
public class TriageChatRequest {
    private Long sessionId;
    private String userMessage; // 用户的新回复
}
