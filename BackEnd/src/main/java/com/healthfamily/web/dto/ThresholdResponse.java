package com.healthfamily.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThresholdResponse {
    private String metric;
    private Double lowerBound;
    private Double upperBound;

    // RequiredArgsConstructor for backwards compatibility with record usage
    public ThresholdResponse(String metric, Double lowerBound, Double upperBound) {
        this.metric = metric;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    // Getters
    public String getMetric() { return metric; }
    public Double getLowerBound() { return lowerBound; }
    public Double getUpperBound() { return upperBound; }

    // Setters
    public void setMetric(String metric) { this.metric = metric; }
    public void setLowerBound(Double lowerBound) { this.lowerBound = lowerBound; }
    public void setUpperBound(Double upperBound) { this.upperBound = upperBound; }
}

