package com.healthfamily.service;

import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.web.dto.HealthLogRequest;
import com.healthfamily.web.dto.HealthLogResponse;
import com.healthfamily.web.dto.HealthLogStatisticsResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HealthLogService {

    HealthLogResponse createLog(Long userId, HealthLogRequest request);

    HealthLogResponse updateLog(Long userId, Long logId, HealthLogRequest request);

    void deleteLog(Long userId, Long logId);

    List<HealthLogResponse> listLogs(Long userId, HealthLogType type, LocalDate startDate, LocalDate endDate);

    HealthLogStatisticsResponse getStatistics(Long userId);

    List<HealthLogResponse> getAbnormalLogs(Long userId);
    
    // 管理员功能相关方法
    Map<String, Object> getAdminHealthLogList(Long userId, String logType, String keyword, int page, int size, LocalDateTime startTime, LocalDateTime endTime);
    Map<String, Object> getAdminStatistics();
    HealthLog findById(Long id);
    HealthLog create(HealthLog healthLog);
    HealthLog update(Long id, HealthLog healthLog);
    boolean deleteById(Long id);
}

