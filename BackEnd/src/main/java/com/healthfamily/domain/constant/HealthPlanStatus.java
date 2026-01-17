package com.healthfamily.domain.constant;

/**
 * 健康计划状态
 */
public enum HealthPlanStatus {
    /**
     * 进行中
     */
    ACTIVE,
    
    /**
     * 已完成
     */
    COMPLETED,
    
    /**
     * 逾期
     */
    OVERDUE,
    
    /**
     * 已取消
     */
    CANCELLED,
    
    /**
     * 已暂停
     */
    PAUSED
}

