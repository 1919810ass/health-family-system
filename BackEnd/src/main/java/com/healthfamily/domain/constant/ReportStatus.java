package com.healthfamily.domain.constant;

public enum ReportStatus {
    PENDING("待处理"),
    PROCESSING("识别中"),
    COMPLETED("已完成"),
    FAILED("识别失败");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
