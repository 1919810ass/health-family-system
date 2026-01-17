package com.healthfamily.service;

import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.constant.UserRole;
import com.healthfamily.web.dto.ChangePasswordRequest;
import com.healthfamily.web.dto.ProfileHealthUpdateRequest;
import com.healthfamily.web.dto.UpdateNotificationsRequest;
import com.healthfamily.web.dto.UpdateProfileRequest;
import com.healthfamily.web.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    UserProfileResponse updateHealthProfile(Long userId, ProfileHealthUpdateRequest request);
    com.healthfamily.web.dto.HealthProfileResponse getHealthProfile(Long userId);
    String updateAvatar(Long userId, MultipartFile file);
    void changePassword(Long userId, ChangePasswordRequest request);
    UpdateNotificationsRequest updateNotifications(Long userId, UpdateNotificationsRequest request);
    java.util.List<com.healthfamily.web.dto.FamilyResponse> listFamilies(Long userId);
    com.healthfamily.web.dto.FamilyResponse switchCurrentFamily(Long userId, Long familyId);
    
    // 管理员功能相关方法
    Map<String, Object> getAdminUserList(String keyword, UserRole role, String status, int page, int size, LocalDateTime startTime, LocalDateTime endTime);
    User findById(Long id);
    User create(User user);
    User update(Long id, User user);
    boolean deleteById(Long id);

    boolean updateStatus(Long id, String status);

    boolean resetPassword(Long id, String password);

    boolean forceLogout(Long id);

    boolean lockUser(Long id);

    boolean unlockUser(Long id);

    boolean auditUser(Long id, boolean approve, String reason);
}
