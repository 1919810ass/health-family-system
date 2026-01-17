package com.healthfamily.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertResponse {
    private Long id;
    private Long userId;
    private String metric;
    private Double value;
    private Double threshold;
    private String message;
    private String severity;
    private String status;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
    private Integer escalationLevel;
    private Long familyId;

    // RequiredArgsConstructor for backwards compatibility with record usage
    public AlertResponse(Long id, Long userId, String metric, Double value, Double threshold, String message,
                         String severity, String status, String channel, LocalDateTime createdAt,
                         LocalDateTime handledAt, Integer escalationLevel, Long familyId) {
        this.id = id;
        this.userId = userId;
        this.metric = metric;
        this.value = value;
        this.threshold = threshold;
        this.message = message;
        this.severity = severity;
        this.status = status;
        this.channel = channel;
        this.createdAt = createdAt;
        this.handledAt = handledAt;
        this.escalationLevel = escalationLevel;
        this.familyId = familyId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
    public Integer getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(Integer escalationLevel) { this.escalationLevel = escalationLevel; }
    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
}

