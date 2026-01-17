package com.healthfamily.service;

import com.healthfamily.domain.constant.AuditResult;
import com.healthfamily.domain.constant.SensitivityLevel;
import com.healthfamily.domain.entity.AuditLog;
import com.healthfamily.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {
    void recordLog(User user, String action, String resource, SensitivityLevel level, AuditResult result, String ip, String userAgent, String extraJson);
    List<AuditLog> getLogs(LocalDateTime start, LocalDateTime end, AuditResult result);
    List<AuditLog> getUserLogs(Long userId);
    Page<AuditLog> searchLogs(Long userId, String action, String resource, AuditResult result, SensitivityLevel sensitivity, LocalDateTime startTime, LocalDateTime endTime, String keyword, Pageable pageable);
}
