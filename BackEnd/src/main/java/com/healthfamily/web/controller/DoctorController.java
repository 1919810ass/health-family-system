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

    // 病历记录相关API
}
