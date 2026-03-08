package com.healthfamily.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "health_reminder_templates")
public class HealthReminderTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // 提醒内容

    @Column(nullable = false)
    private String category; // 分类

    @Column(name = "user_count", columnDefinition = "INT DEFAULT 0")
    private Integer userCount = 0; // 设置用户数

    @Column(nullable = false)
    private Integer status; // 状态 (1: 启用, 0: 禁用)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
