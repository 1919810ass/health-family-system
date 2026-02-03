package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.InteractionType;
import lombok.Data;

@Data
public class SendInteractionRequest {
    private Long targetUserId;
    private Long familyId;
    private InteractionType type;
    private String content;
}
