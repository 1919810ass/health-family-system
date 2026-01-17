package com.healthfamily.service;

import com.healthfamily.web.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface DoctorMonitoringService {
    EnhancedMonitoringDataResponse getEnhancedMonitoringData(Long doctorId, Long familyId);
    
    void handleAlert(Long doctorId, Long alertId, HandleAlertRequest request);
    
    void sendPatientNotification(Long doctorId, SendNotificationRequest request);
    
    List<HandlingRecordResponse> getHandlingHistory(Long doctorId, Long familyId, Long userId);
    
    void batchHandleAlerts(Long doctorId, BatchHandleRequest request);
}