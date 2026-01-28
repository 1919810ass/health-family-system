package com.healthfamily.web.dto;

import lombok.Data;

@Data
public class TriageChatRequest {
    private Long sessionId;
    private String userMessage; // 用户的新回复
}
