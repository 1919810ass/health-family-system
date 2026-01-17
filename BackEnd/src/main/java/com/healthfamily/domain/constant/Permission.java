package com.healthfamily.domain.constant;

public enum Permission {
    // User Management
    USER_READ,
    USER_WRITE,
    USER_DELETE,
    
    // System Config
    SYSTEM_CONFIG_READ,
    SYSTEM_CONFIG_WRITE,
    
    // Monitoring
    MONITOR_READ,
    
    // Audit Logs
    AUDIT_LOG_READ,
    
    // Family Management
    FAMILY_READ,
    FAMILY_WRITE,
    
    // Health Data
    HEALTH_DATA_READ,
    HEALTH_DATA_WRITE,
    
    // Doctor specific
    PATIENT_READ,
    PATIENT_WRITE
}
