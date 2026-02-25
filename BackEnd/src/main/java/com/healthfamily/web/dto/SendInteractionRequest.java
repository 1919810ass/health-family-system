package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.InteractionType;
import lombok.Data;

/**
 * SendInteractionRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
@Data
public class SendInteractionRequest {
    private Long targetUserId;
    private Long familyId;
    private InteractionType type;
    private String content;
}
