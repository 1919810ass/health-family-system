package com.healthfamily.service;

import com.healthfamily.web.dto.*;

import java.time.LocalDate;
/**
 * 医生监测服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.List;

public interface DoctorMonitoringService {
    EnhancedMonitoringDataResponse getEnhancedMonitoringData(Long doctorId, Long familyId);
    
    void handleAlert(Long doctorId, Long alertId, HandleAlertRequest request);
    
    void sendPatientNotification(Long doctorId, SendNotificationRequest request);
    
    List<HandlingRecordResponse> getHandlingHistory(Long doctorId, Long familyId, Long userId);
    
    void batchHandleAlerts(Long doctorId, BatchHandleRequest request);
}