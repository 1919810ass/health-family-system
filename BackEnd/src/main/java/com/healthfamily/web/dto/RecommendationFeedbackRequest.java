package com.healthfamily.web.dto;

import jakarta.validation.constraints.NotNull;

public record RecommendationFeedbackRequest(
        @NotNull(message = "反馈状态不能为空")
        Boolean accepted
) {
}

