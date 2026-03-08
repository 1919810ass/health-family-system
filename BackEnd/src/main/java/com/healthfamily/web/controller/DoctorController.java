package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.DoctorService;
import com.healthfamily.web.dto.DoctorViewResponse;
import com.healthfamily.web.dto.FamilyDoctorBindRequest;
import com.healthfamily.web.dto.FamilyDoctorResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
/**
 * 医生控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequestMapping("/api/families")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/{id}/doctor/bind")
    public Result<FamilyDoctorResponse> bind(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable("id") Long familyId,
                                             @Valid @RequestBody FamilyDoctorBindRequest request) {
        Long userId = principal.getUserId();
        return Result.success(doctorService.bindDoctor(userId, familyId, request.doctorUserId()));
    }

    @GetMapping("/{id}/doctor")
    public Result<FamilyDoctorResponse> getDoctor(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable("id") Long familyId) {
        Long userId = principal.getUserId();
        return Result.success(doctorService.getFamilyDoctor(userId, familyId));
    }

    @DeleteMapping("/{id}/doctor")
    public Result<Void> unbind(@AuthenticationPrincipal UserPrincipal principal,
                               @PathVariable("id") Long familyId) {
        Long userId = principal.getUserId();
        doctorService.unbindDoctor(userId, familyId);
        return Result.success();
    }

    @GetMapping("/{id}/doctor-view")
    public Result<DoctorViewResponse> doctorView(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable("id") Long familyId,
                                                 @RequestParam(value = "useAi", required = false, defaultValue = "false") Boolean useAi) {
        Long userId = principal.getUserId();
        return Result.success(doctorService.getDoctorView(userId, familyId, useAi));
    }

    @PostMapping("/{id}/patients/{userId}/toggle-important")
    public Result<Void> toggleImportant(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable("id") Long familyId,
                                        @PathVariable("userId") Long patientUserId,
                                        @RequestBody java.util.Map<String, Boolean> body) {
        Long doctorId = principal.getUserId();
        doctorService.togglePatientImportant(doctorId, familyId, patientUserId, body.get("isImportant"));
        return Result.success();
    }

    @PostMapping("/{id}/patients/{userId}/tags")
    public Result<Void> updateTags(@AuthenticationPrincipal UserPrincipal principal,
                                   @PathVariable("id") Long familyId,
                                   @PathVariable("userId") Long patientUserId,
                                   @RequestBody java.util.List<String> tags) {
        Long doctorId = principal.getUserId();
        doctorService.updatePatientTags(doctorId, familyId, patientUserId, tags);
        return Result.success();
    }

    @PostMapping("/rate/{doctorId}")
    public Result<Void> rateDoctor(@AuthenticationPrincipal UserPrincipal principal,
                                   @PathVariable("doctorId") Long doctorId,
                                   @RequestBody java.util.Map<String, Object> body) {
        Long userId = principal.getUserId();
        Integer rating = (Integer) body.get("rating");
        String comment = (String) body.get("comment");
        doctorService.rateDoctor(userId, doctorId, rating, comment);
        return Result.success();
    }
    
    @GetMapping("/doctor/ratings")
    /**
     * 获取
     * @param principal 当前登录用户
     * @return 业务返回结果
     */
    public Result<java.util.List<com.healthfamily.web.dto.DoctorRatingResponse>> getMyRatings(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(doctorService.getDoctorRatings(principal.getUserId()));
    }

    @PostMapping("/doctor/ratings/{ratingId}/reply")
    public Result<Void> replyRating(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable("ratingId") Long ratingId,
                                    @RequestBody java.util.Map<String, String> body) {
        String reply = body.get("reply");
        doctorService.replyDoctorRating(principal.getUserId(), ratingId, reply);
        return Result.success();
    }

    // 病历记录相关API

    /**
     * 删除健康计划
     *
     * @param principal 当前登录用户
     * @param id        健康计划ID
     * @return 业务返回结果
     */
    @DeleteMapping("/health-plans/{id}")
    public Result<Void> deleteHealthPlan(@AuthenticationPrincipal UserPrincipal principal,
                                         @PathVariable("id") Long id) {
        doctorService.deleteHealthPlan(principal.getUserId(), id);
        return Result.success();
    }

    /**
     * 批量删除健康计划
     *
     * @param principal 当前登录用户
     * @param ids       健康计划ID列表
     * @return 业务返回结果
     */
    @DeleteMapping("/health-plans/batch")
    public Result<Void> batchDeleteHealthPlans(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestBody java.util.List<Long> ids) {
        doctorService.batchDeleteHealthPlans(principal.getUserId(), ids);
        return Result.success();
    }

    // 随访任务相关API

    /**
     * 删除随访任务
     *
     * @param principal 当前登录用户
     * @param id        随访任务ID
     * @return 业务返回结果
     */
    @DeleteMapping("/follow-up-tasks/{id}")
    public Result<Void> deleteFollowUpTask(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable("id") Long id) {
        doctorService.deleteFollowUpTask(principal.getUserId(), id);
        return Result.success();
    }

    /**
     * 批量删除随访任务
     *
     * @param principal 当前登录用户
     * @param ids       随访任务ID列表
     * @return 业务返回结果
     */
    @DeleteMapping("/follow-up-tasks/batch")
    public Result<Void> batchDeleteFollowUpTasks(@AuthenticationPrincipal UserPrincipal principal,
                                                 @RequestBody java.util.List<Long> ids) {
        doctorService.batchDeleteFollowUpTasks(principal.getUserId(), ids);
        return Result.success();
    }
}
