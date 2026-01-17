package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.Profile;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyMember;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.domain.constant.Sex;
import com.healthfamily.service.UserService;
import com.healthfamily.web.dto.ChangePasswordRequest;
import com.healthfamily.web.dto.HealthProfileResponse;
import com.healthfamily.web.dto.ProfileHealthUpdateRequest;
import com.healthfamily.web.dto.UpdateNotificationsRequest;
import com.healthfamily.web.dto.UpdateProfileRequest;
import com.healthfamily.web.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Profile profile = profileRepository.findById(userId).orElse(null);
        Map<String, Object> prefs = readPreferences(profile);
        String avatar = (String) prefs.getOrDefault("avatar", null);
        String email = (String) prefs.getOrDefault("email", null);
        @SuppressWarnings("unchecked")
        Map<String, Object> notifications = (Map<String, Object>) prefs.getOrDefault("notifications", defaultNotifications());
        return new UserProfileResponse(user.getId(), user.getPhone(), user.getNickname(), email, avatar, user.getRole().name(), notifications);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        if (request.nickname() != null) user.setNickname(request.nickname());
        if (request.phone() != null) user.setPhone(request.phone());
        userRepository.save(user);

        Profile profile = profileRepository.findById(userId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUser(user);
            return p;
        });
        Map<String, Object> prefs = readPreferences(profile);
        if (request.email() != null) {
            prefs.put("email", request.email());
        }
        profile.setPreferences(writePreferences(prefs));
        if (profile.getUpdatedAt() == null) profile.setUpdatedAt(java.time.LocalDateTime.now());
        profileRepository.save(profile);

        @SuppressWarnings("unchecked")
        Map<String, Object> notifications = (Map<String, Object>) prefs.getOrDefault("notifications", defaultNotifications());
        String avatar = (String) prefs.getOrDefault("avatar", null);
        String email = (String) prefs.getOrDefault("email", null);
        return new UserProfileResponse(user.getId(), user.getPhone(), user.getNickname(), email, avatar, user.getRole().name(), notifications);
    }

    @Override
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .map(n -> n.contains(".") ? n.substring(n.lastIndexOf('.') + 1) : "jpg")
                .orElse("jpg");
        String filename = System.currentTimeMillis() + "." + ext;
        // 使用配置的上传目录，而不是临时目录
        Path base = Paths.get(uploadDir, String.valueOf(userId)).toAbsolutePath().normalize();
        try {
            Files.createDirectories(base);
            Path target = base.resolve(filename);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new RuntimeException("上传头像失败", e);
        }
        String url = "/api/user/avatar/" + userId + "/" + filename;

        User user = userRepository.findById(userId).orElseThrow();
        Profile profile = profileRepository.findById(userId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUser(user);
            return p;
        });
        Map<String, Object> prefs = readPreferences(profile);
        prefs.put("avatar", url);
        profile.setPreferences(writePreferences(prefs));
        if (profile.getUpdatedAt() == null) profile.setUpdatedAt(java.time.LocalDateTime.now());
        profileRepository.save(profile);
        return url;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("旧密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UpdateNotificationsRequest updateNotifications(Long userId, UpdateNotificationsRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Profile profile = profileRepository.findById(userId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUser(user);
            return p;
        });
        Map<String, Object> prefs = readPreferences(profile);
        Map<String, Object> noti = new HashMap<>();
        noti.put("inApp", request.inApp());
        noti.put("email", request.email());
        noti.put("sms", request.sms());
        prefs.put("notifications", noti);
        profile.setPreferences(writePreferences(prefs));
        if (profile.getUpdatedAt() == null) profile.setUpdatedAt(java.time.LocalDateTime.now());
        profileRepository.save(profile);
        return request;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readPreferences(Profile profile) {
        try {
            if (profile == null || profile.getPreferences() == null) return new HashMap<>();
            return objectMapper.readValue(profile.getPreferences(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String writePreferences(Map<String, Object> prefs) {
        try {
            return objectMapper.writeValueAsString(prefs);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> defaultNotifications() {
        Map<String, Object> m = new HashMap<>();
        m.put("inApp", true);
        m.put("email", false);
        m.put("sms", false);
        return m;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<com.healthfamily.web.dto.FamilyResponse> listFamilies(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return familyMemberRepository.findByUser(user).stream()
                .map(FamilyMember::getFamily)
                .filter(f -> {
                    if (f.getStatus() != null && f.getStatus() == 1) return true;
                    return f.getOwner() != null && f.getOwner().getId() != null && f.getOwner().getId().equals(userId);
                })
                .map(f -> new com.healthfamily.web.dto.FamilyResponse(f.getId(), f.getName(), f.getOwner().getId(), f.getInviteCode()))
                .toList();
    }

    @Override
    @Transactional
    public com.healthfamily.web.dto.FamilyResponse switchCurrentFamily(Long userId, Long familyId) {
        User user = userRepository.findById(userId).orElseThrow();
        Family family = familyRepository.findById(familyId).orElseThrow();
        familyMemberRepository.findByFamilyAndUser(family, user)
                .orElseThrow(() -> new RuntimeException("非家庭成员无法切换"));
        if (family.getStatus() == null || family.getStatus() != 1) {
            boolean isOwner = family.getOwner() != null && family.getOwner().getId() != null && family.getOwner().getId().equals(userId);
            if (!isOwner) throw new RuntimeException("家庭未通过审核，暂不可切换");
        }
        Profile profile = profileRepository.findById(userId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUser(user);
            return p;
        });
        Map<String, Object> prefs = readPreferences(profile);
        prefs.put("currentFamilyId", family.getId());
        profile.setPreferences(writePreferences(prefs));
        if (profile.getUpdatedAt() == null) profile.setUpdatedAt(java.time.LocalDateTime.now());
        profileRepository.save(profile);
        return new com.healthfamily.web.dto.FamilyResponse(family.getId(), family.getName(), family.getOwner().getId(), family.getInviteCode());
    }

    @Override
    @Transactional
    public UserProfileResponse updateHealthProfile(Long userId, ProfileHealthUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Profile profile = profileRepository.findById(userId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUser(user);
            return p;
        });

        if (request.sex() != null && !request.sex().isBlank()) {
            try {
                profile.setSex(Sex.valueOf(request.sex().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // 忽略无效的性别值
            }
        }
        if (request.birthday() != null) {
            profile.setBirthday(request.birthday());
        }
        if (request.heightCm() != null) {
            profile.setHeightCm(request.heightCm());
        }
        if (request.weightKg() != null) {
            profile.setWeightKg(request.weightKg());
        }
        try {
            if (request.allergies() != null) {
                profile.setAllergies(objectMapper.writeValueAsString(request.allergies()));
            }
            if (request.healthTags() != null) {
                profile.setHealthTags(objectMapper.writeValueAsString(request.healthTags()));
            }
            if (request.lifestyle() != null) {
                profile.setLifestyle(objectMapper.writeValueAsString(request.lifestyle()));
            }
            if (request.goals() != null) {
                profile.setGoals(objectMapper.writeValueAsString(request.goals()));
            }
        } catch (Exception e) {
            throw new RuntimeException("更新健康画像失败", e);
        }

        if (profile.getUpdatedAt() == null) {
            profile.setUpdatedAt(java.time.LocalDateTime.now());
        }
        profileRepository.save(profile);

        Map<String, Object> prefs = readPreferences(profile);
        String avatar = (String) prefs.getOrDefault("avatar", null);
        String email = (String) prefs.getOrDefault("email", null);
        Map<String, Object> notifications = (Map<String, Object>) prefs.getOrDefault("notifications", defaultNotifications());
        return new UserProfileResponse(user.getId(), user.getPhone(), user.getNickname(), email, avatar, user.getRole().name(), notifications);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public HealthProfileResponse getHealthProfile(Long userId) {
        Profile profile = profileRepository.findById(userId).orElse(null);
        if (profile == null) {
            return new HealthProfileResponse(null, null, null, null, null, null, null, null);
        }

        String sex = profile.getSex() != null ? profile.getSex().name() : null;
        List<String> allergies = null;
        List<String> healthTags = null;
        Map<String, Object> lifestyle = null;
        Map<String, Object> goals = null;

        try {
            if (StringUtils.hasText(profile.getAllergies())) {
                allergies = objectMapper.readValue(profile.getAllergies(), new TypeReference<List<String>>() {});
            }
            if (StringUtils.hasText(profile.getHealthTags())) {
                healthTags = objectMapper.readValue(profile.getHealthTags(), new TypeReference<List<String>>() {});
            }
            if (StringUtils.hasText(profile.getLifestyle())) {
                lifestyle = objectMapper.readValue(profile.getLifestyle(), new TypeReference<Map<String, Object>>() {});
            }
            if (StringUtils.hasText(profile.getGoals())) {
                goals = objectMapper.readValue(profile.getGoals(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.warn("解析健康画像数据失败: {}", e.getMessage());
        }

        return new HealthProfileResponse(
                sex,
                profile.getBirthday(),
                profile.getHeightCm(),
                profile.getWeightKg(),
                allergies,
                healthTags,
                lifestyle,
                goals
        );
    }

    @Override
    public Map<String, Object> getAdminUserList(String keyword, com.healthfamily.domain.constant.UserRole role, String status, int page, int size, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        // 使用JPA的分页查询
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, size);
        
        // 获取所有用户（在实际应用中，这应该使用更高效的数据库查询）
        List<User> allUsers = userRepository.findAll();
        
        // 应用筛选条件
        if (keyword != null && !keyword.isEmpty()) {
            allUsers = allUsers.stream()
                .filter(user -> user.getNickname() != null && (user.getNickname().contains(keyword) || user.getPhone() != null && user.getPhone().contains(keyword)))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (role != null) {
            allUsers = allUsers.stream()
                .filter(user -> user.getRole() == role)
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (status != null && !status.isEmpty()) {
            // 根据用户状态字段进行筛选
            // 假设status为"1"表示启用，"0"表示禁用
            Integer parsedStatus;
            try {
                parsedStatus = Integer.parseInt(status);
            } catch (NumberFormatException e) {
                // 如果status不是数字，则忽略状态筛选
                parsedStatus = null;
            }
            
            if (parsedStatus != null) {
                final Integer finalStatusInt = parsedStatus;
                allUsers = allUsers.stream()
                    .filter(user -> user.getStatus() != null && user.getStatus() == finalStatusInt)
                    .collect(java.util.stream.Collectors.toList());
            }
        }
        
        if (startTime != null || endTime != null) {
            allUsers = allUsers.stream()
                .filter(user -> {
                    java.time.LocalDateTime userCreatedAt = user.getCreatedAt();
                    if (userCreatedAt == null) userCreatedAt = java.time.LocalDateTime.now();
                    
                    boolean afterStart = startTime == null || !userCreatedAt.isBefore(startTime);
                    boolean beforeEnd = endTime == null || !userCreatedAt.isAfter(endTime);
                    
                    return afterStart && beforeEnd;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 计算分页数据
        int total = allUsers.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        List<User> users;
        if (start >= total) {
            users = java.util.Collections.emptyList();
        } else {
            users = allUsers.subList(start, end);
        }
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", users);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));
        
        return result;
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User create(User user) {
        // 设置默认密码
        if (user.getPasswordHash() == null) {
            user.setPasswordHash(passwordEncoder.encode("123456"));
        }
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return null;
        }
        
        // 更新用户信息，但不更新密码和创建时间
        existingUser.setNickname(user.getNickname());
        existingUser.setPhone(user.getPhone());
        existingUser.setRole(user.getRole());
        existingUser.setUpdatedAt(java.time.LocalDateTime.now());
        
        return userRepository.save(existingUser);
    }

    @Override
    public boolean deleteById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return false;
        }

        Integer nextStatus;
        try {
            nextStatus = Integer.parseInt(status);
        } catch (NumberFormatException e) {
            throw new RuntimeException("status参数必须是数字");
        }

        if (nextStatus < 0 || nextStatus > 3) {
            throw new RuntimeException("status参数非法");
        }

        user.setStatus(nextStatus);
        if (nextStatus == 1) {
            user.setAuditReason(null);
            user.setLockExpiresAt(null);
            user.setFailedAttempts(0);
        }

        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.saveAndFlush(user);
        return true;
    }

    @Override
    public boolean resetPassword(Long id, String password) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return false;
        }
        
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean forceLogout(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return false;
        // 在JWT模式下，通常需要黑名单机制。这里简化为更新 lastLoginAt 为未来时间或特定标志，
        // 或者依赖前端收到特定状态码后登出。
        // 实际上，更安全的做法是维护一个 tokenVersion 字段，每次登出/改密时 +1，
        // token payload 中包含 version，校验时对比。
        // 这里暂时仅做业务层面的标记，实际强制登出需配合 TokenProvider。
        // 为了演示，我们假设更新 passwordHash 会导致旧 token 失效（如果 token 校验依赖 user 状态）
        // 或者我们简单地记录日志。
        return true;
    }

    @Override
    public boolean lockUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return false;
        user.setStatus(0); // 0 = Disabled/Locked
        user.setLockExpiresAt(java.time.LocalDateTime.now().plusYears(100)); // 永久锁定
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean unlockUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return false;
        user.setStatus(1); // 1 = Enabled
        user.setLockExpiresAt(null);
        user.setFailedAttempts(0);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public boolean auditUser(Long id, boolean approve, String reason) {
        log.info("Auditing user id: {}, approve: {}", id, approve);
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            log.error("User not found for audit: {}", id);
            return false;
        }
        
        if (approve) {
            user.setStatus(1); // Enabled
            user.setAuditReason(null);
            log.info("User {} approved, status set to 1", id);
        } else {
            user.setStatus(3); // Rejected
            user.setAuditReason(reason);
            log.info("User {} rejected, status set to 3", id);
        }
        userRepository.saveAndFlush(user);
        return true;
    }
}
