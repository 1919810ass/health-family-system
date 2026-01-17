package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.FamilyService;
import com.healthfamily.web.dto.FamilyCreateRequest;
import com.healthfamily.web.dto.FamilyInviteRequest;
import com.healthfamily.web.dto.FamilyMemberResponse;
import com.healthfamily.web.dto.FamilyMemberUpdateRequest;
import com.healthfamily.web.dto.FamilyResponse;
import com.healthfamily.web.dto.FamilyUpdateRequest;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/families")
@Slf4j
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    public Result<FamilyResponse> createFamily(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody FamilyCreateRequest request) {
        return Result.success(familyService.createFamily(principal.getUserId(), request));
    }

    @PostMapping("/{id}/invite")
    public Result<FamilyResponse> joinFamily(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable("id") Long familyId,
                                             @Valid @RequestBody FamilyInviteRequest request) {
        return Result.success(familyService.joinFamily(principal.getUserId(), familyId, request));
    }

    @PostMapping("/join")
    public Result<FamilyResponse> joinByCode(@AuthenticationPrincipal UserPrincipal principal,
                                             @Valid @RequestBody FamilyInviteRequest request) {
        return Result.success(familyService.joinByCode(principal.getUserId(), request.inviteCode()));
    }

    @PostMapping("/{id}/invite/regenerate")
    public Result<FamilyResponse> regenerateInvite(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable("id") Long familyId) {
        return Result.success(familyService.regenerateInviteCode(principal.getUserId(), familyId));
    }

    @GetMapping
    public Result<List<FamilyResponse>> listFamilies(@AuthenticationPrincipal UserPrincipal principal) {
        log.info("Request listFamilies for user: {}", principal.getUserId());
        return Result.success(familyService.listUserFamilies(principal.getUserId()));
    }

    @GetMapping("/{id}")
    public Result<FamilyResponse> getFamily(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable("id") Long familyId) {
        return Result.success(familyService.getFamily(principal.getUserId(), familyId));
    }

    @GetMapping("/{id}/members")
    public Result<List<FamilyMemberResponse>> listMembers(@AuthenticationPrincipal UserPrincipal principal,
                                                          @PathVariable("id") Long familyId) {
        try {
            return Result.success(familyService.listMembers(principal.getUserId(), familyId));
        } catch (Exception e) {
            log.error("获取家庭成员列表失败: familyId={}, userId={}", familyId, principal.getUserId(), e);
            return Result.error(500, "获取家庭成员列表失败");
        }
    }

    @PutMapping("/{id}")
    public Result<FamilyResponse> updateFamily(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable("id") Long familyId,
                                               @Valid @RequestBody FamilyUpdateRequest request) {
        return Result.success(familyService.updateFamily(principal.getUserId(), familyId, request));
    }

    @PutMapping("/{id}/members/{memberId}")
    public Result<FamilyMemberResponse> updateMember(@AuthenticationPrincipal UserPrincipal principal,
                                                      @PathVariable("id") Long familyId,
                                                      @PathVariable("memberId") Long memberId,
                                                      @Valid @RequestBody FamilyMemberUpdateRequest request) {
        return Result.success(familyService.updateMember(principal.getUserId(), familyId, memberId, request));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public Result<Void> removeMember(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable("id") Long familyId,
                                     @PathVariable("memberId") Long memberId) {
        familyService.removeMember(principal.getUserId(), familyId, memberId);
        return Result.success(null);
    }

    @PostMapping("/{id}/share")
    public Result<Void> setDataShare(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable("id") Long familyId,
                                     @RequestParam("shareToFamily") Boolean shareToFamily) {
        familyService.setDataShare(principal.getUserId(), familyId, shareToFamily);
        return Result.success(null);
    }
}

