package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.InteractionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 家庭InteractionDto
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
@Builder
public class FamilyInteractionDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long targetUserId;
    private String targetUserName;
    private InteractionType type;
    private String content;
    private LocalDateTime createdAt;
}
