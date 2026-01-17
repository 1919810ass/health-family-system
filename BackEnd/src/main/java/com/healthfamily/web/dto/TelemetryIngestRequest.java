package com.healthfamily.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TelemetryIngestRequest {
    private Long userId;
    private Long familyId;
    private String metric;
    private Double value;
    private LocalDateTime timestamp;
    private String source;

    // RequiredArgsConstructor for backwards compatibility with record usage
    public TelemetryIngestRequest(Long userId, Long familyId, String metric, Double value, LocalDateTime timestamp, String source) {
        this.userId = userId;
        this.familyId = familyId;
        this.metric = metric;
        this.value = value;
        this.timestamp = timestamp;
        this.source = source;
    }

    // Getters
    public Long getUserId() { return userId; }
    public Long getFamilyId() { return familyId; }
    public String getMetric() { return metric; }
    public Double getValue() { return value; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSource() { return source; }

    // Setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    public void setMetric(String metric) { this.metric = metric; }
    public void setValue(Double value) { this.value = value; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setSource(String source) { this.source = source; }
}

