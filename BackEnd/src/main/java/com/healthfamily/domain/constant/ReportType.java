package com.healthfamily.domain.constant;

public enum ReportType {
    LAB_REPORT("化验单"),
    EXAM_REPORT("体检报告"),
    PRESCRIPTION("处方单"),
    OTHER("其他");

    private final String description;

    ReportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
