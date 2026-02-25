package com.healthfamily.service;

import com.healthfamily.web.dto.AlertResponse;
import com.healthfamily.web.dto.TelemetryIngestRequest;
import com.healthfamily.web.dto.ThresholdResponse;
/**
 * 监测服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.List;

public interface MonitoringService {
    AlertResponse ingest(Long requesterId, TelemetryIngestRequest request);
    List<AlertResponse> getAlerts(Long requesterId, Long familyId);
    AlertResponse acknowledge(Long requesterId, Long alertId);
    List<ThresholdResponse> getThresholds(Long requesterId);
    List<ThresholdResponse> optimizeThresholds(Long requesterId);
}