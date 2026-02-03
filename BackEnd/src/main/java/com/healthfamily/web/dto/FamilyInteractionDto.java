package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.InteractionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
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
