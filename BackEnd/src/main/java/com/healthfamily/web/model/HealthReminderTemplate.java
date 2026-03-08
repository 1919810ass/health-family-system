package com.healthfamily.web.model;

import java.time.LocalDateTime;

/**
 * 健康提醒模板的数据模型
 */
public class HealthReminderTemplate {

    private Long id;
    private String content; // 提醒内容
    private String category; // 分类
    private Integer userCount; // 设置用户数
    private Integer status; // 状态 (1: 启用, 0: 禁用)
    private LocalDateTime createdAt;

    // Constructors
    public HealthReminderTemplate() {}

    public HealthReminderTemplate(Long id, String content, String category, Integer userCount, Integer status, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.category = category;
        this.userCount = userCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
