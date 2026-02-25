/**
 * 报告Status
 * <p>
 * 定义系统内的枚举/常量，用于状态机、权限控制或前后端协议对齐。
 * </p>
 */
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
