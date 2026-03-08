package com.healthfamily.web.dto;

import com.healthfamily.domain.entity.HealthReminderTemplate;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthReminderTemplateDto {
    private Long id;
    private String content;
    private String category;
    private Integer userCount;
    private Integer status;
    private LocalDateTime createdAt;

    public static HealthReminderTemplateDto fromEntity(HealthReminderTemplate entity) {
        HealthReminderTemplateDto dto = new HealthReminderTemplateDto();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setCategory(entity.getCategory());
        dto.setUserCount(entity.getUserCount());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public HealthReminderTemplate toEntity() {
        HealthReminderTemplate entity = new HealthReminderTemplate();
        entity.setId(this.id);
        entity.setContent(this.content);
        entity.setCategory(this.category);
        entity.setUserCount(this.userCount);
        entity.setStatus(this.status);
        // createdAt is auto-generated
        return entity;
    }
}
