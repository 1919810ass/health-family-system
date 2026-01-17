package com.healthfamily.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record AssessmentSubmitRequest(
        String type,
        @NotEmpty(message = "答案不能为空")
        List<@Valid AnswerItem> answers
) {

    public record AnswerItem(
            @NotNull(message = "维度不能为空")
            String dimension,
            @NotNull(message = "分数不能为空")
            @PositiveOrZero(message = "分数不能为负数")
            Integer score
    ) {
    }
}

