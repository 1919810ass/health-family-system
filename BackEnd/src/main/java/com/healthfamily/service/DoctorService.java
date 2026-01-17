package com.healthfamily.service;

import com.healthfamily.domain.entity.User;
import com.healthfamily.web.dto.FamilyDoctorResponse;
import com.healthfamily.web.dto.DoctorViewResponse;
import com.healthfamily.web.dto.DoctorNoteRequest;
import com.healthfamily.web.dto.DoctorNoteResponse;
import com.healthfamily.web.dto.MonitoringDataResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DoctorService {
    FamilyDoctorResponse bindDoctor(Long requesterId, Long familyId, Long doctorUserId);
    FamilyDoctorResponse getFamilyDoctor(Long requesterId, Long familyId);
    DoctorViewResponse getDoctorView(Long requesterId, Long familyId, Boolean useAi);
    void unbindDoctor(Long requesterId, Long familyId);
    java.util.List<com.healthfamily.web.dto.FamilyMemberResponse> listBoundMembers(Long requesterId, Long familyId);
    com.healthfamily.web.dto.PatientDetailResponse getPatientDetail(Long doctorId, Long familyId, Long patientUserId);
    void updatePatientTags(Long doctorId, Long familyId, Long patientUserId, java.util.List<String> tags);

    com.healthfamily.web.dto.HealthPlanGenerationResponse generateAiHealthPlan(Long doctorId, Long familyId, com.healthfamily.web.dto.HealthPlanGenerationRequest request);

    /**
     * 批量生成家庭成员健康计划（针对高风险成员）
     */
    java.util.List<com.healthfamily.web.dto.BatchHealthPlanResponse> batchGenerateAiHealthPlans(Long doctorId, Long familyId);

    void togglePatientImportant(Long doctorId, Long familyId, Long patientUserId, Boolean isImportant);
    
    // 病历记录相关方法
    List<DoctorNoteResponse> listDoctorNotes(Long doctorId, Long familyId, Long patientUserId);
    DoctorNoteResponse getDoctorNote(Long doctorId, Long noteId);
    DoctorNoteResponse createDoctorNote(Long doctorId, Long familyId, Long patientUserId, DoctorNoteRequest request);
    DoctorNoteResponse updateDoctorNote(Long doctorId, Long noteId, DoctorNoteRequest request);
    void deleteDoctorNote(Long doctorId, Long noteId);
    
    // 健康监测相关方法
    MonitoringDataResponse getMonitoringData(Long doctorId, Long familyId);
    void markAlertAsHandled(Long doctorId, Long alertId);
    
    // 健康计划相关方法
    java.util.List<com.healthfamily.web.dto.HealthPlanResponse> listHealthPlans(Long doctorId, Long familyId, Long patientUserId, String status, String type);
    com.healthfamily.web.dto.HealthPlanResponse getHealthPlan(Long doctorId, Long planId);
    com.healthfamily.web.dto.HealthPlanResponse createHealthPlan(Long doctorId, Long familyId, com.healthfamily.web.dto.HealthPlanRequest request);
    com.healthfamily.web.dto.HealthPlanResponse updateHealthPlan(Long doctorId, Long planId, com.healthfamily.web.dto.HealthPlanRequest request);
    void deleteHealthPlan(Long doctorId, Long planId);
    java.util.List<com.healthfamily.web.dto.HealthPlanResponse> getHealthPlansCalendar(Long doctorId, Long patientUserId, String startDate, String endDate);
    
    // 随访任务相关方法
    java.util.List<com.healthfamily.web.dto.FollowUpTaskResponse> listFollowUpTasks(Long doctorId, Long familyId, Long patientUserId, String status);
    com.healthfamily.web.dto.FollowUpTaskResponse createFollowUpTask(Long doctorId, Long familyId, Long patientUserId, com.healthfamily.web.dto.CreateFollowUpTaskRequest request);
    com.healthfamily.web.dto.FollowUpTaskResponse updateFollowUpTask(Long doctorId, Long taskId, com.healthfamily.web.dto.UpdateFollowUpTaskRequest request);
    void deleteFollowUpTask(Long doctorId, Long taskId);
    
    // 数据统计相关方法
    com.healthfamily.web.dto.DoctorStatsResponse getStatistics(Long doctorId, Long familyId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    
    // 医生设置相关方法
    com.healthfamily.web.dto.DoctorSettingsResponse getDoctorSettings(Long doctorId);
    com.healthfamily.web.dto.DoctorSettingsResponse updateDoctorSettings(Long doctorId, com.healthfamily.web.dto.DoctorSettingsRequest request);
    
    // 管理员功能相关方法
    Map<String, Object> getAdminDoctorList(String keyword, String status, String specialty, int page, int size, LocalDateTime startTime, LocalDateTime endTime);
    com.healthfamily.web.dto.AdminDoctorDto getAdminDoctorById(Long id);
    Map<String, Object> getAdminDoctorStats();
    java.util.List<com.healthfamily.web.dto.AdminDoctorDto> getPendingDoctors(int page, int size);
    boolean approveDoctor(Long doctorId, Long adminId);
    boolean rejectDoctor(Long doctorId, Long adminId, String rejectReason);
    boolean updateDoctorCertification(Long id, Boolean certified);
    User findById(Long id);
    User create(User doctor);
    User update(Long id, User doctor);
    boolean deleteById(Long id);
    boolean updateStatus(Long id, String status);
    
    // 医生注册（需要审核）
    User registerDoctor(com.healthfamily.web.dto.DoctorRegisterRequest request);
}
