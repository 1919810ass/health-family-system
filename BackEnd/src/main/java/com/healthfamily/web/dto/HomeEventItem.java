package com.healthfamily.web.dto;

import java.time.LocalDateTime;

public record HomeEventItem(
        String type,
        String title,
        String content,
        Long userId,
        String nickname,
        LocalDateTime time
) {}
