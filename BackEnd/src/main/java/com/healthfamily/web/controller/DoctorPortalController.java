package com.healthfamily.web.controller;

import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.entity.FamilyDoctor;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.FamilyDoctorRepository;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.RecommendationService;
import com.healthfamily.web.dto.DoctorFamilyDto;
import com.healthfamily.web.dto.DoctorNoteRequest;
import com.healthfamily.web.dto.DoctorNoteResponse;
import com.healthfamily.web.dto.RecommendationGenerateRequest;
import com.healthfamily.web.dto.RecommendationGenerateResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.healthfamily.web.dto.FollowUpTaskResponse;
import com.healthfamily.web.dto.CreateFollowUpTaskRequest;
import com.healthfamily.web.dto.UpdateFollowUpTaskRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctor")
public class DoctorPortalController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DoctorPortalController.class);

    private final FamilyDoctorRepository familyDoctorRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final com.healthfamily.service.DoctorService doctorService;
    private final RecommendationService recommendationService;

    @GetMapping("/families")
    public Result<List<DoctorFamilyDto>> myFamilies(@AuthenticationPrincipal UserPrincipal principal) {
        Long doctorId = principal.getUserId();
        List<FamilyDoctor> list = familyDoctorRepository.findByDoctor_Id(doctorId);
        List<DoctorFamilyDto> dtos = list.stream()
                .map(fd -> new DoctorFamilyDto(fd.getFamily().getId(), fd.getFamily().getName()))
                .collect(Collectors.toList());
        return Result.success(dtos);
    }

    @GetMapping("/families/{id}/members")
    public Result<java.util.List<com.healthfamily.web.dto.FamilyMemberResponse>> members(
            @AuthenticationPrincipal UserPrincipal principal,
            @org.springframework.web.bind.annotation.PathVariable("id") Long familyId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.listBoundMembers(doctorId, familyId));
        } catch (Exception e) {
            log.error("获取家庭成员列表失败: familyId={}, doctorId={}", familyId, doctorId, e);
            return Result.error(500, "获取家庭成员列表失败");
        }
    }

    @GetMapping("/families/{familyId}/patients/{patientUserId}/detail")
    public Result<com.healthfamily.web.dto.PatientDetailResponse> getPatientDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @org.springframework.web.bind.annotation.PathVariable("familyId") Long familyId,
            @org.springframework.web.bind.annotation.PathVariable("patientUserId") Long patientUserId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.getPatientDetail(doctorId, familyId, patientUserId));
        } catch (Exception e) {
            log.error("获取患者详情失败: familyId={}, patientUserId={}, doctorId={}", familyId, patientUserId, doctorId, e);
            return Result.error(500, "获取患者详情失败");
        }
    }

    @PostMapping("/families/{familyId}/patients/{patientUserId}/toggle-important")
    public Result<Void> togglePatientImportant(
            @AuthenticationPrincipal UserPrincipal principal,
            @org.springframework.web.bind.annotation.PathVariable("familyId") Long familyId,
            @org.springframework.web.bind.annotation.PathVariable("patientUserId") Long patientUserId,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Boolean> request) {
        Long doctorId = principal.getUserId();
        Boolean isImportant = request.getOrDefault("isImportant", false);
        try {
            doctorService.togglePatientImportant(doctorId, familyId, patientUserId, isImportant);
            return Result.success();
        } catch (Exception e) {
            log.error("切换重点管理标记失败: familyId={}, patientUserId={}, doctorId={}", familyId, patientUserId, doctorId, e);
            return Result.error(500, "操作失败");
        }
    }

    @PostMapping("/families/{familyId}/patients/{patientUserId}/tags")
    public Result<Void> updatePatientTags(
            @AuthenticationPrincipal UserPrincipal principal,
            @org.springframework.web.bind.annotation.PathVariable("familyId") Long familyId,
            @org.springframework.web.bind.annotation.PathVariable("patientUserId") Long patientUserId,
            @org.springframework.web.bind.annotation.RequestBody java.util.List<String> tags) {
        Long doctorId = principal.getUserId();
        try {
            doctorService.updatePatientTags(doctorId, familyId, patientUserId, tags);
            return Result.success();
        } catch (Exception e) {
            log.error("更新患者标签失败: familyId={}, patientUserId={}, doctorId={}", familyId, patientUserId, doctorId, e);
            return Result.error(500, "操作失败");
        }
    }

    // ==================== 病历记录相关API ====================

    /**
     * 获取患者的所有病历记录
     */
    @GetMapping("/families/{familyId}/members/{memberId}/notes")
    public Result<List<DoctorNoteResponse>> listDoctorNotes(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @PathVariable("memberId") Long memberId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.listDoctorNotes(doctorId, familyId, memberId));
        } catch (Exception e) {
            log.error("获取病历记录列表失败: familyId={}, memberId={}, doctorId={}", familyId, memberId, doctorId, e);
            return Result.error(500, "获取病历记录列表失败");
        }
    }

    /**
     * 获取单个病历记录详情
     */
    @GetMapping("/notes/{noteId}")
    public Result<DoctorNoteResponse> getDoctorNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("noteId") Long noteId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.getDoctorNote(doctorId, noteId));
        } catch (Exception e) {
            log.error("获取病历记录详情失败: noteId={}, doctorId={}", noteId, doctorId, e);
            return Result.error(500, "获取病历记录详情失败");
        }
    }

    /**
     * 创建病历记录
     */
    @PostMapping("/families/{familyId}/members/{memberId}/notes")
    public Result<DoctorNoteResponse> createDoctorNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @PathVariable("memberId") Long memberId,
            @Valid @RequestBody DoctorNoteRequest request) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.createDoctorNote(doctorId, familyId, memberId, request));
        } catch (Exception e) {
            log.error("创建病历记录失败: familyId={}, memberId={}, doctorId={}", familyId, memberId, doctorId, e);
            return Result.error(500, "创建病历记录失败");
        }
    }

    /**
     * 更新病历记录
     */
    @PutMapping("/notes/{noteId}")
    public Result<DoctorNoteResponse> updateDoctorNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("noteId") Long noteId,
            @Valid @RequestBody DoctorNoteRequest request) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.updateDoctorNote(doctorId, noteId, request));
        } catch (Exception e) {
            log.error("更新病历记录失败: noteId={}, doctorId={}", noteId, doctorId, e);
            return Result.error(500, "更新病历记录失败");
        }
    }

    /**
     * 删除病历记录
     */
    @DeleteMapping("/notes/{noteId}")
    public Result<Void> deleteDoctorNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("noteId") Long noteId) {
        Long doctorId = principal.getUserId();
        try {
            doctorService.deleteDoctorNote(doctorId, noteId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除病历记录失败: noteId={}, doctorId={}", noteId, doctorId, e);
            return Result.error(500, "删除病历记录失败");
        }
    }

    // ==================== 诊断工具相关API ====================

    /**
     * 医生代为患者生成健康建议
     * 基于患者的体质测评和近期日志数据
     */
    @PostMapping("/families/{familyId}/members/{memberId}/recommendations/generate")
    public Result<RecommendationGenerateResponse> generateRecommendationForPatient(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @PathVariable("memberId") Long memberId,
            @Valid @RequestBody RecommendationGenerateRequest request) {
        Long doctorId = principal.getUserId();
        try {
            // 验证医生权限
            com.healthfamily.domain.entity.Family family = familyRepository.findById(familyId)
                    .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
            boolean isDoctor = familyDoctorRepository.findByFamily(family).stream()
                    .anyMatch(fd -> java.util.Objects.equals(fd.getDoctor().getId(), doctorId));
            if (!isDoctor) {
                throw new BusinessException(40301, "无权访问该家庭");
            }
            
            // 验证患者是否在该家庭中
            User patient = userRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
            familyMemberRepository.findByFamilyAndUser(family, patient)
                    .orElseThrow(() -> new BusinessException(40404, "该用户不是家庭成员"));
            
            // 代为患者生成建议（使用患者的ID）
            return Result.success(recommendationService.generate(memberId, request));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("代为患者生成建议失败: familyId={}, memberId={}, doctorId={}", familyId, memberId, doctorId, e);
            return Result.error(500, "生成建议失败");
        }
    }

    // ==================== 随访任务相关API ====================
    
    @GetMapping("/families/{familyId}/members/{memberId}/followups")
    public Result<List<FollowUpTaskResponse>> listFollowUpTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @PathVariable("memberId") Long memberId,
            @RequestParam(value = "status", required = false) String status) {
        Long doctorId = principal.getUserId();
        return Result.success(doctorService.listFollowUpTasks(doctorId, familyId, memberId, status));
    }

    @GetMapping("/families/{familyId}/followups")
    public Result<List<FollowUpTaskResponse>> listFamilyFollowUpTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @RequestParam(value = "status", required = false) String status) {
        Long doctorId = principal.getUserId();
        // memberId 传 null
        return Result.success(doctorService.listFollowUpTasks(doctorId, familyId, null, status));
    }

    @PostMapping("/families/{familyId}/members/{memberId}/followups")
    public Result<FollowUpTaskResponse> createFollowUpTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @PathVariable("memberId") Long memberId,
            @Valid @RequestBody CreateFollowUpTaskRequest request) {
        Long doctorId = principal.getUserId();
        return Result.success(doctorService.createFollowUpTask(doctorId, familyId, memberId, request));
    }

    @PutMapping("/followups/{taskId}")
    public Result<FollowUpTaskResponse> updateFollowUpTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("taskId") Long taskId,
            @Valid @RequestBody UpdateFollowUpTaskRequest request) {
        Long doctorId = principal.getUserId();
        return Result.success(doctorService.updateFollowUpTask(doctorId, taskId, request));
    }

    @DeleteMapping("/followups/{taskId}")
    public Result<Void> deleteFollowUpTask(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("taskId") Long taskId) {
        Long doctorId = principal.getUserId();
        doctorService.deleteFollowUpTask(doctorId, taskId);
        return Result.success();
    }

    // ==================== 健康监测相关API ====================

    /**
     * 获取健康监测数据（旧接口，已弃用，请使用新接口）
     * GET /api/doctor/monitoring/legacy?familyId={familyId}
     */
    @GetMapping("/monitoring/legacy")
    @Deprecated
    public Result<com.healthfamily.web.dto.MonitoringDataResponse> getMonitoringData(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("familyId") Long familyId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.getMonitoringData(doctorId, familyId));
        } catch (Exception e) {
            log.error("获取监测数据失败: familyId={}, doctorId={}", familyId, doctorId, e);
            return Result.error(500, "获取监测数据失败");
        }
    }

    /**
     * 标记预警为已处理（旧接口，已弃用，请使用新接口）
     * POST /api/doctor/monitoring/alerts/{alertId}/handle-legacy
     */
    @PostMapping("/monitoring/alerts/{alertId}/handle-legacy")
    @Deprecated
    public Result<Void> markAlertAsHandled(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("alertId") Long alertId) {
        Long doctorId = principal.getUserId();
        try {
            doctorService.markAlertAsHandled(doctorId, alertId);
            return Result.success();
        } catch (Exception e) {
            log.error("标记预警已处理失败: alertId={}, doctorId={}", alertId, doctorId, e);
            return Result.error(500, "操作失败");
        }
    }

    // ==================== 健康计划相关API ====================

    /**
     * 获取健康计划列表 (指定家庭和成员)
     * GET /api/doctor/families/{familyId}/members/{memberId}/plans?status=ACTIVE&type=BLOOD_PRESSURE_FOLLOWUP
     */
    @GetMapping("/families/{familyId}/members/{memberId}/plans")
    public Result<java.util.List<com.healthfamily.web.dto.HealthPlanResponse>> listHealthPlans(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @PathVariable("memberId") Long memberId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.listHealthPlans(doctorId, familyId, memberId, status, type));
        } catch (Exception e) {
            log.error("获取健康计划列表失败: familyId={}, memberId={}, doctorId={}", familyId, memberId, doctorId, e);
            return Result.error(500, "获取健康计划列表失败");
        }
    }

    /**
     * 获取健康计划列表 (指定家庭的所有成员)
     * GET /api/doctor/families/{familyId}/plans?status=ACTIVE&type=BLOOD_PRESSURE_FOLLOWUP
     */
    @GetMapping("/families/{familyId}/plans")
    public Result<java.util.List<com.healthfamily.web.dto.HealthPlanResponse>> listFamilyHealthPlans(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type) {
        Long doctorId = principal.getUserId();
        try {
            // memberId 传 null 表示查询家庭下所有计划
            return Result.success(doctorService.listHealthPlans(doctorId, familyId, null, status, type));
        } catch (Exception e) {
            log.error("获取家庭健康计划列表失败: familyId={}, doctorId={}", familyId, doctorId, e);
            return Result.error(500, "获取健康计划列表失败");
        }
    }

    /**
     * 获取单个健康计划详情
     * GET /api/doctor/plans/{planId}
     */
    @GetMapping("/plans/{planId}")
    public Result<com.healthfamily.web.dto.HealthPlanResponse> getHealthPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("planId") Long planId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.getHealthPlan(doctorId, planId));
        } catch (Exception e) {
            log.error("获取健康计划详情失败: planId={}, doctorId={}", planId, doctorId, e);
            return Result.error(500, "获取健康计划详情失败");
        }
    }

    /**
     * AI自动生成健康计划
     * POST /api/doctor/families/{familyId}/plans/ai-generate
     */
    @PostMapping("/families/{familyId}/plans/ai-generate")
    public Result<com.healthfamily.web.dto.HealthPlanGenerationResponse> generateAiHealthPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @Valid @RequestBody com.healthfamily.web.dto.HealthPlanGenerationRequest request) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.generateAiHealthPlan(doctorId, familyId, request));
        } catch (Exception e) {
            log.error("AI生成健康计划失败: familyId={}, doctorId={}", familyId, doctorId, e);
            return Result.error(500, "AI生成健康计划失败");
        }
    }

    /**
     * AI批量生成健康计划（全家高风险成员）
     * POST /api/doctor/families/{familyId}/plans/ai-batch-generate
     */
    @PostMapping("/families/{familyId}/plans/ai-batch-generate")
    public Result<java.util.List<com.healthfamily.web.dto.BatchHealthPlanResponse>> batchGenerateAiHealthPlans(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.batchGenerateAiHealthPlans(doctorId, familyId));
        } catch (Exception e) {
            log.error("AI批量生成健康计划失败: familyId={}, doctorId={}", familyId, doctorId, e);
            return Result.error(500, "AI批量生成健康计划失败");
        }
    }

    /**
     * 创建健康计划
     * POST /api/doctor/families/{familyId}/members/{memberId}/plans
     */
    @PostMapping("/families/{familyId}/members/{memberId}/plans")
    public Result<com.healthfamily.web.dto.HealthPlanResponse> createHealthPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("familyId") Long familyId,
            @PathVariable("memberId") Long memberId,
            @Valid @RequestBody com.healthfamily.web.dto.HealthPlanRequest request) {
        Long doctorId = principal.getUserId();
        try {
            // 确保 memberId 与 request 中的 patientUserId 一致
            if (!memberId.equals(request.patientUserId())) {
                return Result.error(400, "路径参数中的memberId与请求体中的patientUserId不一致");
            }
            return Result.success(doctorService.createHealthPlan(doctorId, familyId, request));
        } catch (Exception e) {
            log.error("创建健康计划失败: familyId={}, memberId={}, doctorId={}", familyId, memberId, doctorId, e);
            return Result.error(500, "创建健康计划失败");
        }
    }

    /**
     * 更新健康计划
     * PUT /api/doctor/plans/{planId}
     */
    @PutMapping("/plans/{planId}")
    public Result<com.healthfamily.web.dto.HealthPlanResponse> updateHealthPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("planId") Long planId,
            @Valid @RequestBody com.healthfamily.web.dto.HealthPlanRequest request) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.updateHealthPlan(doctorId, planId, request));
        } catch (Exception e) {
            log.error("更新健康计划失败: planId={}, doctorId={}", planId, doctorId, e);
            return Result.error(500, "更新健康计划失败");
        }
    }

    /**
     * 删除健康计划
     * DELETE /api/doctor/plans/{planId}
     */
    @DeleteMapping("/plans/{planId}")
    public Result<Void> deleteHealthPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("planId") Long planId) {
        Long doctorId = principal.getUserId();
        try {
            doctorService.deleteHealthPlan(doctorId, planId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除健康计划失败: planId={}, doctorId={}", planId, doctorId, e);
            return Result.error(500, "删除健康计划失败");
        }
    }

    /**
     * 获取健康计划日历视图
     * GET /api/doctor/members/{memberId}/plans/calendar?startDate=2025-01-01&endDate=2025-01-31
     */
    @GetMapping("/members/{memberId}/plans/calendar")
    public Result<java.util.List<com.healthfamily.web.dto.HealthPlanResponse>> getHealthPlansCalendar(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("memberId") Long memberId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.getHealthPlansCalendar(doctorId, memberId, startDate, endDate));
        } catch (Exception e) {
            log.error("获取健康计划日历失败: memberId={}, doctorId={}", memberId, doctorId, e);
            return Result.error(500, "获取健康计划日历失败");
        }
    }

    // ==================== 数据统计相关API ====================

    /**
     * 获取统计数据
     * GET /api/doctor/stats?familyId={familyId}&startDate=2025-01-01&endDate=2025-01-31
     */
    @GetMapping("/stats")
    public Result<com.healthfamily.web.dto.DoctorStatsResponse> getStatistics(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("familyId") Long familyId,
            @RequestParam(value = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        Long doctorId = principal.getUserId();
        try {
            // 如果没有指定日期范围，默认使用最近30天
            if (startDate == null || endDate == null) {
                java.time.LocalDate today = java.time.LocalDate.now();
                endDate = today;
                startDate = today.minusDays(29);
            }
            return Result.success(doctorService.getStatistics(doctorId, familyId, startDate, endDate));
        } catch (Exception e) {
            log.error("获取统计数据失败: familyId={}, doctorId={}", familyId, doctorId, e);
            return Result.error(500, "获取统计数据失败");
        }
    }

    // ==================== 医生设置相关API ====================

    /**
     * 获取医生设置
     * GET /api/doctor/settings
     */
    @GetMapping("/settings")
    public Result<com.healthfamily.web.dto.DoctorSettingsResponse> getDoctorSettings(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.getDoctorSettings(doctorId));
        } catch (Exception e) {
            log.error("获取医生设置失败: doctorId={}", doctorId, e);
            return Result.error(500, "获取医生设置失败");
        }
    }

    /**
     * 更新医生设置
     * PUT /api/doctor/settings
     */
    @PutMapping("/settings")
    public Result<com.healthfamily.web.dto.DoctorSettingsResponse> updateDoctorSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody com.healthfamily.web.dto.DoctorSettingsRequest request) {
        Long doctorId = principal.getUserId();
        try {
            return Result.success(doctorService.updateDoctorSettings(doctorId, request));
        } catch (Exception e) {
            log.error("更新医生设置失败: doctorId={}", doctorId, e);
            return Result.error(500, "更新医生设置失败");
        }
    }
}
