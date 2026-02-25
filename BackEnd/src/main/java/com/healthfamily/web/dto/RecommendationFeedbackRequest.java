package com.healthfamily.web.dto;

/**
 * 推荐FeedbackRequest
 * <p>
 * 承载平台业务功能，供上层调用或作为领域组件使用。
 * </p>
 */
import jakarta.validation.constraints.NotNull;

public record RecommendationFeedbackRequest(
        @NotNull(message = "反馈状态不能为空")
        Boolean accepted
) {
}

