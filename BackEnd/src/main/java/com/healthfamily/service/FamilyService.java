package com.healthfamily.service;

import com.healthfamily.domain.entity.Family;
import com.healthfamily.web.dto.FamilyCreateRequest;
import com.healthfamily.web.dto.FamilyInviteRequest;
import com.healthfamily.web.dto.FamilyMemberResponse;
import com.healthfamily.web.dto.FamilyMemberUpdateRequest;
import com.healthfamily.web.dto.FamilyResponse;
import com.healthfamily.web.dto.FamilyUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface FamilyService {

    FamilyResponse createFamily(Long userId, FamilyCreateRequest request);

    FamilyResponse joinFamily(Long userId, Long familyId, FamilyInviteRequest request);

    FamilyResponse joinByCode(Long userId, String inviteCode);

    FamilyResponse regenerateInviteCode(Long userId, Long familyId);

    FamilyResponse getFamily(Long userId, Long familyId);

    List<FamilyResponse> listUserFamilies(Long userId);

    FamilyResponse updateFamily(Long userId, Long familyId, FamilyUpdateRequest request);

    List<FamilyMemberResponse> listMembers(Long userId, Long familyId);

    FamilyMemberResponse updateMember(Long userId, Long familyId, Long memberId, FamilyMemberUpdateRequest request);

    void removeMember(Long userId, Long familyId, Long memberId);

    void setDataShare(Long userId, Long familyId, Boolean shareToFamily);
    
    // 管理员功能相关方法
    Map<String, Object> getAdminFamilyList(String keyword, String status, int page, int size, LocalDateTime startTime, LocalDateTime endTime);
    Family findById(Long id);
    Family create(Family family);
    Family update(Long id, Family family);
    boolean deleteById(Long id);
    boolean updateStatus(Long id, String status);
}

