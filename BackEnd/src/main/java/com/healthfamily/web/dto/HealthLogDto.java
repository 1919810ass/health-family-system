package com.healthfamily.web.dto;

import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.entity.HealthLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthLogDto {
    private Long id;
    private String userNickname;
    private HealthLogType type;
    private String contentJson; // 为保持简洁，暂时直接返回JSON字符串
    private LocalDateTime createdAt;

    public static HealthLogDto fromEntity(HealthLog healthLog) {
        HealthLogDto dto = new HealthLogDto();
        dto.setId(healthLog.getId());
        if (healthLog.getUser() != null) {
            dto.setUserNickname(healthLog.getUser().getNickname());
        }
        dto.setType(healthLog.getType());
        dto.setContentJson(healthLog.getContentJson());
        dto.setCreatedAt(healthLog.getCreatedAt());
        return dto;
    }
}
