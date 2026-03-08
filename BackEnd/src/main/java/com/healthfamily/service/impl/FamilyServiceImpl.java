package com.healthfamily.service.impl;

import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.constant.MemberRole;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyMember;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.service.FamilyService;
import com.healthfamily.web.dto.AdminFamilyListItemDto;
import com.healthfamily.web.dto.FamilyCreateRequest;
import com.healthfamily.web.dto.FamilyInviteRequest;
import com.healthfamily.web.dto.FamilyMemberResponse;
import com.healthfamily.web.dto.FamilyMemberUpdateRequest;
import com.healthfamily.web.dto.FamilyResponse;
import com.healthfamily.web.dto.FamilyUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
/**
 * 家庭服务Impl实现类
 * <p>
 * 实现平台核心业务服务，负责业务编排、数据聚合及与 AI/规则引擎的协同。
 * </p>
 */
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private static final String INVITE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;
    private final HealthLogRepository healthLogRepository;
    private final ConstitutionAssessmentRepository constitutionAssessmentRepository;

    @Override
    @Transactional
    /**
     * 创建
     * @param userId 家庭成员唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public FamilyResponse createFamily(Long userId, FamilyCreateRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));



        String inviteCode = generateInviteCode();
        while (familyRepository.findByInviteCode(inviteCode).isPresent()) {
            inviteCode = generateInviteCode();
        }

        Family family = Family.builder()
                .owner(owner)
                .name(request.name())
                .inviteCode(inviteCode)
                .status(0)
                .build();
        Family saved = familyRepository.save(family);

        FamilyMember member = FamilyMember.builder()
                .family(saved)
                .user(owner)
                .relation("OWNER")
                .admin(true)
                .role(MemberRole.ADMIN)
                .build();
        familyMemberRepository.save(member);
        return toFamilyResponse(saved, userId);
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param userId 家庭成员唯一标识
     * @param familyId 家庭唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public FamilyResponse joinFamily(Long userId, Long familyId, FamilyInviteRequest request) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        if (family.getStatus() == null || family.getStatus() != 1) {
            throw new BusinessException(40302, "家庭未通过审核，暂不可加入");
        }
        String inviteCode = request.inviteCode();
        if (!family.getInviteCode().equalsIgnoreCase(inviteCode)) {
            throw new BusinessException(40003, "邀请码错误");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));

        familyMemberRepository.findByFamilyAndUser(family, user)
                .ifPresent(member -> {
                    throw new BusinessException(40004, "已加入该家庭");
                });

        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(user)
                .relation("MEMBER")
                .admin(false)
                .role(MemberRole.MEMBER)
                .build();
        familyMemberRepository.save(member);
        return toFamilyResponse(family, userId);
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param userId 家庭成员唯一标识
     * @param inviteCode 业务参数
     * @return 业务返回结果
     */
    public FamilyResponse joinByCode(Long userId, String inviteCode) {
        Family family = familyRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在或邀请码错误"));
        if (family.getStatus() == null || family.getStatus() != 1) {
            throw new BusinessException(40302, "家庭未通过审核，暂不可加入");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        familyMemberRepository.findByFamilyAndUser(family, user)
                .ifPresent(member -> { throw new BusinessException(40004, "已加入该家庭"); });
        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(user)
                .relation("MEMBER")
                .admin(false)
                .role(MemberRole.MEMBER)
                .build();
        familyMemberRepository.save(member);
        return toFamilyResponse(family, userId);
    }

    @Override
    @Transactional
    /**
     * 获取
     * @param userId 家庭成员唯一标识
     * @param familyId 家庭唯一标识
     * @return 业务返回结果
     */
    public FamilyResponse getFamily(Long userId, Long familyId) {
        Family family = ensureMembership(userId, familyId);
        return toFamilyResponse(family, userId);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 查询列表
     * @param userId 家庭成员唯一标识
     * @return 业务返回结果
     */
    public List<FamilyResponse> listUserFamilies(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));

        List<Family> memberFamilies = familyMemberRepository.findFamiliesByUser(user);
        List<Family> ownedFamilies = familyRepository.findByOwner_Id(userId);

        return java.util.stream.Stream.concat(memberFamilies.stream(), ownedFamilies.stream())
                .filter(f -> f != null)
                .collect(Collectors.toMap(Family::getId, f -> f, (a, b) -> a))
                .values()
                .stream()
                .filter(f -> f.getStatus() != null && f.getStatus() == 1)
                .map(f -> toFamilyResponse(f, userId))
                .collect(Collectors.toList());
    }

    private FamilyResponse toFamilyResponse(Family family, Long currentUserId) {
        Long ownerId = (family.getOwner() != null) ? family.getOwner().getId() : null;
        
        // 检查当前用户是否是该家庭的管理员
        boolean isAdmin = (ownerId != null && ownerId.equals(currentUserId));
        if (!isAdmin) {
            // 安全起见，先查出用户实体，或者使用 id 查询
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                isAdmin = familyMemberRepository.findByFamilyAndUser(family, currentUser)
                        .map(fm -> Boolean.TRUE.equals(fm.getAdmin()))
                        .orElse(false);
            }
        }

        return new FamilyResponse(
                family.getId(),
                family.getName(),
                ownerId,
                family.getInviteCode(),
                isAdmin
        );
    }

    @Override
    @Transactional
    /**
     * 查询列表
     * @param userId 家庭成员唯一标识
     * @param familyId 家庭唯一标识
     * @return 业务返回结果
     */
    public List<FamilyMemberResponse> listMembers(Long userId, Long familyId) {
        Family family = ensureMembership(userId, familyId);
        return familyMemberRepository.findByFamily(family).stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    private Family ensureMembership(Long userId, Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));

        familyMemberRepository.findByFamilyAndUser(family, user)
                .orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        return family;
    }

    private FamilyMemberResponse toMemberResponse(FamilyMember member) {
        User user = member.getUser();
        if (user == null) {
            // 如果用户不存在，创建一个默认的响应对象
            return new FamilyMemberResponse(
                    member.getId(),
                    null, // userId
                    "已删除用户", // nickname
                    member.getRelation(),
                    member.getAdmin(),
                    null, // phone
                    null, // avatar
                    member.getRole() != null ? member.getRole().name() : (member.getAdmin() ? MemberRole.ADMIN.name() : MemberRole.MEMBER.name()),
                    java.util.Collections.emptyList(), // tags
                    null, // lastActive
                    false, // shareToDoctor
                    false // shareToFamily
            );
        }
        
        // 获取Profile以解析头像和标签
        String avatar = null;
        java.util.List<String> tags = new java.util.ArrayList<>();
        com.healthfamily.domain.entity.Profile profile = profileRepository.findById(user.getId()).orElse(null);
        
        if (profile != null) {
             // 解析头像
             try {
                 if (profile.getPreferences() != null && !profile.getPreferences().isBlank()) {
                     java.util.Map<?, ?> map = objectMapper.readValue(profile.getPreferences(), java.util.Map.class);
                     Object avt = map.get("avatar");
                     if (avt != null) avatar = avt.toString();
                 }
             } catch (Exception e) {
                 // ignore
             }

             // 解析标签
             try {
                 if (profile.getHealthTags() != null && !profile.getHealthTags().isBlank()) {
                     java.util.List<String> healthTags = objectMapper.readValue(profile.getHealthTags(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
                     if (healthTags != null) tags.addAll(healthTags);
                 }
                 if (profile.getAllergies() != null && !profile.getAllergies().isBlank()) {
                     java.util.List<String> allergies = objectMapper.readValue(profile.getAllergies(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
                     if (allergies != null) {
                         tags.addAll(allergies.stream().map(a -> "过敏:" + a).collect(Collectors.toList()));
                     }
                 }
             } catch (Exception e) {
                 // ignore
             }
        }
        
        String lastActive = user.getLastLoginAt() != null ? 
            user.getLastLoginAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
            (user.getUpdatedAt() != null ? user.getUpdatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "从未登录");
        
        // 获取隐私设置
        boolean shareToDoctor = false;
        boolean shareToFamily = false;
        if (profile != null && profile.getPreferences() != null && !profile.getPreferences().isBlank()) {
            try {
                java.util.Map<?, ?> map = objectMapper.readValue(profile.getPreferences(), java.util.Map.class);
                Object sd = map.get("shareToDoctor");
                if (sd instanceof Boolean) shareToDoctor = (Boolean) sd;
                Object sf = map.get("shareToFamily");
                if (sf instanceof Boolean) shareToFamily = (Boolean) sf;
            } catch (Exception e) {
                // ignore
            }
        }
        
        return new FamilyMemberResponse(
                member.getId(),
                user.getId(),
                user.getNickname(),
                member.getRelation(),
                member.getAdmin(),
                user.getPhone(),
                avatar,
                member.getRole() != null ? member.getRole().name() : (member.getAdmin() ? MemberRole.ADMIN.name() : MemberRole.MEMBER.name()),
                tags,
                lastActive,
                shareToDoctor,
                shareToFamily
        );
    }

    private String readAvatar(Long userId) {
        return profileRepository.findById(userId).map(profile -> {
            String prefs = profile.getPreferences();
            if (prefs == null || prefs.isBlank()) return null;
            try {
                java.util.Map<?, ?> map = objectMapper.readValue(prefs, java.util.Map.class);
                Object avatar = map.get("avatar");
                return avatar != null ? avatar.toString() : null;
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                return null;
            }
        }).orElse(null);
    }

    private String generateInviteCode() {
        StringBuilder builder = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            builder.append(INVITE_CHARS.charAt(RANDOM.nextInt(INVITE_CHARS.length())));
        }
        return builder.toString();
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param userId 家庭成员唯一标识
     * @param familyId 家庭唯一标识
     * @return 业务返回结果
     */
    public FamilyResponse regenerateInviteCode(Long userId, Long familyId) {
        Family family = ensureAdminAccess(userId, familyId);
        String inviteCode = generateInviteCode();
        while (familyRepository.findByInviteCode(inviteCode).isPresent()) {
            inviteCode = generateInviteCode();
        }
        family.setInviteCode(inviteCode);
        Family saved = familyRepository.save(family);
        return toFamilyResponse(saved, userId);
    }

    @Override
    @Transactional
    /**
     * 更新
     * @param userId 家庭成员唯一标识
     * @param familyId 家庭唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public FamilyResponse updateFamily(Long userId, Long familyId, FamilyUpdateRequest request) {
        Family family = ensureAdminAccess(userId, familyId);
        family.setName(request.name());
        Family saved = familyRepository.save(family);
        return toFamilyResponse(saved, userId);
    }

    @Override
    @Transactional
    /**
     * 更新
     * @param userId 家庭成员唯一标识
     * @param familyId 家庭唯一标识
     * @param memberId 家庭成员唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public FamilyMemberResponse updateMember(Long userId, Long familyId, Long memberId, FamilyMemberUpdateRequest request) {
        Family family = ensureAdminAccess(userId, familyId);
        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(40403, "成员不存在"));
        if (!member.getFamily().getId().equals(familyId)) {
            throw new BusinessException(40303, "成员不属于该家庭");
        }
        
        if (request.relation() != null && !request.relation().isBlank()) {
            member.setRelation(request.relation());
        }
        
        if (request.role() != null && !request.role().isBlank()) {
            try {
                MemberRole role = MemberRole.valueOf(request.role().toUpperCase());
                member.setRole(role);
                member.setAdmin(role == MemberRole.ADMIN);
            } catch (IllegalArgumentException e) {
                throw new BusinessException(40005, "无效的角色类型");
            }
        }
        
        if (request.shareToFamily() != null) {
            User user = member.getUser();
            if (user != null) {
                updateDataShare(user.getId(), request.shareToFamily());
            }
        }
        
        FamilyMember saved = familyMemberRepository.save(member);
        return toMemberResponse(saved);
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param userId 家庭成员唯一标识
     * @param familyId 家庭唯一标识
     * @param memberId 家庭成员唯一标识
     * @return 无
     */
    public void removeMember(Long userId, Long familyId, Long memberId) {
        Family family = ensureAdminAccess(userId, familyId);
        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(40403, "成员不存在"));
        if (!member.getFamily().getId().equals(familyId)) {
            throw new BusinessException(40303, "成员不属于该家庭");
        }
        User memberUser = member.getUser();
        if (memberUser == null) {
            throw new BusinessException(40403, "成员不存在或用户信息已删除");
        }
        if (memberUser.getId().equals(userId)) {
            throw new BusinessException(40006, "不能移除自己");
        }
        if (family.getOwner().getId().equals(memberUser.getId())) {
            throw new BusinessException(40007, "不能移除家庭所有者");
        }
        familyMemberRepository.delete(member);
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param userId 家庭成员唯一标识
     * @param familyId 家庭唯一标识
     * @param shareToFamily 业务参数
     * @return 无
     */
    public void setDataShare(Long userId, Long familyId, Boolean shareToFamily) {
        ensureMembership(userId, familyId);
        updateDataShare(userId, shareToFamily);
    }

    private Family ensureAdminAccess(Long userId, Long familyId) {
        Family family = ensureMembership(userId, familyId);
        FamilyMember member = familyMemberRepository.findByFamilyAndUser(family, 
                userRepository.findById(userId).orElseThrow(() -> new BusinessException(40401, "用户不存在")))
                .orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        if (!member.getAdmin() && member.getRole() != MemberRole.ADMIN) {
            throw new BusinessException(40304, "仅管理员可执行此操作");
        }
        return family;
    }

    private void updateDataShare(Long userId, Boolean shareToFamily) {
        profileRepository.findById(userId).ifPresent(profile -> {
            try {
                java.util.Map<String, Object> prefs = profile.getPreferences() != null && !profile.getPreferences().isBlank()
                        ? objectMapper.readValue(profile.getPreferences(), java.util.Map.class)
                        : new java.util.HashMap<>();
                prefs.put("shareToFamily", shareToFamily);
                profile.setPreferences(objectMapper.writeValueAsString(prefs));
                profileRepository.save(profile);
            } catch (Exception e) {
                throw new BusinessException(50001, "更新数据共享设置失败");
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminFamilyList(String keyword, String status, int page, int size, LocalDateTime startTime, LocalDateTime endTime) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Specification<Family> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                Predicate nameLike = cb.like(root.get("name"), "%" + keyword + "%");
                Predicate ownerNicknameLike = cb.like(root.get("owner").get("nickname"), "%" + keyword + "%");
                predicates.add(cb.or(nameLike, ownerNicknameLike));
            }

            if (status != null && !status.isEmpty()) {
                try {
                    Integer desiredStatus = Integer.parseInt(status);
                    predicates.add(cb.equal(root.get("status"), desiredStatus));
                } catch (NumberFormatException ex) {
                    // 状态格式不正确，可以记录日志或忽略
                }
            }

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Family> familyPage = familyRepository.findAll(spec, pageable);

        List<AdminFamilyListItemDto> items = familyPage.getContent().stream().map(f -> {
            long memberCount = familyMemberRepository.countByFamily_Id(f.getId());
            List<Long> userIds = familyMemberRepository.findUserIdsByFamilyId(f.getId());
            long healthLogCount = userIds.isEmpty() ? 0 : healthLogRepository.countByUser_IdIn(userIds);
            long assessmentCount = userIds.isEmpty() ? 0 : constitutionAssessmentRepository.countByUser_IdIn(userIds);
            String ownerName = f.getOwner() != null ? f.getOwner().getNickname() : null;
            String ownerAvatar = f.getOwner() != null ? readAvatar(f.getOwner().getId()) : null;
            return new AdminFamilyListItemDto(
                    f.getId(),
                    f.getName(),
                    ownerName,
                    ownerAvatar,
                    memberCount,
                    healthLogCount,
                    assessmentCount,
                    f.getStatus(),
                    f.getCreatedAt()
            );
        }).toList();

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", items);
        result.put("total", familyPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", familyPage.getTotalPages());

        return result;
    }

    @Override
    /**
     * 执行业务操作
     * @param id 业务对象唯一标识
     * @return 业务返回结果
     */
    public Family findById(Long id) {
        return familyRepository.findById(id).orElse(null);
    }

    @Override
    /**
     * 创建
     * @param family 业务参数
     * @return 业务返回结果
     */
    public Family create(Family family) {
        // 生成邀请码
        String inviteCode = generateInviteCode();
        while (familyRepository.findByInviteCode(inviteCode).isPresent()) {
            inviteCode = generateInviteCode();
        }
        family.setInviteCode(inviteCode);
        if (family.getStatus() == null) family.setStatus(1);
        return familyRepository.save(family);
    }

    @Override
    /**
     * 更新
     * @param id 业务对象唯一标识
     * @param family 业务参数
     * @return 业务返回结果
     */
    public Family update(Long id, Family family) {
        Family existingFamily = familyRepository.findById(id).orElse(null);
        if (existingFamily == null) {
            return null;
        }
        
        // 更新家庭信息
        existingFamily.setName(family.getName());
        if (family.getStatus() != null) {
            existingFamily.setStatus(family.getStatus());
        }
        existingFamily.setUpdatedAt(java.time.LocalDateTime.now());
        
        return familyRepository.save(existingFamily);
    }

    @Override
    /**
     * 删除
     * @param id 业务对象唯一标识
     * @return 业务返回结果
     */
    public boolean deleteById(Long id) {
        if (familyRepository.existsById(id)) {
            familyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    /**
     * 更新
     * @param id 业务对象唯一标识
     * @param status 业务参数
     * @return 业务返回结果
     */
    public boolean updateStatus(Long id, String status) {
        Family family = familyRepository.findById(id).orElse(null);
        if (family == null) {
            return false;
        }
        
        Integer parsed;
        try {
            parsed = Integer.parseInt(status);
        } catch (NumberFormatException ex) {
            throw new BusinessException(40008, "无效的状态值");
        }
        family.setStatus(parsed);
        family.setUpdatedAt(java.time.LocalDateTime.now());
        familyRepository.save(family);
        return true;
    }
}

