package com.healthfamily.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.constant.AlertStatus;
import com.healthfamily.domain.constant.FrequencyType;
import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.constant.HealthPlanStatus;
import com.healthfamily.domain.constant.HealthPlanType;
import com.healthfamily.domain.constant.ReminderStatus;
import com.healthfamily.domain.constant.Sex;
import com.healthfamily.domain.entity.AbnormalHandlingRecord;
import com.healthfamily.domain.entity.AiRecommendation;
import com.healthfamily.domain.entity.ConsultationSession;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.DoctorNote;
import com.healthfamily.domain.entity.DoctorProfile;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.FamilyDoctor;
import com.healthfamily.domain.entity.FamilyMember;
import com.healthfamily.domain.entity.HealthAlert;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.HealthPlan;
import com.healthfamily.domain.entity.Profile;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.*;
import com.healthfamily.service.AiAssistantService;
import com.healthfamily.service.HealthDataAiService;
import com.healthfamily.service.HealthReminderService;
import com.healthfamily.service.SeasonalWellnessService;
import com.healthfamily.web.dto.AdminDoctorDto;
import com.healthfamily.web.dto.DoctorRegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.healthfamily.web.dto.HandleRiskRequest;
import com.healthfamily.web.dto.HealthPlanRequest;
import com.healthfamily.web.dto.HealthPlanResponse;
import com.healthfamily.web.dto.DoctorStatsResponse;
import com.healthfamily.web.dto.DoctorWorkbenchDto;
import com.healthfamily.web.dto.HighRiskPatientDto;
import com.healthfamily.web.dto.ConsultationTaskDto;
import com.healthfamily.web.dto.DoctorSettingsRequest;
import com.healthfamily.web.dto.DoctorSettingsResponse;
import com.healthfamily.web.dto.DoctorNotificationSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.healthfamily.web.dto.PatientDetailResponse;
import com.healthfamily.web.dto.DoctorNoteRequest;
import com.healthfamily.web.dto.DoctorNoteResponse;
import com.healthfamily.web.dto.AbnormalEventDto;
import com.healthfamily.web.dto.HighRiskMemberDto;
import com.healthfamily.web.dto.MonitoringDataResponse;
import com.healthfamily.web.dto.MonitoringAlertDto;
import com.healthfamily.web.dto.SeasonalWellnessDTO;
import com.healthfamily.web.dto.ReminderRequest;
import com.healthfamily.domain.constant.AlertStatus;
import com.healthfamily.domain.constant.ReminderStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.healthfamily.service.DoctorService;
import com.healthfamily.web.dto.DoctorViewResponse;
import com.healthfamily.web.dto.FamilyDoctorResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
/**
 * 医生服务Impl实现类
 * <p>
 * 实现平台核心业务服务，负责业务编排、数据聚合及与 AI/规则引擎的协同。
 * </p>
 */
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DoctorServiceImpl.class);

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyDoctorRepository familyDoctorRepository;
    private final UserRepository userRepository;
    private final HealthDataAiService healthDataAiService;
    private final ProfileRepository profileRepository;
    private final HealthLogRepository healthLogRepository;
    private final HealthAlertRepository healthAlertRepository;
    private final HealthReminderRepository healthReminderRepository;
    private final ConstitutionAssessmentRepository assessmentRepository;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final DoctorNoteRepository doctorNoteRepository;
    private final HealthPlanRepository healthPlanRepository;
    private final com.healthfamily.domain.repository.ConsultationSessionRepository consultationSessionRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final com.healthfamily.domain.repository.DoctorRatingRepository doctorRatingRepository;
    private final AlertRepository monitoringAlertRepository;
    private final HealthReportRepository healthReportRepository;
    private final AbnormalHandlingRecordRepository abnormalHandlingRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final ChatModel chatModel;
    private final SeasonalWellnessService seasonalWellnessService;
    private final HealthReminderService healthReminderService;
    private final AiAssistantService aiAssistantService;
    private static final TypeReference<List<String>> STRING_LIST_TYPE_REF = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};
    private static final com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>> MAP_TYPE = new com.fasterxml.jackson.core.type.TypeReference<>() {};
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param familyId 家庭唯一标识
     * @param doctorUserId 业务对象唯一标识
     * @return 业务返回结果
     */
    public FamilyDoctorResponse bindDoctor(Long requesterId, Long familyId, Long doctorUserId) {
        Family family = ensureAdmin(requesterId, familyId);
        User doctor = userRepository.findById(doctorUserId)
                .orElseThrow(() -> new BusinessException(40401, "医生用户不存在"));

        if (!familyDoctorRepository.findByFamily(family).isEmpty()) {
            throw new BusinessException(40021, "已绑定医生");
        }

        FamilyDoctor fd = FamilyDoctor.builder().family(family).doctor(doctor).build();
        familyDoctorRepository.save(fd);
        
        // Update Service Count
        DoctorProfile profile = doctorProfileRepository.findById(doctor.getId()).orElseGet(() -> {
             DoctorProfile p = new DoctorProfile();
             p.setDoctor(doctor);
             p.setCertificationStatus("APPROVED");
             p.setRating(java.math.BigDecimal.ZERO);
             p.setRatingCount(0);
             p.setServiceCount(0);
             return p;
        });
        
        if (profile.getServiceCount() == null) {
            profile.setServiceCount(0);
        }
        // Recalculate real count to ensure consistency
        int realCount = familyDoctorRepository.findByDoctor(doctor).size();
        profile.setServiceCount(realCount);
        doctorProfileRepository.save(profile);
        
        String title = null;
        String hospital = null;
        String department = null;
        String bio = null;
        Double rating = 5.0;
        Integer serviceCount = 0;
        
        if (profile != null) {
            title = profile.getTitle();
            hospital = profile.getHospital();
            department = profile.getDepartment();
            bio = profile.getBio();
            if (profile.getRating() != null) {
                rating = profile.getRating().doubleValue();
            }
            if (profile.getServiceCount() != null) {
                serviceCount = profile.getServiceCount();
            }
        }
        
        return new FamilyDoctorResponse(
            doctor.getId(), 
            doctor.getNickname(), 
            doctor.getPhone(), 
            readAvatar(doctor.getId()),
            title,
            hospital,
            department,
            bio,
            rating,
            serviceCount
        );
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param familyId 家庭唯一标识
     * @return 无
     */
    public void unbindDoctor(Long requesterId, Long familyId) {
        Family family = ensureAdmin(requesterId, familyId);
        List<FamilyDoctor> list = familyDoctorRepository.findByFamily(family);
        if (list.isEmpty()) {
            throw new BusinessException(40403, "未绑定医生");
        }
        
        // Get doctor before deleting binding
        User doctor = list.get(0).getDoctor();
        
        familyDoctorRepository.deleteAll(list);
        
        // Update service count
        doctorProfileRepository.findByDoctorId(doctor.getId()).ifPresent(profile -> {
            int count = familyDoctorRepository.findByDoctor(doctor).size();
            profile.setServiceCount(count);
            doctorProfileRepository.save(profile);
        });
    }

    @Override
    @Transactional
    /**
     * 查询列表
     * @param requesterId 业务对象唯一标识
     * @param familyId 家庭唯一标识
     * @return 业务返回结果
     */
    public java.util.List<com.healthfamily.web.dto.FamilyMemberResponse> listBoundMembers(Long requesterId, Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        boolean isDoctor = familyDoctorRepository.findByFamily(family).stream()
                .anyMatch(fd -> java.util.Objects.equals(fd.getDoctor().getId(), requesterId));
        if (!isDoctor) {
            throw new BusinessException(40301, "无权访问该家庭");
        }

        List<com.healthfamily.domain.entity.FamilyMember> members = familyMemberRepository.findByFamily(family);
        List<Long> userIds = members.stream()
                .map(m -> m.getUser() != null ? m.getUser().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, Profile> profileMap = profileRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p));

        return members.stream()
                .map(m -> {
                    User user = m.getUser();
                    if (user == null) {
                        return new com.healthfamily.web.dto.FamilyMemberResponse(
                                m.getId(),
                                null, // userId
                                "已删除用户", // nickname
                                m.getRelation(),
                                m.getAdmin(),
                                null, // phone
                                null, // avatar
                                null, // role
                                java.util.Collections.emptyList(), // tags
                                null // lastActive
                        );
                    }

                    // 获取头像和标签
                    String avatar = null;
                    List<String> tags = new ArrayList<>();
                    Profile profile = profileMap.get(user.getId());
                    
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
                                List<String> healthTags = objectMapper.readValue(profile.getHealthTags(), STRING_LIST_TYPE);
                                if (healthTags != null) tags.addAll(healthTags);
                            }
                            if (profile.getAllergies() != null && !profile.getAllergies().isBlank()) {
                                List<String> allergies = objectMapper.readValue(profile.getAllergies(), STRING_LIST_TYPE);
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

                    return new com.healthfamily.web.dto.FamilyMemberResponse(
                            m.getId(),
                            user.getId(),
                            user.getNickname(),
                            m.getRelation(),
                            m.getAdmin(),
                            user.getPhone(),
                            avatar,
                            user.getRole() != null ? user.getRole().name() : null,
                            tags,
                            lastActive
                    );
                })
                .toList();
    }

    @Override
    /**
     * 获取
     * @param requesterId 业务对象唯一标识
     * @param familyId 家庭唯一标识
     * @return 业务返回结果
     */
    public FamilyDoctorResponse getFamilyDoctor(Long requesterId, Long familyId) {
        Family family = ensureMember(requesterId, familyId);
        List<FamilyDoctor> list = familyDoctorRepository.findByFamily(family);
        if (list.isEmpty()) {
            return null;
        }
        FamilyDoctor fd = list.get(0);
        Long doctorId = fd.getDoctor().getId();
        
        // Use findById for standard PK lookup
        DoctorProfile profile = doctorProfileRepository.findById(doctorId).orElse(null);
        
        // Calculate real service count from binding table
        int realServiceCount = familyDoctorRepository.findByDoctor(fd.getDoctor()).size();
        // Safety check: since we found 'fd', count must be at least 1
        if (realServiceCount == 0) {
            realServiceCount = 1;
        }
        
        // Auto-fix profile if count mismatches
        if (profile != null && (profile.getServiceCount() == null || profile.getServiceCount() != realServiceCount)) {
            profile.setServiceCount(realServiceCount);
            doctorProfileRepository.save(profile);
        }
        
        String title = null;
        String hospital = null;
        String department = null;
        String bio = null;
        Double rating = 5.0;
        Integer serviceCount = realServiceCount; // Use real count
        
        if (profile != null) {
            title = profile.getTitle();
            hospital = profile.getHospital();
            department = profile.getDepartment();
            bio = profile.getBio();
            if (profile.getRating() != null) {
                // Ensure 1 decimal place for consistency
                rating = profile.getRating().setScale(1, java.math.RoundingMode.HALF_UP).doubleValue();
            }
        } else {
             // Try to find by doctorId via query method as fallback (should be same)
             profile = doctorProfileRepository.findByDoctorId(doctorId).orElse(null);
             if (profile != null) {
                if (profile.getRating() != null) {
                    rating = profile.getRating().setScale(1, java.math.RoundingMode.HALF_UP).doubleValue();
                }
                // Fix count on fallback profile too
                if (profile.getServiceCount() == null || profile.getServiceCount() != realServiceCount) {
                    profile.setServiceCount(realServiceCount);
                    doctorProfileRepository.save(profile);
                }
             }
        }

        return new FamilyDoctorResponse(
            doctorId, 
            fd.getDoctor().getNickname(), 
            fd.getDoctor().getPhone(), 
            readAvatar(doctorId),
            title,
            hospital,
            department,
            bio,
            rating,
            serviceCount
        );
    }

    @Override
    /**
     * 获取
     * @param requesterId 业务对象唯一标识
     * @param familyId 家庭唯一标识
     * @param useAi 业务参数
     * @return 业务返回结果
     */
    public DoctorViewResponse getDoctorView(Long requesterId, Long familyId, Boolean useAi) {
        // 请求者必须是家庭成员或绑定医生
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User requester = userRepository.findById(requesterId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));

        Optional<com.healthfamily.domain.entity.FamilyMember> memberRecord = familyMemberRepository.findByFamilyAndUser(family, requester);
        boolean isMember = memberRecord.isPresent();
        boolean isDoctor = familyDoctorRepository.findByFamily(family).stream()
                .anyMatch(fd -> Objects.equals(fd.getDoctor().getId(), requesterId));
        boolean isFamilyAdmin = false;
        if (isMember) {
            com.healthfamily.domain.entity.FamilyMember member = memberRecord.get();
            isFamilyAdmin = Boolean.TRUE.equals(member.getAdmin()) || member.getRole() == com.healthfamily.domain.constant.MemberRole.ADMIN
                    || requester.getRole() == com.healthfamily.domain.constant.UserRole.FAMILY_ADMIN;
        }
        if (!isDoctor && !isFamilyAdmin) {
            throw new BusinessException(40301, "无权限访问医生视图");
        }

        // 汇总近7天成员数据（仅限共享）
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        List<com.healthfamily.domain.entity.FamilyMember> members = familyMemberRepository.findByFamily(family);

        Map<String, Object> telemetry = new HashMap<>();
        int totalMembers = 0;
        int sharedMembers = 0;
        int totalLogs = 0;
        List<User> viewableUsers = new ArrayList<>();

        for (com.healthfamily.domain.entity.FamilyMember m : members) {
            User u = m.getUser();
            if (u == null) continue; // 跳过用户不存在的成员
            totalMembers++;
            boolean canView = Objects.equals(requesterId, u.getId()) || canViewMemberData(u.getId(), isDoctor);
            if (canView) {
                viewableUsers.add(u);
                sharedMembers++;
            }
            
            List<HealthLog> logs = canView
                    ? healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(u.getId(), com.healthfamily.domain.constant.HealthLogType.VITALS)
                        .stream().filter(l -> !l.getLogDate().isBefore(start)).collect(Collectors.toList())
                    : java.util.Collections.emptyList();
            totalLogs += logs.size();
            
            // 统计近7日体征日志总数，作为活跃度指标
            String displayName = u.getNickname();
            if (displayName == null || displayName.isBlank()) {
                displayName = u.getPhone();
            }
            if (displayName == null || displayName.isBlank()) {
                displayName = "成员" + u.getId();
            }
            String key = displayName;
            if (telemetry.containsKey(key)) {
                key = displayName + "(" + u.getId() + ")";
            }
            Map<String, Object> entry = new HashMap<>();
            entry.put("count", logs.size());
            entry.put("items", java.util.Collections.emptyList()); // 不再需要详细的日志项
            entry.put("userId", u.getId());
            entry.put("displayName", displayName);
            entry.put("shared", canView);
            telemetry.put(key, entry);
        }

        // 计算待办事项统计
        long pendingConsultationsCount = consultationSessionRepository.countByDoctorAndUnreadCountDoctorGreaterThan(requester, 0);
        long pendingPlansCount = 0; // 待审核计划功能暂未实现
        
        // 今日需随访数（基于提醒）
        java.time.LocalDateTime todayStart = today.atStartOfDay();
        java.time.LocalDateTime todayEnd = todayStart.plusDays(1);
        long todayFollowupsCount = healthReminderRepository.findByFamily_IdOrderByScheduledTimeAsc(familyId)
                .stream()
                .filter(r -> r.getType() == com.healthfamily.domain.constant.ReminderType.FOLLOW_UP)
                .filter(r -> r.getStatus() == ReminderStatus.PENDING || r.getStatus() == ReminderStatus.SENT)
                .filter(r -> {
                    java.time.LocalDateTime scheduled = r.getScheduledTime();
                    return scheduled != null && !scheduled.isBefore(todayStart) && scheduled.isBefore(todayEnd);
                })
                .count();

        // 异常事件列表（基于实时检测 + Alert + 异常提醒）
        List<AbnormalEventDto> abnormalEvents = new ArrayList<>();
        
        // 1. 重新扫描近7天日志，生成实时异常事件
        for (User user : viewableUsers) {
             List<HealthLog> recentLogs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(user.getId(), com.healthfamily.domain.constant.HealthLogType.VITALS)
                    .stream()
                    .filter(l -> !l.getLogDate().isBefore(start))
                    .collect(Collectors.toList());
                    
             for (HealthLog log : recentLogs) {
                 Map<String, Object> content = safeParse(log.getContentJson());
                 if (checkAnomaly(user.getId(), content)) {
                     abnormalEvents.add(new AbnormalEventDto(
                         "LOG_" + log.getId(),
                         "HEALTH_LOG",
                         "健康指标异常",
                         "检测到 " + user.getNickname() + " " + buildLogSummary(content) + " 异常",
                         com.healthfamily.domain.constant.AlertLevel.CRITICAL.name(),
                         log.getLogDate().atStartOfDay(),
                         "PENDING",
                         user.getId()
                     ));
                 }
             }
        }
        
        // 从Alert中获取异常事件（近7天，状态为PENDING）
        List<HealthAlert> recentAlerts = healthAlertRepository.findByFamily_IdOrderByCreatedAtDesc(familyId)
                .stream()
                .filter(a -> a.getStatus() == AlertStatus.PENDING)
                .filter(a -> a.getCreatedAt().toLocalDate().isAfter(start.minusDays(1)))
                .limit(10)
                .collect(Collectors.toList());
        
        for (HealthAlert alert : recentAlerts) {
            abnormalEvents.add(new AbnormalEventDto(
                    String.valueOf(alert.getId()),
                    "ALERT",
                    alert.getMetric() + "异常预警",
                    alert.getMessage(),
                    alert.getSeverity().name(),
                    alert.getCreatedAt(),
                    alert.getStatus().name(),
                    alert.getUser().getId()
            ));
        }
        
        // 从异常提醒中获取（近7天，高优先级）
        List<com.healthfamily.domain.entity.HealthReminder> abnormalReminders = healthReminderRepository.findByFamily_IdOrderByScheduledTimeAsc(familyId)
                .stream()
                .filter(r -> r.getStatus() == ReminderStatus.PENDING || r.getStatus() == ReminderStatus.SENT)
                .filter(r -> r.getType() == com.healthfamily.domain.constant.ReminderType.ABNORMAL)
                .filter(r -> r.getCreatedAt().toLocalDate().isAfter(start.minusDays(1)))
                .limit(10)
                .collect(Collectors.toList());
        
        for (com.healthfamily.domain.entity.HealthReminder reminder : abnormalReminders) {
            abnormalEvents.add(new AbnormalEventDto(
                    String.valueOf(reminder.getId()),
                    "REMINDER",
                    reminder.getTitle(),
                    reminder.getContent(),
                    reminder.getPriority().name(),
                    reminder.getCreatedAt(),
                    reminder.getStatus().name(),
                    reminder.getUser().getId()
            ));
        }
        
        // 按创建时间倒序排序
        abnormalEvents.sort((a, b) -> b.time().compareTo(a.time()));
        abnormalEvents = abnormalEvents.stream().limit(10).collect(Collectors.toList());

        // 高风险患者列表（基于实时检测）
        List<HighRiskMemberDto> highRiskMembers = new ArrayList<>();
        for (User user : viewableUsers) {
            // 检查是否有最近的异常日志（基于实时检测）
            boolean hasAbnormal = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(user.getId(), com.healthfamily.domain.constant.HealthLogType.VITALS)
                    .stream()
                    .filter(l -> !l.getLogDate().isBefore(start))
                    .anyMatch(l -> checkAnomaly(user.getId(), safeParse(l.getContentJson())));
            
            if (!hasAbnormal) continue;
            
            // 获取健康标签和过敏信息
            List<String> tags = new ArrayList<>();
            Profile profile = profileRepository.findById(user.getId()).orElse(null);
            if (profile != null) {
                try {
                    if (profile.getHealthTags() != null && !profile.getHealthTags().isBlank()) {
                        List<String> healthTags = objectMapper.readValue(profile.getHealthTags(), STRING_LIST_TYPE);
                        if (healthTags != null) tags.addAll(healthTags);
                    }
                    if (profile.getAllergies() != null && !profile.getAllergies().isBlank()) {
                        List<String> allergies = objectMapper.readValue(profile.getAllergies(), STRING_LIST_TYPE);
                        if (allergies != null) {
                            tags.addAll(allergies.stream().map(a -> "过敏:" + a).collect(Collectors.toList()));
                        }
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
            
            // 如果没有标签，添加"高风险"标签
            if (tags.isEmpty()) {
                tags.add("高风险");
            }
            
            LocalDateTime lastAbnormalTime = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(user.getId(), com.healthfamily.domain.constant.HealthLogType.VITALS)
                    .stream()
                    .filter(l -> !l.getLogDate().isBefore(start))
                    .filter(l -> checkAnomaly(user.getId(), safeParse(l.getContentJson())))
                    .findFirst()
                    .map(l -> l.getLogDate().atStartOfDay())
                    .orElse(LocalDateTime.now());
            
            highRiskMembers.add(new HighRiskMemberDto(
                    user.getId(),
                    family.getId(),
                    user.getNickname(),
                    family.getName(),
                    tags,
                    lastAbnormalTime,
                    readAvatar(user.getId()),
                    null,
                    null,
                    null,
                    null,
                    false
            ));
        }
        
        // 按最近异常时间倒序排序，取前10个
        highRiskMembers.sort((a, b) -> {
            if (a.lastAbnormalTime() == null && b.lastAbnormalTime() == null) return 0;
            if (a.lastAbnormalTime() == null) return 1;
            if (b.lastAbnormalTime() == null) return -1;
            return b.lastAbnormalTime().compareTo(a.lastAbnormalTime());
        });
        highRiskMembers = highRiskMembers.stream().limit(10).collect(Collectors.toList());

        String summary;
        if (Boolean.TRUE.equals(useAi)) {
            int unsharedMembers = Math.max(0, totalMembers - sharedMembers);
            StringBuilder sb = new StringBuilder();
            sb.append("本周家庭成员").append(totalMembers).append("人，开启数据共享").append(sharedMembers).append("人");
            if (unsharedMembers > 0) sb.append("，未共享").append(unsharedMembers).append("人");
            sb.append("。近7日体征记录").append(totalLogs).append("条");
            if (!abnormalEvents.isEmpty()) {
                sb.append("，异常事件").append(abnormalEvents.size()).append("条");
            } else {
                sb.append("，未检测到明显异常");
            }
            if (!highRiskMembers.isEmpty()) {
                List<String> names = highRiskMembers.stream()
                        .map(HighRiskMemberDto::nickname)
                        .filter(Objects::nonNull)
                        .limit(3)
                        .collect(Collectors.toList());
                if (!names.isEmpty()) {
                    sb.append("。重点关注：").append(String.join("、", names));
                }
            }
            if (todayFollowupsCount > 0) {
                sb.append("。今日待随访").append(todayFollowupsCount).append("项");
            }
            if (!abnormalEvents.isEmpty() || !highRiskMembers.isEmpty()) {
                sb.append("。建议优先复核异常指标并安排复诊");
            } else {
                sb.append("。建议保持规律监测，持续观察指标变化");
            }
            summary = sb.toString();
        } else {
            summary = "成员近7日体征记录已汇总，请点击'AI生成摘要'按钮获取智能分析。";
        }

        // 数据简报
        LocalDate lastWeek = today.minusWeeks(1);
        long weeklyConsultations = consultationSessionRepository.countByDoctorAndCreatedAtAfter(requester, lastWeek.atStartOfDay());
        long newPatients = familyDoctorRepository.countByDoctorAndCreatedAtAfter(requester, lastWeek.atStartOfDay());
        
        // 计算改善率：基于当前家庭下，由当前医生创建的所有健康计划的平均完成度
        List<HealthPlan> plans = healthPlanRepository.findByDoctorAndFamilyOrderByCreatedAtDesc(requester, family);
        int improvementRate = 0;
        if (plans != null && !plans.isEmpty()) {
            double totalCompletion = plans.stream()
                    .filter(p -> p.getCompletionRate() != null)
                    .mapToDouble(p -> p.getCompletionRate().doubleValue())
                    .sum();
            improvementRate = (int) (totalCompletion / plans.size());
        }

        return new DoctorViewResponse(
                familyId,
                summary,
                telemetry,
                (int) pendingConsultationsCount,
                (int) pendingPlansCount,
                (int) todayFollowupsCount,
                abnormalEvents,
                highRiskMembers,
                (int) weeklyConsultations,
                (int) newPatients,
                improvementRate
        );
    }

    private Family ensureAdmin(Long userId, Long familyId) {
        Family f = ensureMember(userId, familyId);
        com.healthfamily.domain.entity.FamilyMember member = familyMemberRepository.findByFamilyAndUser(f, userRepository.findById(userId).orElseThrow()).orElseThrow();
        if (Boolean.FALSE.equals(member.getAdmin())) throw new BusinessException(40304, "仅管理员可执行此操作");
        return f;
    }

    private Family ensureMember(Long userId, Long familyId) {
        Family f = familyRepository.findById(familyId).orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User u = userRepository.findById(userId).orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        familyMemberRepository.findByFamilyAndUser(f, u).orElseThrow(() -> new BusinessException(40301, "无权访问该家庭"));
        return f;
    }

    private boolean canViewMemberData(Long targetUserId) {
        return profileRepository.findById(targetUserId).map(p -> {
            String prefs = p.getPreferences();
            if (prefs == null || prefs.isBlank()) return false;
            try {
                Map<?, ?> map = objectMapper.readValue(prefs, Map.class);
                Object share = map.get("shareToFamily");
                return share instanceof Boolean ? (Boolean) share : false;
            } catch (Exception e) {
                return false;
            }
        }).orElse(false);
    }

    private boolean canViewMemberData(Long targetUserId, boolean forDoctor) {
        return profileRepository.findById(targetUserId).map(p -> {
            String prefs = p.getPreferences();
            if (prefs == null || prefs.isBlank()) return false;
            try {
                Map<?, ?> map = objectMapper.readValue(prefs, Map.class);
                Object share = map.get(forDoctor ? "shareToDoctor" : "shareToFamily");
                return share instanceof Boolean ? (Boolean) share : false;
            } catch (Exception e) {
                return false;
            }
        }).orElse(false);
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

    private Map<String, Object> safeParse(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * 将字符串形式的风险等级转换为可比较的权重
     */
    private int mapRiskLevelOrder(String riskLevel) {
        if (riskLevel == null) {
            return 0;
        }
        return switch (riskLevel) {
            case "CRITICAL" -> 3;
            case "WARNING" -> 2;
            case "NOTICE" -> 1;
            default -> 0;
        };
    }

    /**
     * 比较两个风险等级的高低（CRITICAL > WARNING > NOTICE）
     */
    private int compareRiskLevel(String levelA, String levelB) {
        int a = mapRiskLevelOrder(levelA);
        int b = mapRiskLevelOrder(levelB);
        return Integer.compare(a, b);
    }

    @Override
    /**
     * 获取
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param patientUserId 业务对象唯一标识
     * @return 业务返回结果
     */
    public PatientDetailResponse getPatientDetail(Long doctorId, Long familyId, Long patientUserId) {
        // 验证医生权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        boolean isDoctor = familyDoctorRepository.findByFamily(family).stream()
                .anyMatch(fd -> Objects.equals(fd.getDoctor().getId(), doctorId));
        if (!isDoctor) {
            throw new BusinessException(40301, "无权访问该家庭");
        }

        // 验证患者是否在该家庭中
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        com.healthfamily.domain.entity.FamilyMember member = familyMemberRepository.findByFamilyAndUser(family, patient)
                .orElseThrow(() -> new BusinessException(40404, "该用户不是家庭成员"));

        // 获取基础信息
        Profile profile = profileRepository.findById(patientUserId).orElse(null);
        Integer age = null;
        String sex = null;
        if (profile != null && profile.getBirthday() != null) {
            age = java.time.Period.between(profile.getBirthday(), LocalDate.now()).getYears();
            sex = profile.getSex() != null ? profile.getSex().name() : null;
        }

        // 获取最近体质测评
        PatientDetailResponse.AssessmentSummary latestAssessment = null;
        List<ConstitutionAssessment> assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(patient);
        if (!assessments.isEmpty()) {
            ConstitutionAssessment latest = assessments.get(0);
            Map<String, Double> scores = parseScores(latest.getScoreVector());
            double confidence = calculateConfidence(scores);
            latestAssessment = new PatientDetailResponse.AssessmentSummary(
                    latest.getId(),
                    latest.getPrimaryType(),
                    confidence,
                    latest.getCreatedAt()
            );
        }

        // 获取日志统计（近7天和近30天）
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        LocalDate thirtyDaysAgo = today.minusDays(29);
        
        Map<String, Integer> logStatistics = new HashMap<>();
        for (HealthLogType type : HealthLogType.values()) {
            List<HealthLog> logs7d = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(patientUserId, type)
                    .stream()
                    .filter(l -> !l.getLogDate().isBefore(sevenDaysAgo))
                    .collect(Collectors.toList());
            List<HealthLog> logs30d = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(patientUserId, type)
                    .stream()
                    .filter(l -> !l.getLogDate().isBefore(thirtyDaysAgo))
                    .collect(Collectors.toList());
            
            String key = type.name().toLowerCase();
            logStatistics.put(key + "_7d", logs7d.size());
            logStatistics.put(key + "_30d", logs30d.size());
        }

        // 获取近期建议（近30天）
        List<PatientDetailResponse.RecommendationSummary> recentRecommendations = new ArrayList<>();
        LocalDate startDate = today.minusDays(29);
        List<AiRecommendation> recommendations = aiRecommendationRepository.findByUser_IdAndForDateBetween(patientUserId, startDate, today)
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());
        
        for (AiRecommendation rec : recommendations) {
            recentRecommendations.add(new PatientDetailResponse.RecommendationSummary(
                    rec.getId(),
                    rec.getTitle(),
                    rec.getCategory() != null ? rec.getCategory().name() : "OTHER",
                    rec.getPriority() != null ? rec.getPriority().name() : "MEDIUM",
                    rec.getCreatedAt()
            ));
        }

        // 获取健康标签和风险等级
        List<String> healthTags = new ArrayList<>();
        if (profile != null) {
            try {
                if (profile.getHealthTags() != null && !profile.getHealthTags().isBlank()) {
                    List<String> tags = objectMapper.readValue(profile.getHealthTags(), STRING_LIST_TYPE);
                    if (tags != null) healthTags.addAll(tags);
                }
                if (profile.getAllergies() != null && !profile.getAllergies().isBlank()) {
                    List<String> allergies = objectMapper.readValue(profile.getAllergies(), STRING_LIST_TYPE);
                    if (allergies != null) {
                        healthTags.addAll(allergies.stream().map(a -> "过敏:" + a).collect(Collectors.toList()));
                    }
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        // 计算风险等级（基于最近异常日志）
        String riskLevel = "LOW";
        List<HealthLog> abnormalLogs = healthLogRepository.findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(patientUserId)
                .stream()
                .filter(l -> !l.getLogDate().isBefore(today.minusDays(30)))
                .collect(Collectors.toList());
        if (abnormalLogs.size() > 10) {
            riskLevel = "HIGH";
        } else if (abnormalLogs.size() > 3) {
            riskLevel = "MEDIUM";
        }

        return new PatientDetailResponse(
                patient.getId(),
                patient.getNickname(),
                patient.getPhone(),
                readAvatar(patientUserId),
                age,
                sex,
                family.getName(),
                member.getRelation(),
                patient.getRole() != null ? patient.getRole().name() : null,
                latestAssessment,
                logStatistics,
                recentRecommendations,
                healthTags,
                riskLevel
        );
    }

    @Override
    @Transactional
    /**
     * 更新
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param patientUserId 业务对象唯一标识
     * @param tags 业务参数
     * @return 无
     */
    public void updatePatientTags(Long doctorId, Long familyId, Long patientUserId, List<String> tags) {
        // 验证权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);

        // 验证患者
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        familyMemberRepository.findByFamilyAndUser(family, patient)
                .orElseThrow(() -> new BusinessException(40404, "该用户不是家庭成员"));

        Profile profile = profileRepository.findById(patientUserId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUser(patient);
            return p;
        });

        try {
            profile.setHealthTags(objectMapper.writeValueAsString(tags));
            if (profile.getUpdatedAt() == null) profile.setUpdatedAt(LocalDateTime.now());
            profileRepository.save(profile);
        } catch (Exception e) {
            throw new BusinessException(500, "保存标签失败: " + e.getMessage());
        }
    }

    @Override
    /**
     * 执行业务操作
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param patientUserId 业务对象唯一标识
     * @param isImportant 业务参数
     * @return 无
     */
    public void togglePatientImportant(Long doctorId, Long familyId, Long patientUserId, Boolean isImportant) {
        // 暂时不实现，可以使用前端本地存储或后续扩展数据库表
        // 验证权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        boolean isDoctor = familyDoctorRepository.findByFamily(family).stream()
                .anyMatch(fd -> Objects.equals(fd.getDoctor().getId(), doctorId));
        if (!isDoctor) {
            throw new BusinessException(40301, "无权访问该家庭");
        }
        // TODO: 实现重点管理标记的存储逻辑
    }

    @Override
    /**
     * 生成
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public com.healthfamily.web.dto.HealthPlanGenerationResponse generateAiHealthPlan(Long doctorId, Long familyId, com.healthfamily.web.dto.HealthPlanGenerationRequest request) {
        // 1. 验证权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);

        User patient = userRepository.findById(request.patientUserId())
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        familyMemberRepository.findByFamilyAndUser(family, patient)
                .orElseThrow(() -> new BusinessException(40404, "该用户不是家庭成员"));

        return generateAiPlanInternal(patient, request.focusArea(), request.additionalRequirements());
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @return 业务返回结果
     */
    public List<com.healthfamily.web.dto.BatchHealthPlanResponse> batchGenerateAiHealthPlans(Long doctorId, Long familyId) {
        // 1. 验证权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);

        // 2. 查找家庭成员
        List<com.healthfamily.domain.entity.FamilyMember> members = familyMemberRepository.findByFamily(family);
        List<com.healthfamily.web.dto.BatchHealthPlanResponse> results = new ArrayList<>();

        // 3. 智能筛选与评分
        // 评分规则：有异常日志(+10)，有健康标签(+5)，有体质测评(+3)，有近期日志(+1)
        List<UserScore> candidates = new ArrayList<>();
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        for (com.healthfamily.domain.entity.FamilyMember member : members) {
            User user = member.getUser();
            if (user == null) continue;

            int score = 0;
            StringBuilder reason = new StringBuilder();

            // A. 检查异常日志 (近30天)
            boolean hasAbnormal = healthLogRepository.findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(user.getId())
                    .stream()
                    .anyMatch(l -> !l.getLogDate().isBefore(thirtyDaysAgo));
            if (hasAbnormal) {
                score += 10;
                reason.append("存在异常指标; ");
            }

            // B. 检查健康标签 (慢病人群)
            Profile profile = profileRepository.findById(user.getId()).orElse(null);
            boolean hasTags = false;
            if (profile != null && profile.getHealthTags() != null && !profile.getHealthTags().isBlank()) {
                try {
                    List<String> tags = objectMapper.readValue(profile.getHealthTags(), STRING_LIST_TYPE_REF);
                    if (!tags.isEmpty()) {
                        hasTags = true;
                        score += 5;
                        reason.append("标记为重点关注人群(").append(String.join(",", tags)).append("); ");
                    }
                } catch (Exception e) {}
            }

            // C. 检查最近体质测评
            boolean hasAssessment = !assessmentRepository.findByUserOrderByCreatedAtDesc(user).isEmpty();
            if (hasAssessment) {
                score += 3;
            }

            // D. 检查近期活跃 (有日志)
            boolean hasRecentLogs = healthLogRepository.findByUserAndLogDateBetweenOrderByLogDateDesc(user, thirtyDaysAgo, LocalDate.now())
                    .stream().findAny().isPresent();
            if (hasRecentLogs) {
                score += 1;
            }

            // 只要有任何相关数据，都列为候选（不仅仅是高风险）
            if (score > 0) {
                candidates.add(new UserScore(user, score, reason.toString()));
            }
        }

        // 4. 按优先级排序并生成计划 (限制前5名以防超时)
        candidates.sort((a, b) -> b.score - a.score);

        for (UserScore candidate : candidates.stream().limit(5).collect(Collectors.toList())) {
            try {
                User user = candidate.user;
                String aiInstruction = "请基于患者的健康数据进行全面分析。";
                if (candidate.reason.contains("异常指标")) {
                    aiInstruction += "患者近期存在异常指标，请生成紧迫的干预计划。";
                } else if (candidate.reason.contains("重点关注")) {
                    aiInstruction += "患者为慢病或重点关注人群，请生成长期的健康管理与改善计划。";
                } else {
                    aiInstruction += "请根据患者的体质和日常数据，生成预防性健康促进计划。";
                }

                // 调用内部生成逻辑
                com.healthfamily.web.dto.HealthPlanGenerationResponse plan = generateAiPlanInternal(user, "", aiInstruction);
                results.add(new com.healthfamily.web.dto.BatchHealthPlanResponse(
                    user.getId(),
                    user.getNickname(),
                    plan
                ));
            } catch (Exception e) {
                log.error("Failed to generate batch plan for user {}", candidate.user.getId(), e);
            }
        }
        
        return results;
    }

    private record UserScore(User user, int score, String reason) {}

    private com.healthfamily.web.dto.HealthPlanGenerationResponse generateAiPlanInternal(User patient, String focusArea, String additionalRequirements) {
        // 2. 收集患者健康数据
        StringBuilder context = new StringBuilder();
        
        // 基础信息
        Profile profile = profileRepository.findById(patient.getId()).orElse(null);
        if (profile != null) {
            context.append("【基础信息】\n");
            if (profile.getBirthday() != null) {
                int age = java.time.Period.between(profile.getBirthday(), LocalDate.now()).getYears();
                context.append("年龄: ").append(age).append("岁; ");
            }
            if (profile.getSex() != null) context.append("性别: ").append(profile.getSex() == Sex.M ? "男" : "女").append("; ");
            if (profile.getHeightCm() != null) context.append("身高: ").append(profile.getHeightCm()).append("cm; ");
            if (profile.getWeightKg() != null) context.append("体重: ").append(profile.getWeightKg()).append("kg; ");
            context.append("\n");

            // 健康标签
            try {
                if (profile.getHealthTags() != null && !profile.getHealthTags().isBlank()) {
                    List<String> tags = objectMapper.readValue(profile.getHealthTags(), STRING_LIST_TYPE_REF);
                    context.append("健康标签: ").append(String.join(", ", tags)).append("\n");
                }
            } catch (Exception e) {}
        }

        // 最近体质
        List<ConstitutionAssessment> assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(patient);
        if (!assessments.isEmpty()) {
            context.append("【中医体质】\n").append(assessments.get(0).getPrimaryType()).append("\n");
        }

        // 最近异常记录 (近30天)
        List<HealthLog> abnormalLogs = healthLogRepository.findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(patient.getId())
                .stream()
                .filter(l -> !l.getLogDate().isBefore(LocalDate.now().minusDays(30)))
                .limit(5)
                .collect(Collectors.toList());
        if (!abnormalLogs.isEmpty()) {
            context.append("【近期异常指标】\n");
            for (HealthLog log : abnormalLogs) {
                context.append(log.getLogDate()).append(" ").append(log.getType()).append(": ").append(log.getContentJson()).append("\n");
            }
        }

        // 3. 构建 Prompt
        String systemPrompt = """
            你是一个专业的家庭医生助手。请根据患者的健康数据和医生要求，生成一个结构化的健康干预计划。
            
            输出必须是严格的JSON格式，包含以下字段：
            - title: 计划标题 (String)
            - description: 计划详细描述 (String)
            - type: 计划类型 (Enum: BLOOD_PRESSURE_FOLLOWUP, DIET_MANAGEMENT, EXERCISE_PRESCRIPTION, MEDICATION_MANAGEMENT, WEIGHT_MANAGEMENT, OTHER)
            - frequencyType: 执行频率类型 (Enum: DAILY, WEEKLY, MONTHLY)
            - frequencyValue: 频率数值 (Integer, 如每周3次则为3)
            - targetIndicators: 目标指标 (JSON String, e.g. "{\\"systolic\\": \\"<130\\"}")
            - reminderStrategy: 提醒策略 (JSON String, e.g. "{\\"time\\": \\"09:00\\", \\"channels\\": [\\"APP\\"]}")
            - reasoning: 生成理由 (String)
            
            不要包含Markdown代码块标记（如 ```json），直接返回JSON字符串。
            """;

        String userPrompt = String.format("""
            患者数据：
            %s
            
            医生侧重/要求：
            %s
            %s
            
            请生成健康计划。
            """, 
            context.toString(), 
            focusArea != null && !focusArea.isBlank() ? "关注领域: " + focusArea : "请自动分析最紧迫的健康问题",
            additionalRequirements != null ? "额外要求: " + additionalRequirements : ""
        );

        // 4. 调用 AI
        try {
            Prompt prompt = new Prompt(List.of(
                new org.springframework.ai.chat.messages.SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
            ));
            
            String jsonResponse = chatModel.call(prompt).getResult().getOutput().getContent();
            
            // 清理可能的 Markdown 标记
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
            }
            if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.substring(3);
            }
            if (jsonResponse.endsWith("```")) {
                jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
            }
            jsonResponse = jsonResponse.trim();

            // 5. 解析并返回
            Map<String, Object> map = objectMapper.readValue(jsonResponse, MAP_TYPE_REF);
            
            return new com.healthfamily.web.dto.HealthPlanGenerationResponse(
                (String) map.getOrDefault("title", "健康计划"),
                (String) map.getOrDefault("description", ""),
                (String) map.getOrDefault("type", "OTHER"),
                (String) map.getOrDefault("frequencyType", "DAILY"),
                map.get("frequencyValue") instanceof Number ? ((Number) map.get("frequencyValue")).intValue() : 1,
                map.get("targetIndicators") instanceof String ? (String) map.get("targetIndicators") : objectMapper.writeValueAsString(map.getOrDefault("targetIndicators", Map.of())),
                map.get("reminderStrategy") instanceof String ? (String) map.get("reminderStrategy") : objectMapper.writeValueAsString(map.getOrDefault("reminderStrategy", Map.of())),
                (String) map.getOrDefault("reasoning", "")
            );
            
        } catch (Exception e) {
            log.error("AI生成健康计划失败", e);
            throw new BusinessException(500, "AI生成计划失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    /**
     * 查询列表
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param patientUserId 业务对象唯一标识
     * @return 业务返回结果
     */
    public List<DoctorNoteResponse> listDoctorNotes(Long doctorId, Long familyId, Long patientUserId) {
        // 验证医生权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);
        
        // 验证患者是否存在
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        familyMemberRepository.findByFamilyAndUser(family, patient)
                .orElseThrow(() -> new BusinessException(40404, "该用户不是家庭成员"));
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        
        List<DoctorNote> notes = doctorNoteRepository.findByPatientAndDoctorOrderByCreatedAtDesc(patient, doctor);
        return notes.stream()
                .map(this::toDoctorNoteResponse)
                .collect(Collectors.toList());
    }

    @Override
    /**
     * 获取
     * @param doctorId 医生唯一标识
     * @param noteId 业务对象唯一标识
     * @return 业务返回结果
     */
    public DoctorNoteResponse getDoctorNote(Long doctorId, Long noteId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        DoctorNote note = doctorNoteRepository.findByIdAndDoctor(noteId, doctor)
                .orElseThrow(() -> new BusinessException(40405, "病历记录不存在或无权限访问"));
        return toDoctorNoteResponse(note);
    }

    @Override
    @Transactional
    /**
     * 创建
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param patientUserId 业务对象唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public DoctorNoteResponse createDoctorNote(Long doctorId, Long familyId, Long patientUserId, DoctorNoteRequest request) {
        // 验证医生权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);
        
        // 验证患者是否存在
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        familyMemberRepository.findByFamilyAndUser(family, patient)
                .orElseThrow(() -> new BusinessException(40404, "该用户不是家庭成员"));
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        
        DoctorNote note = DoctorNote.builder()
                .doctor(doctor)
                .patient(patient)
                .family(family)
                .consultationDate(request.consultationDate())
                .chiefComplaint(request.chiefComplaint())
                .pastHistory(request.pastHistory())
                .medication(request.medication())
                .lifestyleAssessment(request.lifestyleAssessment())
                .diagnosisOpinion(request.diagnosisOpinion())
                .followupSuggestion(request.followupSuggestion())
                .content(request.content())
                .build();
        
        DoctorNote saved = doctorNoteRepository.save(note);
        return toDoctorNoteResponse(saved);
    }

    @Override
    @Transactional
    /**
     * 更新
     * @param doctorId 医生唯一标识
     * @param noteId 业务对象唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public DoctorNoteResponse updateDoctorNote(Long doctorId, Long noteId, DoctorNoteRequest request) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        DoctorNote note = doctorNoteRepository.findByIdAndDoctor(noteId, doctor)
                .orElseThrow(() -> new BusinessException(40405, "病历记录不存在或无权限访问"));
        
        note.setConsultationDate(request.consultationDate());
        note.setChiefComplaint(request.chiefComplaint());
        note.setPastHistory(request.pastHistory());
        note.setMedication(request.medication());
        note.setLifestyleAssessment(request.lifestyleAssessment());
        note.setDiagnosisOpinion(request.diagnosisOpinion());
        note.setFollowupSuggestion(request.followupSuggestion());
        note.setContent(request.content());
        
        DoctorNote saved = doctorNoteRepository.save(note);
        return toDoctorNoteResponse(saved);
    }

    @Override
    @Transactional
    /**
     * 删除
     * @param doctorId 医生唯一标识
     * @param noteId 业务对象唯一标识
     * @return 无
     */
    public void deleteDoctorNote(Long doctorId, Long noteId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        DoctorNote note = doctorNoteRepository.findByIdAndDoctor(noteId, doctor)
                .orElseThrow(() -> new BusinessException(40405, "病历记录不存在或无权限访问"));
        doctorNoteRepository.delete(note);
    }

    /**
     * 确保医生有权限访问该家庭
     */
    private void ensureDoctorAccess(Long doctorId, Family family) {
        boolean isDoctor = familyDoctorRepository.findByFamily(family).stream()
                .anyMatch(fd -> Objects.equals(fd.getDoctor().getId(), doctorId));
        if (!isDoctor) {
            throw new BusinessException(40301, "无权访问该家庭");
        }
    }

    /**
     * 将DoctorNote实体转换为DTO
     */
    private DoctorNoteResponse toDoctorNoteResponse(DoctorNote note) {
        return new DoctorNoteResponse(
                note.getId(),
                note.getDoctor().getId(),
                note.getDoctor().getNickname(),
                note.getPatient().getId(),
                note.getPatient().getNickname(),
                note.getFamily().getId(),
                note.getFamily().getName(),
                note.getConsultationDate(),
                note.getChiefComplaint(),
                note.getPastHistory(),
                note.getMedication(),
                note.getLifestyleAssessment(),
                note.getDiagnosisOpinion(),
                note.getFollowupSuggestion(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    private double calculateConfidence(Map<String, Double> scores) {
        if (scores == null || scores.isEmpty()) return 0d;
        List<Double> vals = new ArrayList<>(scores.values());
        vals.sort(Comparator.reverseOrder());
        double top = vals.get(0);
        double second = vals.size() > 1 ? vals.get(1) : 0d;
        double gap = Math.max(0d, top - second);
        double gapFactor = Math.min(1d, gap / 30d);
        double countFactor = Math.min(1d, vals.size() / 9d);
        double conf = Math.min(1d, 0.6 * gapFactor + 0.4 * countFactor);
        return Math.round(conf * 100d) / 100d;
    }

    private Map<String, Double> parseScores(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    @Override
    @Deprecated // 已迁移至DoctorMonitoringService，请使用新的增强版接口
    /**
     * 获取
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @return 业务返回结果
     */
    public MonitoringDataResponse getMonitoringData(Long doctorId, Long familyId) {
        // 验证医生权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);
        
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(30);  // 近30天
        
        // 获取家庭成员列表
        List<com.healthfamily.domain.entity.FamilyMember> members = familyMemberRepository.findByFamily(family);
        List<User> viewableUsers = new ArrayList<>();
        for (com.healthfamily.domain.entity.FamilyMember m : members) {
            User u = m.getUser();
            if (u == null || !canViewMemberData(u.getId())) continue;
            viewableUsers.add(u);
        }
        
        // 1. 构建趋势数据（按日期统计异常事件数量）
        Map<String, Integer> dateCountMap = new HashMap<>();
        Map<String, Integer> dateAlertCountMap = new HashMap<>();
        Map<String, Integer> dateReminderCountMap = new HashMap<>();
        
        // 从Alert中获取异常事件
        List<HealthAlert> alerts = healthAlertRepository.findByFamily_IdOrderByCreatedAtDesc(familyId)
                .stream()
                .filter(a -> a.getStatus() == AlertStatus.PENDING || a.getStatus() == AlertStatus.ESCALATED)
                .filter(a -> !a.getCreatedAt().toLocalDate().isBefore(start))
                .collect(Collectors.toList());
        
        for (HealthAlert alert : alerts) {
            String dateStr = alert.getCreatedAt().toLocalDate().toString();
            dateCountMap.put(dateStr, dateCountMap.getOrDefault(dateStr, 0) + 1);
            dateAlertCountMap.put(dateStr, dateAlertCountMap.getOrDefault(dateStr, 0) + 1);
        }
        
        // 从Reminder中获取异常提醒
        List<com.healthfamily.domain.entity.HealthReminder> reminders = healthReminderRepository.findByFamily_IdOrderByScheduledTimeAsc(familyId)
                .stream()
                .filter(r -> r.getStatus() == ReminderStatus.PENDING || r.getStatus() == ReminderStatus.SENT)
                .filter(r -> r.getType() == com.healthfamily.domain.constant.ReminderType.ABNORMAL)
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().toLocalDate().isBefore(start))
                .collect(Collectors.toList());
        
        for (com.healthfamily.domain.entity.HealthReminder reminder : reminders) {
            String dateStr = reminder.getCreatedAt().toLocalDate().toString();
            dateCountMap.put(dateStr, dateCountMap.getOrDefault(dateStr, 0) + 1);
            dateReminderCountMap.put(dateStr, dateReminderCountMap.getOrDefault(dateStr, 0) + 1);
        }
        
        // 构建趋势点列表（填充缺失的日期）
        List<MonitoringDataResponse.AbnormalEventTrendPoint> trendData = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            trendData.add(new MonitoringDataResponse.AbnormalEventTrendPoint(
                    dateStr,
                    dateCountMap.getOrDefault(dateStr, 0),
                    dateAlertCountMap.getOrDefault(dateStr, 0),
                    dateReminderCountMap.getOrDefault(dateStr, 0)
            ));
        }
        
        // 2. 构建事件类型统计（饼图数据）
        Map<String, Integer> eventTypeStats = new HashMap<>();
        for (HealthAlert alert : alerts) {
            String key = "ALERT_" + alert.getMetric();
            eventTypeStats.put(key, eventTypeStats.getOrDefault(key, 0) + 1);
        }
        eventTypeStats.put("REMINDER", reminders.size());
        
        // 3. 构建预警列表
        List<MonitoringAlertDto> monitoringAlerts = new ArrayList<>();
        
        // 添加Alert类型的预警
        for (HealthAlert alert : alerts) {
            User user = alert.getUser();
            monitoringAlerts.add(new MonitoringAlertDto(
                    alert.getId(),
                    "ALERT",
                    alert.getMetric() + "异常预警",
                    String.format("%s指标值为%.2f，超过阈值%.2f", alert.getMetric(), alert.getValue(), alert.getThreshold()),
                    user.getId(),
                    user.getNickname(),
                    readAvatar(user.getId()),
                    family.getId(),
                    family.getName(),
                    alert.getSeverity().name(),
                    alert.getStatus().name(),
                    alert.getMetric(),
                    alert.getValue(),
                    alert.getThreshold(),
                    alert.getCreatedAt(),
                    alert.getHandledAt(),
                    alert.getId(),
                    null
            ));
        }
        
        // 添加Reminder类型的预警
        for (com.healthfamily.domain.entity.HealthReminder reminder : reminders) {
            User user = reminder.getUser();
            monitoringAlerts.add(new MonitoringAlertDto(
                    reminder.getId(),
                    "REMINDER",
                    reminder.getTitle(),
                    reminder.getContent(),
                    user.getId(),
                    user.getNickname(),
                    readAvatar(user.getId()),
                    family.getId(),
                    family.getName(),
                    reminder.getPriority() != null ? reminder.getPriority().name() : "MEDIUM",
                    reminder.getStatus().name(),
                    null,
                    null,
                    null,
                    reminder.getCreatedAt(),
                    null,
                    null,
                    reminder.getId()
            ));
        }
        
        // 按创建时间倒序排序
        monitoringAlerts.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
        
        // 4. 获取高风险患者列表（复用getDoctorView中的逻辑）
        List<HighRiskMemberDto> highRiskMembers = new ArrayList<>();
        for (User user : viewableUsers) {
            List<HealthLog> abnormalLogs = healthLogRepository.findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(user.getId())
                    .stream()
                    .filter(l -> !l.getLogDate().isBefore(today.minusDays(7)))
                    .collect(Collectors.toList());
            
            if (abnormalLogs.isEmpty()) continue;
            
            List<String> tags = new ArrayList<>();
            Profile profile = profileRepository.findById(user.getId()).orElse(null);
            if (profile != null) {
                try {
                    if (profile.getHealthTags() != null && !profile.getHealthTags().isBlank()) {
                        List<String> healthTags = objectMapper.readValue(profile.getHealthTags(), STRING_LIST_TYPE);
                        if (healthTags != null) tags.addAll(healthTags);
                    }
                    if (profile.getAllergies() != null && !profile.getAllergies().isBlank()) {
                        List<String> allergies = objectMapper.readValue(profile.getAllergies(), STRING_LIST_TYPE);
                        if (allergies != null) {
                            tags.addAll(allergies.stream().map(a -> "过敏:" + a).collect(Collectors.toList()));
                        }
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
            
            if (tags.isEmpty()) {
                tags.add("高风险");
            }
            
            LocalDateTime lastAbnormalTime = abnormalLogs.get(0).getLogDate().atStartOfDay();
            highRiskMembers.add(new HighRiskMemberDto(
                    user.getId(),
                    family.getId(),
                    user.getNickname(),
                    family.getName(),
                    tags,
                    lastAbnormalTime,
                    readAvatar(user.getId()),
                    null,
                    null,
                    null,
                    null,
                    false
            ));
        }
        
        highRiskMembers.sort((a, b) -> {
            if (a.lastAbnormalTime() == null && b.lastAbnormalTime() == null) return 0;
            if (a.lastAbnormalTime() == null) return 1;
            if (b.lastAbnormalTime() == null) return -1;
            return b.lastAbnormalTime().compareTo(a.lastAbnormalTime());
        });
        highRiskMembers = highRiskMembers.stream().limit(10).collect(Collectors.toList());
        
        return new MonitoringDataResponse(
                trendData,
                eventTypeStats,
                monitoringAlerts,
                highRiskMembers
        );
    }

    @Override
    @Deprecated // 已迁移至DoctorMonitoringService，请使用新的增强版接口
    @Transactional
    /**
     * 执行业务操作
     * @param doctorId 医生唯一标识
     * @param alertId 业务对象唯一标识
     * @return 无
     */
    public void markAlertAsHandled(Long doctorId, Long alertId) {
        HealthAlert alert = healthAlertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException(40407, "预警不存在"));
        
        // 验证医生权限
        if (alert.getFamily() != null) {
            ensureDoctorAccess(doctorId, alert.getFamily());
        }
        
        alert.setStatus(AlertStatus.ACKED);
        alert.setHandledAt(LocalDateTime.now());
        healthAlertRepository.save(alert);
    }

    @Override
    @Transactional
    /**
     * 工作台“高风险患者”一键标记已处理
     * @param doctorId 当前医生ID
     * @param request  处理请求（包含患者ID、处置方式和备注）
     */
    public void handleRisk(Long doctorId, HandleRiskRequest request) {
        if (request == null || request.getPatientId() == null) {
            throw new BusinessException(40001, "患者ID不能为空");
        }

        Long patientId = request.getPatientId();
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));

        // 获取医生管理的所有家庭ID
        List<FamilyDoctor> bindings = familyDoctorRepository.findByDoctor_Id(doctorId);
        if (bindings.isEmpty()) {
            throw new BusinessException(40301, "当前医生未绑定任何家庭");
        }
        java.util.Set<Long> familyIds = bindings.stream()
                .map(fd -> fd.getFamily() != null ? fd.getFamily().getId() : null)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // 找出该患者在医生所管理家庭下的所有未处理预警（PENDING / ESCALATED）
        List<HealthAlert> allAlertsForUser = healthAlertRepository.findByUserOrderByCreatedAtDesc(patient);
        LocalDateTime now = LocalDateTime.now();
        List<HealthAlert> targetAlerts = allAlertsForUser.stream()
                .filter(a -> a.getFamily() != null && familyIds.contains(a.getFamily().getId()))
                .filter(a -> a.getStatus() == AlertStatus.PENDING || a.getStatus() == AlertStatus.ESCALATED)
                .toList();

        if (targetAlerts.isEmpty()) {
            // 没有需要处理的预警，但仍然记录一次处置操作，方便审计
            AbnormalHandlingRecord record = AbnormalHandlingRecord.builder()
                    .alert(null)
                    .doctor(doctor)
                    .patient(patient)
                    .handlingAction(request.getHandlingMethod() != null ? request.getHandlingMethod() : "MANUAL")
                    .handlingContent(request.getNotes())
                    .handlingNote(request.getNotes())
                    .handledAt(now)
                    .followUpRequired(false)
                    .build();
            abnormalHandlingRecordRepository.save(record);
            return;
        }

        // 更新所有相关预警为已处理状态
        for (HealthAlert alert : targetAlerts) {
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setHandledAt(now);
            alert.setHandledBy(doctorId);

            if (request.getNotes() != null && !request.getNotes().isBlank()) {
                String existing = alert.getHandlingNote();
                if (existing == null || existing.isBlank()) {
                    alert.setHandlingNote(request.getNotes());
                } else {
                    alert.setHandlingNote(existing + "\n" + request.getNotes());
                }
            }
        }
        healthAlertRepository.saveAll(targetAlerts);

        // 以最近一条预警为主，创建一条处置记录
        HealthAlert primaryAlert = targetAlerts.get(0);
        AbnormalHandlingRecord record = AbnormalHandlingRecord.builder()
                .alert(primaryAlert)
                .doctor(doctor)
                .patient(patient)
                .handlingAction(request.getHandlingMethod() != null ? request.getHandlingMethod() : "MANUAL")
                .handlingContent(request.getNotes())
                .handlingNote(request.getNotes())
                .handledAt(now)
                .followUpRequired(false)
                .build();
        abnormalHandlingRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 基于近期日志与体质评估生成患者健康AI摘要（当前为Mock实现，便于前端演示）
     */
    public String generatePatientHealthSummary(Long doctorId, Long patientId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));

        // 基本信息：性别 + 年龄
        Profile profile = profileRepository.findById(patientId).orElse(null);
        String sexText = "未知性别";
        if (profile != null && profile.getSex() != null) {
            sexText = switch (profile.getSex()) {
                case M -> "男";
                case F -> "女";
                default -> "未知性别";
            };
        }

        Integer age = null;
        if (profile != null && profile.getBirthday() != null) {
            age = java.time.Period.between(profile.getBirthday(), java.time.LocalDate.now()).getYears();
        }
        String ageText = age != null && age > 0 ? age + "岁" : "年龄未知";

        // 最近 8 条健康日志
        List<HealthLog> recentLogsAll = healthLogRepository.findByUserOrderByLogDateDesc(patient);
        List<HealthLog> recentLogs = recentLogsAll.stream().limit(8).toList();

        // 提取体征类日志的血压/血糖等信息的简单统计
        int bpCount = 0;
        double systolicSum = 0.0;
        double diastolicSum = 0.0;
        int glucoseCount = 0;
        double glucoseSum = 0.0;

        List<String> logSnippets = new ArrayList<>();
        for (HealthLog log : recentLogs) {
            Map<String, Object> content = safeParse(log.getContentJson());
            StringBuilder sb = new StringBuilder();
            sb.append(log.getLogDate()).append(" ").append(log.getType().name()).append(" ");

            if (log.getType() == HealthLogType.VITALS && !content.isEmpty()) {
                Double systolic = extractDouble(content.get("systolic"));
                Double diastolic = extractDouble(content.get("diastolic"));
                Double glucose = extractDouble(content.getOrDefault("glucose", content.get("bloodSugar")));

                if (systolic != null || diastolic != null) {
                    bpCount++;
                    if (systolic != null) systolicSum += systolic;
                    if (diastolic != null) diastolicSum += diastolic;
                    sb.append("血压")
                            .append(systolic != null ? String.format("%.0f", systolic) : "--")
                            .append("/")
                            .append(diastolic != null ? String.format("%.0f", diastolic) : "--")
                            .append("mmHg ");
                }
                if (glucose != null) {
                    glucoseCount++;
                    glucoseSum += glucose;
                    sb.append(String.format("血糖%.1fmmol/L ", glucose));
                }
            } else {
                Object note = content.getOrDefault("note",
                        content.getOrDefault("text", content.getOrDefault("content", "")));
                if (note != null) {
                    String text = note.toString();
                    if (text.length() > 30) {
                        text = text.substring(0, 30) + "...";
                    }
                    sb.append(text);
                }
            }

            logSnippets.add(sb.toString().trim());
        }

        double avgSys = bpCount > 0 ? systolicSum / bpCount : 0.0;
        double avgDia = bpCount > 0 ? diastolicSum / bpCount : 0.0;
        double avgGlucose = glucoseCount > 0 ? glucoseSum / glucoseCount : 0.0;

        // 最近一次中医体质评估
        ConstitutionAssessment latestTcm = assessmentRepository.findByUserOrderByCreatedAtDesc(patient)
                .stream()
                .findFirst()
                .orElse(null);

        String tcmType = null;
        if (latestTcm != null && latestTcm.getPrimaryType() != null) {
            tcmType = latestTcm.getPrimaryType();
        }

        // Mock AI 摘要文本（尽量专业、100 字左右）
        StringBuilder summary = new StringBuilder();
        summary.append("患者（")
                .append(sexText)
                .append("，")
                .append(ageText)
                .append("）近期健康数据提示：");

        if (bpCount > 0) {
            summary.append(String.format("收缩压平均约%.0fmmHg，舒张压约%.0fmmHg，", avgSys, avgDia));
            if (avgSys >= 140 || avgDia >= 90) {
                summary.append("存在血压偏高趋势，需警惕心脑血管风险；");
            } else {
                summary.append("血压整体控制在可接受范围内；");
            }
        }

        if (glucoseCount > 0) {
            summary.append(String.format("空腹血糖平均约%.1fmmol/L，", avgGlucose));
            if (avgGlucose > 7.0) {
                summary.append("需关注糖代谢异常及饮食控制；");
            } else {
                summary.append("血糖水平基本平稳；");
            }
        }

        if (tcmType != null) {
            summary.append("中医体质偏向「").append(tcmType).append("」，建议结合祛湿、调和脾胃等干预；");
        }

        if (summary.length() < 40) {
            // 数据较少时给一个通用建议
            summary.append("当前可用数据有限，建议继续完善血压、血糖及生活方式记录，以便进行更精确评估。");
        } else {
            summary.append("总体建议保持规律作息、清淡饮食，适量运动，如血压或血糖持续异常应及时门诊随访。");
        }

        return summary.toString();
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 基于近期日志与体质评估生成患者健康AI摘要（流式，真实 LLM 调用）
     */
    public reactor.core.publisher.Flux<String> generatePatientHealthSummaryStream(Long doctorId, Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));

        // 最近 10 条健康日志
        List<HealthLog> recentLogsAll = healthLogRepository.findByUserOrderByLogDateDesc(patient);
        List<HealthLog> recentLogs = recentLogsAll.stream().limit(10).toList();

        // 最新一次中医体质评估
        ConstitutionAssessment latestTcm = assessmentRepository.findByUserOrderByCreatedAtDesc(patient)
                .stream()
                .findFirst()
                .orElse(null);

        // 拼接原始数据片段，供 LLM 阅读
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("患者ID: ").append(patientId).append("\n");
        dataBuilder.append("患者昵称: ").append(patient.getNickname()).append("\n\n");

        dataBuilder.append("【最近健康日志】（最多10条，按时间倒序）：\n");
        for (HealthLog log : recentLogs) {
            Map<String, Object> content = safeParse(log.getContentJson());
            dataBuilder.append("- 日期: ").append(log.getLogDate())
                    .append("，类型: ").append(log.getType().name())
                    .append("，内容: ").append(content).append("\n");
        }

        dataBuilder.append("\n【最新中医体质评估】:\n");
        if (latestTcm != null) {
            dataBuilder.append("主判体质: ").append(latestTcm.getPrimaryType())
                    .append("，打分向量(scoreVector): ").append(latestTcm.getScoreVector())
                    .append("，生成时间: ").append(latestTcm.getCreatedAt())
                    .append("\n");
        } else {
            dataBuilder.append("暂无体质评估记录。\n");
        }

        String systemPrompt = """
                你是一名专业的临床医生和家庭健康管理专家。
                下面会提供某位患者近期的健康日志数据以及中医体质评估结果。
                请你基于这些数据，生成一段不超过 120 字的中文健康摘要和建议，要求：
                1. 语言专业但亲切，适合直接展示给医生查看。
                2. 先用一句话概括当前主要风险或关注点，再给出 1-2 条简短建议。
                3. 不要出现“根据提供的信息”、“作为AI模型”等措辞，直接给出结论和建议。
                """;

        String userPrompt = """
                患者近期数据如下：
                %s
                
                请按照上述要求，用中文输出“健康摘要和建议”。
                """.formatted(dataBuilder);

        log.info("[AI] 正在调用 LLM 生成患者健康摘要, doctorId={}, patientId={}", doctorId, patientId);

        String legacyPrompt = systemPrompt + "\n\n" + userPrompt;
        StringBuilder fullResponse = new StringBuilder();

        reactor.core.publisher.Flux<String> stream = aiAssistantService.chatStream(legacyPrompt, patientId)
                .doOnSubscribe(s -> log.info("[AI] LLM 请求已发送"))
                .doOnNext(chunk -> {
                    fullResponse.append(chunk);
                    log.debug("[AI] LLM 响应分片: {}", chunk);
                })
                .doOnComplete(() -> {
                    String preview = fullResponse.length() > 300
                            ? fullResponse.substring(0, 300) + "..."
                            : fullResponse.toString();
                    log.info("[AI] LLM 响应汇总预览: {}", preview);
                })
                .doOnError(e -> log.error("[AI] LLM 调用失败: {}", e.getMessage(), e));

        return stream;
    }

    // ==================== 健康计划相关方法 ====================

    @Override
    @Transactional
    /**
     * 查询列表
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param patientUserId 业务对象唯一标识
     * @param status 业务参数
     * @param type 业务参数
     * @return 业务返回结果
     */
    public List<HealthPlanResponse> listHealthPlans(Long doctorId, Long familyId, Long patientUserId, String status, String type) {
        // 验证医生权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        
        List<HealthPlan> plans;
        if (patientUserId != null) {
            // 查询特定患者的计划
            User patient = userRepository.findById(patientUserId)
                    .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
            familyMemberRepository.findByFamilyAndUser(family, patient)
                    .orElseThrow(() -> new BusinessException(40404, "该用户不是家庭成员"));
            
            if (status != null && !status.isEmpty() && type != null && !type.isEmpty()) {
                plans = healthPlanRepository.findByPatientAndTypeAndStatusOrderByCreatedAtDesc(
                        patient, HealthPlanType.valueOf(type), HealthPlanStatus.valueOf(status));
            } else if (status != null && !status.isEmpty()) {
                plans = healthPlanRepository.findByPatientAndStatusOrderByCreatedAtDesc(
                        patient, HealthPlanStatus.valueOf(status));
            } else if (type != null && !type.isEmpty()) {
                plans = healthPlanRepository.findByDoctorAndPatientOrderByCreatedAtDesc(doctor, patient)
                        .stream()
                        .filter(p -> p.getType().name().equals(type))
                        .collect(Collectors.toList());
            } else {
                plans = healthPlanRepository.findByDoctorAndPatientOrderByCreatedAtDesc(doctor, patient);
            }
        } else {
            // 查询该家庭的所有计划
            plans = healthPlanRepository.findByDoctorAndFamilyOrderByCreatedAtDesc(doctor, family);
            
            // 过滤逻辑
            if (status != null && !status.isEmpty()) {
                plans = plans.stream()
                        .filter(p -> p.getStatus().name().equals(status))
                        .collect(Collectors.toList());
            }
            if (type != null && !type.isEmpty()) {
                plans = plans.stream()
                        .filter(p -> p.getType().name().equals(type))
                        .collect(Collectors.toList());
            }
        }
        
        return plans.stream()
                .map(this::toHealthPlanResponse)
                .collect(Collectors.toList());
    }

    @Override
    /**
     * 获取
     * @param doctorId 医生唯一标识
     * @param planId 计划唯一标识
     * @return 业务返回结果
     */
    public HealthPlanResponse getHealthPlan(Long doctorId, Long planId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        HealthPlan plan = healthPlanRepository.findByIdAndDoctor(planId, doctor)
                .orElseThrow(() -> new BusinessException(40408, "健康计划不存在或无权限访问"));
        return toHealthPlanResponse(plan);
    }

    @Override
    @Transactional
    /**
     * 创建
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public HealthPlanResponse createHealthPlan(Long doctorId, Long familyId, HealthPlanRequest request) {
        // 验证医生权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);
        
        // 验证患者是否存在
        User patient = userRepository.findById(request.patientUserId())
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        familyMemberRepository.findByFamilyAndUser(family, patient)
                .orElseThrow(() -> new BusinessException(40404, "该用户不是家庭成员"));
        
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        
        HealthPlan plan = HealthPlan.builder()
                .doctor(doctor)
                .patient(patient)
                .family(family)
                .type(HealthPlanType.valueOf(request.type()))
                .title(request.title())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .frequencyType(FrequencyType.valueOf(request.frequencyType()))
                .frequencyValue(request.frequencyValue())
                .frequencyDetail(request.frequencyDetail())
                .targetIndicators(request.targetIndicators())
                .reminderStrategy(request.reminderStrategy())
                .status(HealthPlanStatus.ACTIVE)
                .completionRate(BigDecimal.ZERO)
                .complianceRate(BigDecimal.ZERO)
                .build();
        
        HealthPlan saved = healthPlanRepository.save(plan);
        
        // 自动生成提醒任务
        generatePlanReminders(saved, request.reminderStrategy());
        
        return toHealthPlanResponse(saved);
    }

    private void generatePlanReminders(HealthPlan plan, String strategyJson) {
        if (strategyJson == null || strategyJson.isBlank()) return;
        
        try {
            Map<String, Object> strategy = objectMapper.readValue(strategyJson, MAP_TYPE);
            String timeStr = (String) strategy.getOrDefault("time", "09:00");
            Object channelsObj = strategy.getOrDefault("channels", List.of("APP"));
            String channel = "APP"; // Default
            if (channelsObj instanceof List && !((List<?>) channelsObj).isEmpty()) {
                channel = ((List<?>) channelsObj).get(0).toString();
            }
            
            java.time.LocalTime remindTime = java.time.LocalTime.parse(timeStr);
            
            List<LocalDateTime> scheduleTimes = new ArrayList<>();
            LocalDate current = plan.getStartDate();
            LocalDate end = plan.getEndDate() != null ? plan.getEndDate() : plan.getStartDate().plusMonths(1); // Default 1 month if no end date
            
            // Limit generation to avoid explosion (e.g. max 90 days)
            if (end.isAfter(current.plusDays(90))) {
                end = current.plusDays(90);
            }

            switch (plan.getFrequencyType()) {
                case DAILY -> {
                    while (!current.isAfter(end)) {
                        scheduleTimes.add(current.atTime(remindTime));
                        current = current.plusDays(1);
                    }
                }
                case WEEKLY -> {
                    // Simple implementation: reminder on the same day of week as start date
                    // Or parse frequencyDetail for specific days (e.g. "1,3,5")
                    // If frequencyValue > 1, maybe "Every X weeks"? Assuming "X times a week" needs detail.
                    // For MVP: If frequencyValue is set, assume "Every X days" roughly?
                    // Let's stick to: Every week on the StartDate's day of week.
                    // Improve: if frequencyDetail has days (1-7), use them.
                    List<Integer> days = new ArrayList<>();
                    if (plan.getFrequencyDetail() != null && !plan.getFrequencyDetail().isBlank()) {
                        try {
                            String[] parts = plan.getFrequencyDetail().split(",");
                            for (String p : parts) days.add(Integer.parseInt(p.trim()));
                        } catch (Exception e) {}
                    }
                    
                    if (days.isEmpty()) {
                        // Default to start date's day of week
                        days.add(current.getDayOfWeek().getValue());
                    }
                    
                    while (!current.isAfter(end)) {
                        if (days.contains(current.getDayOfWeek().getValue())) {
                            scheduleTimes.add(current.atTime(remindTime));
                        }
                        current = current.plusDays(1);
                    }
                }
                case MONTHLY -> {
                    // Monthly on the same day of month
                    while (!current.isAfter(end)) {
                        scheduleTimes.add(current.atTime(remindTime));
                        current = current.plusMonths(1);
                    }
                }
                case CUSTOM -> {
                    // Skip for now or just do once at start
                    scheduleTimes.add(current.atTime(remindTime));
                }
            }
            
            // Save reminders
            for (LocalDateTime dt : scheduleTimes) {
                // Check if already exists (optional but good)
                // Skip for performance in MVP
                
                com.healthfamily.domain.entity.HealthReminder reminder = com.healthfamily.domain.entity.HealthReminder.builder()
                    .user(plan.getPatient())
                    .creator(plan.getDoctor())
                    .family(plan.getFamily())
                    .type(com.healthfamily.domain.constant.ReminderType.FOLLOW_UP) // Link to plan as FollowUp
                    .title(plan.getTitle() + " - 提醒")
                    .content("请按时执行健康计划：" + plan.getTitle())
                    .scheduledTime(dt)
                    .status(com.healthfamily.domain.constant.ReminderStatus.PENDING)
                    .priority(com.healthfamily.domain.constant.ReminderPriority.MEDIUM)
                    .channel(channel)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                // Set metadata to link back to plan
                Map<String, Object> meta = new HashMap<>();
                meta.put("planId", plan.getId());
                meta.put("planTitle", plan.getTitle());
                reminder.setMetadataJson(objectMapper.writeValueAsString(meta));
                
                healthReminderRepository.save(reminder);
            }
            
        } catch (Exception e) {
            log.error("Failed to generate plan reminders", e);
        }
    }

    @Override
    @Transactional
    /**
     * 更新
     * @param doctorId 医生唯一标识
     * @param planId 计划唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public HealthPlanResponse updateHealthPlan(Long doctorId, Long planId, HealthPlanRequest request) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        HealthPlan plan = healthPlanRepository.findByIdAndDoctor(planId, doctor)
                .orElseThrow(() -> new BusinessException(40408, "健康计划不存在或无权限访问"));
        
        plan.setType(HealthPlanType.valueOf(request.type()));
        plan.setTitle(request.title());
        plan.setDescription(request.description());
        plan.setStartDate(request.startDate());
        plan.setEndDate(request.endDate());
        plan.setFrequencyType(FrequencyType.valueOf(request.frequencyType()));
        plan.setFrequencyValue(request.frequencyValue());
        plan.setFrequencyDetail(request.frequencyDetail());
        plan.setTargetIndicators(request.targetIndicators());
        plan.setReminderStrategy(request.reminderStrategy());
        
        HealthPlan saved = healthPlanRepository.save(plan);
        return toHealthPlanResponse(saved);
    }

    @Override
    @Transactional
    /**
     * 删除
     * @param doctorId 医生唯一标识
     * @param planId 计划唯一标识
     * @return 无
     */
    public void deleteHealthPlan(Long doctorId, Long planId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        HealthPlan plan = healthPlanRepository.findByIdAndDoctor(planId, doctor)
                .orElseThrow(() -> new BusinessException(40408, "健康计划不存在或无权限访问"));
        healthPlanRepository.delete(plan);
    }

    @Override
    /**
     * 获取
     * @param doctorId 医生唯一标识
     * @param patientUserId 业务对象唯一标识
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 业务返回结果
     */
    public List<HealthPlanResponse> getHealthPlansCalendar(Long doctorId, Long patientUserId, String startDate, String endDate) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        // 查询在指定日期范围内的计划
        // 先查询所有该患者的计划，然后在内存中过滤
        List<HealthPlan> allPlans = healthPlanRepository.findByPatientOrderByCreatedAtDesc(patient);
        List<HealthPlan> plans = allPlans.stream()
                .filter(p -> {
                    // 计划开始日期 <= 查询结束日期
                    if (p.getStartDate().isAfter(end)) return false;
                    // 如果计划有结束日期，则结束日期 >= 查询开始日期
                    if (p.getEndDate() != null && p.getEndDate().isBefore(start)) return false;
                    return true;
                })
                .collect(Collectors.toList());
        
        // 过滤出当前医生的计划
        return plans.stream()
                .filter(p -> p.getDoctor().getId().equals(doctorId))
                .map(this::toHealthPlanResponse)
                .collect(Collectors.toList());
    }

    /**
     * 将HealthPlan实体转换为DTO
     */
    private HealthPlanResponse toHealthPlanResponse(HealthPlan plan) {
        return new HealthPlanResponse(
                plan.getId(),
                plan.getDoctor().getId(),
                plan.getDoctor().getNickname(),
                plan.getPatient().getId(),
                plan.getPatient().getNickname(),
                plan.getFamily().getId(),
                plan.getFamily().getName(),
                plan.getType().name(),
                getPlanTypeLabel(plan.getType()),
                plan.getTitle(),
                plan.getDescription(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getFrequencyType().name(),
                getFrequencyTypeLabel(plan.getFrequencyType()),
                plan.getFrequencyValue(),
                plan.getFrequencyDetail(),
                plan.getTargetIndicators(),
                plan.getReminderStrategy(),
                plan.getStatus().name(),
                getPlanStatusLabel(plan.getStatus()),
                plan.getCompletionRate(),
                plan.getComplianceRate(),
                plan.getMetadataJson(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }

    private String getPlanTypeLabel(HealthPlanType type) {
        return switch (type) {
            case BLOOD_PRESSURE_FOLLOWUP -> "血压随访";
            case DIET_MANAGEMENT -> "饮食管理";
            case EXERCISE_PRESCRIPTION -> "运动处方";
            case MEDICATION_MANAGEMENT -> "用药管理";
            case WEIGHT_MANAGEMENT -> "体重管理";
            case OTHER -> "其他";
        };
    }

    private String getFrequencyTypeLabel(FrequencyType type) {
        return switch (type) {
            case DAILY -> "每日";
            case WEEKLY -> "每周";
            case MONTHLY -> "每月";
            case CUSTOM -> "自定义";
        };
    }

    private String getPlanStatusLabel(HealthPlanStatus status) {
        return switch (status) {
            case ACTIVE -> "进行中";
            case COMPLETED -> "已完成";
            case OVERDUE -> "逾期";
            case CANCELLED -> "已取消";
            case PAUSED -> "已暂停";
        };
    }

    // ==================== 随访任务相关方法 ====================

    @Override
    /**
     * 查询列表
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param patientUserId 业务对象唯一标识
     * @param status 业务参数
     * @return 业务返回结果
     */
    public List<com.healthfamily.web.dto.FollowUpTaskResponse> listFollowUpTasks(Long doctorId, Long familyId, Long patientUserId, String status) {
        // 验证权限
        ensureDoctorAccess(doctorId, familyRepository.findById(familyId).orElseThrow(() -> new BusinessException(40402, "家庭不存在")));
        
        // 构建查询条件
        List<com.healthfamily.domain.entity.HealthReminder> reminders = healthReminderRepository.findByFamily_IdOrderByScheduledTimeAsc(familyId);
        
        return reminders.stream()
                .filter(r -> patientUserId == null || r.getUser().getId().equals(patientUserId))
                .filter(r -> r.getType() == com.healthfamily.domain.constant.ReminderType.FOLLOW_UP)
                .filter(r -> status == null || status.isBlank() || (r.getStatus() != null && r.getStatus().name().equals(status)))
                .map(r -> {
                    Long planId = null;
                    String planTitle = null;
                    String result = null;
                    // 尝试从metadata解析
                    if (r.getMetadataJson() != null) {
                        try {
                            Map<String, Object> meta = objectMapper.readValue(r.getMetadataJson(), new TypeReference<>() {});
                            if (meta.containsKey("planId")) {
                                planId = ((Number) meta.get("planId")).longValue();
                            }
                            if (meta.containsKey("planTitle")) {
                                planTitle = (String) meta.get("planTitle");
                            }
                            if (meta.containsKey("result")) {
                                result = (String) meta.get("result");
                            }
                        } catch (Exception e) {}
                    }
                    
                    return new com.healthfamily.web.dto.FollowUpTaskResponse(
                            r.getId(),
                            r.getTitle(),
                            r.getContent(),
                            r.getScheduledTime(),
                            r.getStatus().name(),
                            r.getPriority().name(),
                            result,
                            planId,
                            planTitle
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    /**
     * 创建
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param patientUserId 业务对象唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public com.healthfamily.web.dto.FollowUpTaskResponse createFollowUpTask(Long doctorId, Long familyId, Long patientUserId, com.healthfamily.web.dto.CreateFollowUpTaskRequest request) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        
        ensureDoctorAccess(doctorId, family);
        
        com.healthfamily.domain.entity.HealthReminder reminder = com.healthfamily.domain.entity.HealthReminder.builder()
                .user(patient)
                .creator(doctor)
                .family(family)
                .type(com.healthfamily.domain.constant.ReminderType.FOLLOW_UP) // 随访类型
                .title(request.title())
                .content(request.content() != null ? request.content() : "")
                .scheduledTime(request.scheduledTime())
                .status(com.healthfamily.domain.constant.ReminderStatus.PENDING)
                .priority(request.priority() != null ? 
                        com.healthfamily.domain.constant.ReminderPriority.valueOf(request.priority()) : 
                        com.healthfamily.domain.constant.ReminderPriority.MEDIUM)
                .channel("APP")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        if (request.planId() != null) {
            try {
                Map<String, Object> meta = new HashMap<>();
                meta.put("planId", request.planId());
                healthPlanRepository.findById(request.planId()).ifPresent(p -> meta.put("planTitle", p.getTitle()));
                reminder.setMetadataJson(objectMapper.writeValueAsString(meta));
            } catch (Exception e) {}
        }
        
        reminder = healthReminderRepository.save(reminder);
        
        return new com.healthfamily.web.dto.FollowUpTaskResponse(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getContent(),
                reminder.getScheduledTime(),
                reminder.getStatus().name(),
                reminder.getPriority().name(),
                null,
                request.planId(),
                null
        );
    }

    @Override
    @Transactional
    /**
     * 更新
     * @param doctorId 医生唯一标识
     * @param taskId 业务对象唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public com.healthfamily.web.dto.FollowUpTaskResponse updateFollowUpTask(Long doctorId, Long taskId, com.healthfamily.web.dto.UpdateFollowUpTaskRequest request) {
        com.healthfamily.domain.entity.HealthReminder reminder = healthReminderRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(40410, "任务不存在"));
        
        if (reminder.getCreator() != null && !reminder.getCreator().getId().equals(doctorId)) {
            throw new BusinessException(40301, "无权修改此任务");
        }
        
        if (request.title() != null) reminder.setTitle(request.title());
        if (request.content() != null) reminder.setContent(request.content());
        if (request.scheduledTime() != null) reminder.setScheduledTime(request.scheduledTime());
        if (request.status() != null) reminder.setStatus(com.healthfamily.domain.constant.ReminderStatus.valueOf(request.status()));
        if (request.priority() != null) reminder.setPriority(com.healthfamily.domain.constant.ReminderPriority.valueOf(request.priority()));
        
        // 处理结果存入 metadata
        if (request.result() != null) {
            try {
                Map<String, Object> meta = new HashMap<>();
                if (reminder.getMetadataJson() != null) {
                    meta = objectMapper.readValue(reminder.getMetadataJson(), new TypeReference<Map<String, Object>>() {});
                }
                meta.put("result", request.result());
                reminder.setMetadataJson(objectMapper.writeValueAsString(meta));
            } catch (Exception e) {}
        }
        
        reminder.setUpdatedAt(LocalDateTime.now());
        reminder = healthReminderRepository.save(reminder);
        
        Long planId = null;
        String result = null;
        if (reminder.getMetadataJson() != null) {
             try {
                Map<String, Object> meta = objectMapper.readValue(reminder.getMetadataJson(), new TypeReference<Map<String, Object>>() {});
                if (meta.containsKey("planId")) planId = ((Number) meta.get("planId")).longValue();
                if (meta.containsKey("result")) result = (String) meta.get("result");
            } catch (Exception e) {}
        }
        
        return new com.healthfamily.web.dto.FollowUpTaskResponse(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getContent(),
                reminder.getScheduledTime(),
                reminder.getStatus().name(),
                reminder.getPriority().name(),
                result,
                planId,
                null
        );
    }

    @Override
    @Transactional
    /**
     * 删除
     * @param doctorId 医生唯一标识
     * @param taskId 业务对象唯一标识
     * @return 无
     */
    public void deleteFollowUpTask(Long doctorId, Long taskId) {
        com.healthfamily.domain.entity.HealthReminder reminder = healthReminderRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(40410, "任务不存在"));
        
        if (reminder.getCreator() != null && !reminder.getCreator().getId().equals(doctorId)) {
            throw new BusinessException(40301, "无权删除此任务");
        }
        
        healthReminderRepository.delete(reminder);
    }
    
    // ==================== 数据统计相关方法 ====================

    @Override
    /**
     * 获取
     * @param doctorId 医生唯一标识
     * @param familyId 家庭唯一标识
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 业务返回结果
     */
    public DoctorStatsResponse getStatistics(Long doctorId, Long familyId, LocalDate startDate, LocalDate endDate) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));

        List<Family> families = new java.util.ArrayList<>();
        if (familyId != null) {
            Family family = familyRepository.findById(familyId)
                    .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
            ensureDoctorAccess(doctorId, family);
            families.add(family);
        } else {
            List<FamilyDoctor> bindings = familyDoctorRepository.findByDoctor_Id(doctorId);
            families = bindings.stream().map(FamilyDoctor::getFamily).collect(Collectors.toList());
        }

        if (families.isEmpty()) {
            return createEmptyDoctorStatsResponse();
        }

        List<User> allPatients = new java.util.ArrayList<>();
        DoctorStatsResponse.WorkloadStats totalWorkload = createEmptyWorkloadStats();

        for (Family family : families) {
            List<User> patients = familyMemberRepository.findByFamily(family).stream()
                    .map(com.healthfamily.domain.entity.FamilyMember::getUser)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            allPatients.addAll(patients);

            DoctorStatsResponse.WorkloadStats familyWorkload = buildWorkloadStats(doctor, family, startDate, endDate);
            totalWorkload = mergeWorkloadStats(totalWorkload, familyWorkload);
        }

        DoctorStatsResponse.PatientStructureStats totalPatientStructure = buildPatientStructureStats(allPatients);
        DoctorStatsResponse.ManagementEffectStats totalManagementEffect = buildManagementEffectStats(allPatients, startDate, endDate);

        return new DoctorStatsResponse(totalPatientStructure, totalManagementEffect, totalWorkload);
    }

    private DoctorStatsResponse createEmptyDoctorStatsResponse() {
        return new DoctorStatsResponse(createEmptyPatientStructureStats(), createEmptyManagementEffectStats(), createEmptyWorkloadStats());
    }

    private DoctorStatsResponse.PatientStructureStats createEmptyPatientStructureStats() {
        return new DoctorStatsResponse.PatientStructureStats(java.util.Collections.emptyMap(), java.util.Collections.emptyMap(), java.util.Collections.emptyMap());
    }

    private DoctorStatsResponse.ManagementEffectStats createEmptyManagementEffectStats() {
        DoctorStatsResponse.ManagementEffectStats.BloodPressureStats bp = new DoctorStatsResponse.ManagementEffectStats.BloodPressureStats(0.0, 0, 0, java.util.Collections.emptyList());
        DoctorStatsResponse.ManagementEffectStats.WeightStats w = new DoctorStatsResponse.ManagementEffectStats.WeightStats(0.0, 0, 0, java.util.Collections.emptyList());
        DoctorStatsResponse.ManagementEffectStats.SleepStats s = new DoctorStatsResponse.ManagementEffectStats.SleepStats(0.0, 0.0, 0, java.util.Collections.emptyList());
        return new DoctorStatsResponse.ManagementEffectStats(bp, w, s);
    }

    private DoctorStatsResponse.WorkloadStats createEmptyWorkloadStats() {
        DoctorStatsResponse.WorkloadStats.ConsultationStats cs = new DoctorStatsResponse.WorkloadStats.ConsultationStats(0, 0, java.util.Collections.emptyList());
        DoctorStatsResponse.WorkloadStats.FollowupStats fs = new DoctorStatsResponse.WorkloadStats.FollowupStats(0, 0, 0, java.util.Collections.emptyList());
        DoctorStatsResponse.WorkloadStats.ReminderStats rs = new DoctorStatsResponse.WorkloadStats.ReminderStats(0, 0, 0, 0.0, java.util.Collections.emptyList());
        return new DoctorStatsResponse.WorkloadStats(cs, fs, rs);
    }

    private DoctorStatsResponse.WorkloadStats mergeWorkloadStats(DoctorStatsResponse.WorkloadStats s1, DoctorStatsResponse.WorkloadStats s2) {
        // Merge ConsultationStats
        DoctorStatsResponse.WorkloadStats.ConsultationStats cs = new DoctorStatsResponse.WorkloadStats.ConsultationStats(
            s1.consultation().totalCount() + s2.consultation().totalCount(),
            s1.consultation().activeSessions() + s2.consultation().activeSessions(),
            mergeDateValueLists(s1.consultation().trend(), s2.consultation().trend())
        );

        // Merge FollowupStats
        DoctorStatsResponse.WorkloadStats.FollowupStats fs = new DoctorStatsResponse.WorkloadStats.FollowupStats(
            s1.followup().totalPlans() + s2.followup().totalPlans(),
            s1.followup().activePlans() + s2.followup().activePlans(),
            s1.followup().completedPlans() + s2.followup().completedPlans(),
            mergeDateValueLists(s1.followup().trend(), s2.followup().trend())
        );

        // Merge ReminderStats
        int totalSent = s1.reminder().totalSent() + s2.reminder().totalSent();
        int totalCompleted = s1.reminder().completed() + s2.reminder().completed();
        double newCompletionRate = (totalSent == 0) ? 0.0 : (double) totalCompleted / totalSent * 100;
        DoctorStatsResponse.WorkloadStats.ReminderStats rs = new DoctorStatsResponse.WorkloadStats.ReminderStats(
            totalSent,
            totalCompleted,
            s1.reminder().pending() + s2.reminder().pending(),
            newCompletionRate,
            mergeDateValueLists(s1.reminder().trend(), s2.reminder().trend())
        );

        return new DoctorStatsResponse.WorkloadStats(cs, fs, rs);
    }

    private List<DoctorStatsResponse.DateValue> mergeDateValueLists(List<DoctorStatsResponse.DateValue> l1, List<DoctorStatsResponse.DateValue> l2) {
        Map<LocalDate, Double> mergedMap = new java.util.HashMap<>();
        l1.forEach(dv -> mergedMap.merge(dv.date(), dv.value(), Double::sum));
        l2.forEach(dv -> mergedMap.merge(dv.date(), dv.value(), Double::sum));
        return mergedMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new DoctorStatsResponse.DateValue(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * 构建患者结构统计
     */
    private DoctorStatsResponse.PatientStructureStats buildPatientStructureStats(List<User> patients) {
        Map<String, Integer> ageDistribution = new HashMap<>();
        Map<String, Integer> genderDistribution = new HashMap<>();
        Map<String, Integer> diseaseDistribution = new HashMap<>();
        
        for (User patient : patients) {
            Profile profile = profileRepository.findById(patient.getId()).orElse(null);
            
            // 年龄段分布
            if (profile != null && profile.getBirthday() != null) {
                int age = java.time.Period.between(profile.getBirthday(), LocalDate.now()).getYears();
                String ageGroup = getAgeGroup(age);
                ageDistribution.put(ageGroup, ageDistribution.getOrDefault(ageGroup, 0) + 1);
            }
            
            // 性别分布
            if (profile != null && profile.getSex() != null) {
                String gender = profile.getSex() == Sex.M ? "M" : (profile.getSex() == Sex.F ? "F" : "OTHER");
                genderDistribution.put(gender, genderDistribution.getOrDefault(gender, 0) + 1);
            }
            
            // 疾病类别分布
            if (profile != null && profile.getHealthTags() != null) {
                try {
                    List<String> healthTags = objectMapper.readValue(profile.getHealthTags(), STRING_LIST_TYPE_REF);
                    for (String tag : healthTags) {
                        diseaseDistribution.put(tag, diseaseDistribution.getOrDefault(tag, 0) + 1);
                    }
                } catch (Exception e) {
                    log.warn("解析健康标签失败: {}", e.getMessage());
                }
            }
        }
        
        return new DoctorStatsResponse.PatientStructureStats(ageDistribution, genderDistribution, diseaseDistribution);
    }

    private String getAgeGroup(int age) {
        if (age < 18) return "0-18";
        if (age < 35) return "19-35";
        if (age < 50) return "36-50";
        if (age < 65) return "51-65";
        return "65+";
    }

    /**
     * 构建管理效果统计
     */
    private DoctorStatsResponse.ManagementEffectStats buildManagementEffectStats(List<User> patients, LocalDate startDate, LocalDate endDate) {
        // 血压统计
        DoctorStatsResponse.ManagementEffectStats.BloodPressureStats bloodPressure = buildBloodPressureStats(patients, startDate, endDate);
        
        // 体重统计
        DoctorStatsResponse.ManagementEffectStats.WeightStats weight = buildWeightStats(patients, startDate, endDate);
        
        // 睡眠统计
        DoctorStatsResponse.ManagementEffectStats.SleepStats sleep = buildSleepStats(patients, startDate, endDate);
        
        return new DoctorStatsResponse.ManagementEffectStats(bloodPressure, weight, sleep);
    }

    private DoctorStatsResponse.ManagementEffectStats.BloodPressureStats buildBloodPressureStats(List<User> patients, LocalDate startDate, LocalDate endDate) {
        int totalCount = 0;
        int compliantCount = 0;
        Map<LocalDate, List<Boolean>> dailyCompliance = new HashMap<>();
        
        for (User patient : patients) {
            List<HealthLog> logs = healthLogRepository.findByUserAndLogDateBetweenOrderByLogDateDesc(patient, startDate, endDate);
            for (HealthLog healthLog : logs) {
                if (healthLog.getType() == HealthLogType.VITALS) {
                    try {
                        Map<String, Object> content = objectMapper.readValue(healthLog.getContentJson(), MAP_TYPE_REF);
                        Object vitalType = content.get("vitalType");
                        if ("血压".equals(vitalType) || vitalType != null && vitalType.toString().contains("血压")) {
                            totalCount++;
                            boolean compliant = isBloodPressureCompliant(content);
                            if (compliant) compliantCount++;
                            
                            dailyCompliance.computeIfAbsent(healthLog.getLogDate(), k -> new ArrayList<>()).add(compliant);
                        }
                    } catch (Exception e) {
                        log.warn("解析血压日志失败: {}", e.getMessage());
                    }
                }
            }
        }
        
        Double complianceRate = totalCount > 0 ? (double) compliantCount * 100 / totalCount : 0.0;
        List<DoctorStatsResponse.DateValue> trend = buildDailyComplianceTrend(dailyCompliance);
        
        return new DoctorStatsResponse.ManagementEffectStats.BloodPressureStats(
                complianceRate, totalCount, compliantCount, trend);
    }

    private boolean isBloodPressureCompliant(Map<String, Object> content) {
        Object sys = content.get("systolic");
        Object dia = content.get("diastolic");
        if (sys instanceof Number && dia instanceof Number) {
            double systolic = ((Number) sys).doubleValue();
            double diastolic = ((Number) dia).doubleValue();
            // 血压达标标准：收缩压 < 140 且 舒张压 < 90
            return systolic < 140 && diastolic < 90;
        }
        return false;
    }

    private DoctorStatsResponse.ManagementEffectStats.WeightStats buildWeightStats(List<User> patients, LocalDate startDate, LocalDate endDate) {
        Map<Long, List<WeightRecord>> patientWeights = new HashMap<>();
        
        for (User patient : patients) {
            List<HealthLog> logs = healthLogRepository.findByUserAndLogDateBetweenOrderByLogDateDesc(patient, startDate, endDate);
            List<WeightRecord> weights = new ArrayList<>();
            
            for (HealthLog healthLog : logs) {
                if (healthLog.getType() == HealthLogType.VITALS) {
                    try {
                        Map<String, Object> content = objectMapper.readValue(healthLog.getContentJson(), MAP_TYPE_REF);
                        Object vitalType = content.get("vitalType");
                        if ("体重".equals(vitalType) || vitalType != null && vitalType.toString().contains("体重")) {
                            Object value = content.get("value");
                            if (value instanceof Number) {
                                weights.add(new WeightRecord(healthLog.getLogDate(), ((Number) value).doubleValue()));
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析体重日志失败: {}", e.getMessage());
                    }
                }
            }
            
            if (!weights.isEmpty()) {
                patientWeights.put(patient.getId(), weights);
            }
        }
        
        // 计算平均体重变化
        double totalChange = 0.0;
        int weightLossCount = 0;
        int totalPatients = patientWeights.size();
        Map<LocalDate, Double> dailyAverageWeight = new HashMap<>();
        
        for (Map.Entry<Long, List<WeightRecord>> entry : patientWeights.entrySet()) {
            List<WeightRecord> weights = entry.getValue();
            if (weights.size() >= 2) {
                WeightRecord first = weights.get(weights.size() - 1);  // 最早的
                WeightRecord last = weights.get(0);  // 最新的
                double change = last.weight - first.weight;
                totalChange += change;
                if (change < 0) weightLossCount++;
            }
            
            // 构建每日平均体重
            for (WeightRecord wr : weights) {
                dailyAverageWeight.merge(wr.date, wr.weight, (a, b) -> (a + b) / 2);
            }
        }
        
        Double averageWeightChange = totalPatients > 0 ? totalChange / totalPatients : 0.0;
        List<DoctorStatsResponse.DateValue> trend = dailyAverageWeight.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new DoctorStatsResponse.DateValue(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        
        return new DoctorStatsResponse.ManagementEffectStats.WeightStats(
                averageWeightChange, totalPatients, weightLossCount, trend);
    }

    private record WeightRecord(LocalDate date, double weight) {}

    private DoctorStatsResponse.ManagementEffectStats.SleepStats buildSleepStats(List<User> patients, LocalDate startDate, LocalDate endDate) {
        List<Double> sleepHoursList = new ArrayList<>();
        Map<LocalDate, List<Double>> dailySleepHours = new HashMap<>();
        
        for (User patient : patients) {
            List<HealthLog> logs = healthLogRepository.findByUserAndLogDateBetweenOrderByLogDateDesc(patient, startDate, endDate);
            for (HealthLog healthLog : logs) {
                if (healthLog.getType() == HealthLogType.SLEEP) {
                    try {
                        Map<String, Object> content = objectMapper.readValue(healthLog.getContentJson(), MAP_TYPE_REF);
                        Object hours = content.get("hours");
                        if (hours instanceof Number) {
                            double h = ((Number) hours).doubleValue();
                            sleepHoursList.add(h);
                            dailySleepHours.computeIfAbsent(healthLog.getLogDate(), k -> new ArrayList<>()).add(h);
                        }
                    } catch (Exception e) {
                        log.warn("解析睡眠日志失败: {}", e.getMessage());
                    }
                }
            }
        }
        
        Double averageSleepHours = sleepHoursList.isEmpty() ? 0.0 : 
                sleepHoursList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // 改善率：比较前半段和后半段的平均睡眠时长
        Double improvementRate = calculateImprovementRate(dailySleepHours);
        
        List<DoctorStatsResponse.DateValue> trend = dailySleepHours.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    double avg = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    return new DoctorStatsResponse.DateValue(e.getKey(), avg);
                })
                .collect(Collectors.toList());
        
        return new DoctorStatsResponse.ManagementEffectStats.SleepStats(
                averageSleepHours, improvementRate, sleepHoursList.size(), trend);
    }

    private Double calculateImprovementRate(Map<LocalDate, List<Double>> dailySleepHours) {
        if (dailySleepHours.size() < 4) return 0.0;
        
        List<Map.Entry<LocalDate, List<Double>>> sorted = dailySleepHours.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());
        
        int mid = sorted.size() / 2;
        List<Double> firstHalf = sorted.subList(0, mid).stream()
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList());
        List<Double> secondHalf = sorted.subList(mid, sorted.size()).stream()
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList());
        
        if (firstHalf.isEmpty() || secondHalf.isEmpty()) return 0.0;
        
        double firstAvg = firstHalf.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double secondAvg = secondHalf.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        if (firstAvg == 0) return 0.0;
        return (secondAvg - firstAvg) / firstAvg * 100;
    }

    private List<DoctorStatsResponse.DateValue> buildDailyComplianceTrend(Map<LocalDate, List<Boolean>> dailyCompliance) {
        return dailyCompliance.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    long compliant = e.getValue().stream().mapToLong(b -> b ? 1 : 0).sum();
                    double rate = e.getValue().isEmpty() ? 0.0 : (double) compliant * 100 / e.getValue().size();
                    return new DoctorStatsResponse.DateValue(e.getKey(), rate);
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建工作负载统计
     */
    private DoctorStatsResponse.WorkloadStats buildWorkloadStats(User doctor, Family family, LocalDate startDate, LocalDate endDate) {
        // 咨询统计
        DoctorStatsResponse.WorkloadStats.ConsultationStats consultation = buildConsultationStats(doctor, family, startDate, endDate);
        
        // 随访统计
        DoctorStatsResponse.WorkloadStats.FollowupStats followup = buildFollowupStats(doctor, family, startDate, endDate);
        
        // 提醒统计
        DoctorStatsResponse.WorkloadStats.ReminderStats reminder = buildReminderStats(doctor, family, startDate, endDate);
        
        return new DoctorStatsResponse.WorkloadStats(consultation, followup, reminder);
    }

    private DoctorStatsResponse.WorkloadStats.ConsultationStats buildConsultationStats(User doctor, Family family, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<ConsultationSession> sessions = consultationSessionRepository.findByFamilyOrderByLastMessageAtDesc(family);
        sessions = sessions.stream()
                .filter(s -> s.getDoctor() != null && s.getDoctor().getId().equals(doctor.getId()))
                .filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().isBefore(startDateTime) && !s.getCreatedAt().isAfter(endDateTime))
                .collect(Collectors.toList());
        
        int totalCount = sessions.size();
        int activeSessions = (int) sessions.stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .count();
        
        Map<LocalDate, Integer> dailyCount = new HashMap<>();
        for (ConsultationSession session : sessions) {
            LocalDate date = session.getCreatedAt().toLocalDate();
            dailyCount.put(date, dailyCount.getOrDefault(date, 0) + 1);
        }
        
        List<DoctorStatsResponse.DateValue> trend = dailyCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new DoctorStatsResponse.DateValue(e.getKey(), (double) e.getValue()))
                .collect(Collectors.toList());
        
        return new DoctorStatsResponse.WorkloadStats.ConsultationStats(totalCount, activeSessions, trend);
    }

    private DoctorStatsResponse.WorkloadStats.FollowupStats buildFollowupStats(User doctor, Family family, LocalDate startDate, LocalDate endDate) {
        List<HealthPlan> plans = healthPlanRepository.findByDoctorAndFamilyOrderByCreatedAtDesc(doctor, family);
        plans = plans.stream()
                .filter(p -> !p.getStartDate().isAfter(endDate))
                .filter(p -> p.getEndDate() == null || !p.getEndDate().isBefore(startDate))
                .collect(Collectors.toList());
        
        int totalPlans = plans.size();
        int activePlans = (int) plans.stream()
                .filter(p -> p.getStatus() == HealthPlanStatus.ACTIVE)
                .count();
        int completedPlans = (int) plans.stream()
                .filter(p -> p.getStatus() == HealthPlanStatus.COMPLETED)
                .count();
        
        Map<LocalDate, Integer> dailyCount = new HashMap<>();
        for (HealthPlan plan : plans) {
            LocalDate date = plan.getCreatedAt().toLocalDate();
            dailyCount.put(date, dailyCount.getOrDefault(date, 0) + 1);
        }
        
        List<DoctorStatsResponse.DateValue> trend = dailyCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new DoctorStatsResponse.DateValue(e.getKey(), (double) e.getValue()))
                .collect(Collectors.toList());
        
        return new DoctorStatsResponse.WorkloadStats.FollowupStats(totalPlans, activePlans, completedPlans, trend);
    }

    private DoctorStatsResponse.WorkloadStats.ReminderStats buildReminderStats(User doctor, Family family, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<com.healthfamily.domain.entity.HealthReminder> reminders = healthReminderRepository.findByCreator_IdAndFamily_Id(doctor.getId(), family.getId());
        reminders = reminders.stream()
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(startDateTime) && !r.getCreatedAt().isAfter(endDateTime))
                .collect(Collectors.toList());
        
        int totalSent = reminders.size();
        int completed = (int) reminders.stream()
                .filter(r -> r.getStatus() == ReminderStatus.ACKNOWLEDGED || r.getStatus() == ReminderStatus.COMPLETED)
                .count();
        int pending = (int) reminders.stream()
                .filter(r -> r.getStatus() == ReminderStatus.PENDING || r.getStatus() == ReminderStatus.SENT)
                .count();
        
        Double completionRate = totalSent > 0 ? (double) completed * 100 / totalSent : 0.0;
        
        Map<LocalDate, Integer> dailyCount = new HashMap<>();
        for (com.healthfamily.domain.entity.HealthReminder reminder : reminders) {
            LocalDate date = reminder.getCreatedAt().toLocalDate();
            dailyCount.put(date, dailyCount.getOrDefault(date, 0) + 1);
        }
        
        List<DoctorStatsResponse.DateValue> trend = dailyCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new DoctorStatsResponse.DateValue(e.getKey(), (double) e.getValue()))
                .collect(Collectors.toList());
        
        return new DoctorStatsResponse.WorkloadStats.ReminderStats(totalSent, completed, pending, completionRate, trend);
    }

    // ==================== 医生设置相关方法 ====================

    @Override
    /**
     * 获取
     * @param doctorId 医生唯一标识
     * @return 业务返回结果
     */
    public DoctorSettingsResponse getDoctorSettings(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        
        Profile profile = profileRepository.findById(doctorId).orElse(null);
        Map<String, Object> prefs = readPreferences(profile);
        
        // 读取通知设置
        DoctorNotificationSettings notifications = readNotificationSettings(prefs);
        
        // 读取工作时间设置
        DoctorSettingsResponse.WorkingHours workingHours = readWorkingHours(prefs);
        
        return new DoctorSettingsResponse(notifications, workingHours);
    }

    @Override
    @Transactional
    /**
     * 更新
     * @param doctorId 医生唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public DoctorSettingsResponse updateDoctorSettings(Long doctorId, DoctorSettingsRequest request) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        
        Profile profile = profileRepository.findById(doctorId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUser(doctor);
            return p;
        });
        
        Map<String, Object> prefs = readPreferences(profile);
        
        // 保存通知设置（如果传入）
        if (request.notifications() != null) {
            Map<String, Object> doctorNotifications = new HashMap<>();
            if (request.notifications().system() != null) {
                Map<String, Object> system = new HashMap<>();
                system.put("inApp", request.notifications().system().inApp());
                system.put("email", request.notifications().system().email());
                system.put("sms", request.notifications().system().sms());
                doctorNotifications.put("system", system);
            }
            if (request.notifications().followup() != null) {
                Map<String, Object> followup = new HashMap<>();
                followup.put("inApp", request.notifications().followup().inApp());
                followup.put("email", request.notifications().followup().email());
                followup.put("sms", request.notifications().followup().sms());
                doctorNotifications.put("followup", followup);
            }
            if (request.notifications().alert() != null) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("inApp", request.notifications().alert().inApp());
                alert.put("email", request.notifications().alert().email());
                alert.put("sms", request.notifications().alert().sms());
                doctorNotifications.put("alert", alert);
            }
            prefs.put("doctorNotifications", doctorNotifications);
        }
        
        // 保存工作时间设置（如果传入）
        if (request.workingHours() != null) {
            Map<String, Object> workingHoursMap = new HashMap<>();
            workingHoursMap.put("workDays", request.workingHours().workDays());
            workingHoursMap.put("startTime", request.workingHours().startTime());
            workingHoursMap.put("endTime", request.workingHours().endTime());
            prefs.put("doctorWorkingHours", workingHoursMap);
        }
        
        profile.setPreferences(writePreferences(prefs));
        if (profile.getUpdatedAt() == null) profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);
        
        return getDoctorSettings(doctorId);
    }

    private Map<String, Object> readPreferences(Profile profile) {
        try {
            if (profile == null || profile.getPreferences() == null || profile.getPreferences().isBlank()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(profile.getPreferences(), MAP_TYPE_REF);
        } catch (Exception e) {
            log.warn("解析偏好设置失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private String writePreferences(Map<String, Object> prefs) {
        try {
            return objectMapper.writeValueAsString(prefs);
        } catch (Exception e) {
            log.error("序列化偏好设置失败: {}", e.getMessage());
            return "{}";
        }
    }

    private DoctorNotificationSettings readNotificationSettings(Map<String, Object> prefs) {
        Map<String, Object> doctorNotifications = (Map<String, Object>) prefs.getOrDefault("doctorNotifications", new HashMap<>());
        
        // 读取系统通知
        Map<String, Object> systemMap = (Map<String, Object>) doctorNotifications.getOrDefault("system", defaultNotificationChannels());
        DoctorNotificationSettings.NotificationChannels system = new DoctorNotificationSettings.NotificationChannels(
                (Boolean) systemMap.getOrDefault("inApp", true),
                (Boolean) systemMap.getOrDefault("email", false),
                (Boolean) systemMap.getOrDefault("sms", false)
        );
        
        // 读取随访提醒
        Map<String, Object> followupMap = (Map<String, Object>) doctorNotifications.getOrDefault("followup", defaultNotificationChannels());
        DoctorNotificationSettings.NotificationChannels followup = new DoctorNotificationSettings.NotificationChannels(
                (Boolean) followupMap.getOrDefault("inApp", true),
                (Boolean) followupMap.getOrDefault("email", false),
                (Boolean) followupMap.getOrDefault("sms", false)
        );
        
        // 读取预警通知
        Map<String, Object> alertMap = (Map<String, Object>) doctorNotifications.getOrDefault("alert", defaultNotificationChannels());
        DoctorNotificationSettings.NotificationChannels alert = new DoctorNotificationSettings.NotificationChannels(
                (Boolean) alertMap.getOrDefault("inApp", true),
                (Boolean) alertMap.getOrDefault("email", false),
                (Boolean) alertMap.getOrDefault("sms", false)
        );
        
        return new DoctorNotificationSettings(system, followup, alert);
    }

    private Map<String, Object> defaultNotificationChannels() {
        Map<String, Object> map = new HashMap<>();
        map.put("inApp", true);
        map.put("email", false);
        map.put("sms", false);
        return map;
    }

    private DoctorSettingsResponse.WorkingHours readWorkingHours(Map<String, Object> prefs) {
        Map<String, Object> workingHours = (Map<String, Object>) prefs.getOrDefault("doctorWorkingHours", new HashMap<>());
        
        @SuppressWarnings("unchecked")
        List<String> workDays = (List<String>) workingHours.getOrDefault("workDays", List.of("MON", "TUE", "WED", "THU", "FRI"));
        String startTime = (String) workingHours.getOrDefault("startTime", "09:00");
        String endTime = (String) workingHours.getOrDefault("endTime", "18:00");
        
        return new DoctorSettingsResponse.WorkingHours(workDays, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 获取
     * @param keyword 业务参数
     * @param status 业务参数
     * @param specialty 业务参数
     * @param page 分页页码
     * @param size 分页大小
     * @param startTime 业务参数
     * @param endTime 业务参数
     * @return 业务返回结果
     */
    public java.util.Map<String, Object> getAdminDoctorList(String keyword, String status, String specialty, int page, int size, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        // 获取所有医生用户
        List<User> doctors = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == com.healthfamily.domain.constant.UserRole.DOCTOR)
                .collect(java.util.stream.Collectors.toList());
        
        // 转换为DTO列表
        List<AdminDoctorDto> doctorDtos = new ArrayList<>();
        for (User doctor : doctors) {
            DoctorProfile profile = doctorProfileRepository.findByDoctorId(doctor.getId()).orElse(null);
            AdminDoctorDto dto = toAdminDoctorDto(doctor, profile);
            if (dto != null) {
                doctorDtos.add(dto);
            }
        }
        
        // 应用筛选条件
        if (keyword != null && !keyword.isEmpty()) {
            final String keywordLower = keyword.toLowerCase();
            doctorDtos = doctorDtos.stream()
                .filter(dto -> (dto.name() != null && dto.name().toLowerCase().contains(keywordLower))
                    || (dto.phone() != null && dto.phone().contains(keyword))
                    || (dto.hospital() != null && dto.hospital().toLowerCase().contains(keywordLower))
                    || (dto.specialty() != null && dto.specialty().toLowerCase().contains(keywordLower)))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 根据认证状态筛选
        if (status != null && !status.isEmpty()) {
            if ("certified".equalsIgnoreCase(status) || "true".equals(status)) {
                doctorDtos = doctorDtos.stream()
                    .filter(dto -> dto.certified() != null && dto.certified())
                    .collect(java.util.stream.Collectors.toList());
            } else if ("pending".equalsIgnoreCase(status)) {
                doctorDtos = doctorDtos.stream()
                    .filter(dto -> "PENDING".equals(dto.certificationStatus()))
                    .collect(java.util.stream.Collectors.toList());
            } else if ("rejected".equalsIgnoreCase(status)) {
                doctorDtos = doctorDtos.stream()
                    .filter(dto -> "REJECTED".equals(dto.certificationStatus()))
                    .collect(java.util.stream.Collectors.toList());
            }
        }
        
        // 根据专业领域筛选
        if (specialty != null && !specialty.isEmpty()) {
            final String specialtyLower = specialty.toLowerCase();
            doctorDtos = doctorDtos.stream()
                .filter(dto -> dto.specialty() != null && dto.specialty().toLowerCase().contains(specialtyLower))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 根据时间范围筛选
        if (startTime != null || endTime != null) {
            doctorDtos = doctorDtos.stream()
                .filter(dto -> {
                    java.time.LocalDateTime createdAt = dto.createdAt();
                    if (createdAt == null) return false;
                    
                    boolean afterStart = startTime == null || !createdAt.isBefore(startTime);
                    boolean beforeEnd = endTime == null || !createdAt.isAfter(endTime);
                    
                    return afterStart && beforeEnd;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 按创建时间倒序排序
        doctorDtos.sort((a, b) -> {
            if (a.createdAt() == null) return 1;
            if (b.createdAt() == null) return -1;
            return b.createdAt().compareTo(a.createdAt());
        });
        
        // 计算分页数据
        int total = doctorDtos.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        List<AdminDoctorDto> pagedList;
        if (start >= total) {
            pagedList = java.util.Collections.emptyList();
        } else {
            pagedList = doctorDtos.subList(start, end);
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", pagedList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));
        
        return result;
    }

    @Override
    /**
     * 执行业务操作
     * @param id 业务对象唯一标识
     * @return 业务返回结果
     */
    public User findById(Long id) {
        // 注意：在当前实现中，医生信息存储在User表中
        // 这里返回的是User对象
        User user = userRepository.findById(id).orElse(null);
        if (user != null && user.getRole() == com.healthfamily.domain.constant.UserRole.DOCTOR) {
            return user;
        }
        return null;
    }

    @Override
    /**
     * 创建
     * @param doctor 业务参数
     * @return 业务返回结果
     */
    public User create(User doctor) {
        // 创建医生用户
        User user = new User();
        user.setNickname(doctor.getNickname());
        user.setPhone(doctor.getPhone());
        user.setPasswordHash("$2a$10$defaultDoctorPasswordHash"); // 设置默认密码
        user.setRole(com.healthfamily.domain.constant.UserRole.DOCTOR);
        User savedUser = userRepository.save(user);
        
        return savedUser;
    }

    @Override
    /**
     * 更新
     * @param id 业务对象唯一标识
     * @param doctor 业务参数
     * @return 业务返回结果
     */
    public User update(Long id, User doctor) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null || existingUser.getRole() != com.healthfamily.domain.constant.UserRole.DOCTOR) {
            return null;
        }
        
        // 更新医生信息
        existingUser.setNickname(doctor.getNickname());
        existingUser.setPhone(doctor.getPhone());
        existingUser.setUpdatedAt(java.time.LocalDateTime.now());
        
        User updatedUser = userRepository.save(existingUser);
        
        return updatedUser;
    }

    @Override
    /**
     * 删除
     * @param id 业务对象唯一标识
     * @return 业务返回结果
     */
    public boolean deleteById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
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
        User user = userRepository.findById(id).orElse(null);
        if (user == null || user.getRole() != com.healthfamily.domain.constant.UserRole.DOCTOR) {
            return false;
        }
        
        // 更新用户状态
        int statusValue = "1".equals(status) || "enabled".equalsIgnoreCase(status) ? 1 : 0;
        user.setStatus(statusValue);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    /**
     * 将User和DoctorProfile转换为AdminDoctorDto
     */
    private AdminDoctorDto toAdminDoctorDto(User user, DoctorProfile profile) {
        if (user == null) {
            return null;
        }
        
        return new AdminDoctorDto(
            user.getId(),
            user.getNickname(),
            user.getPhone(),
            profile != null ? profile.getEmail() : null,
            profile != null ? profile.getHospital() : null,
            profile != null ? profile.getDepartment() : null,
            profile != null ? profile.getSpecialty() : null,
            profile != null ? profile.getTitle() : null,
            profile != null ? profile.getBio() : null,
            profile != null && profile.isCertified(),
            profile != null ? profile.getCertificationStatus() : "PENDING",
            user.getStatus(),
            profile != null && profile.getRating() != null ? profile.getRating() : java.math.BigDecimal.ZERO,
            profile != null && profile.getRatingCount() != null ? profile.getRatingCount() : 0,
            profile != null && profile.getServiceCount() != null ? profile.getServiceCount() : 0,
            user.getCreatedAt(),
            user.getUpdatedAt(),
            profile != null ? profile.getCertifiedAt() : null,
            profile != null ? profile.getRejectReason() : null
        );
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 获取
     * @param id 业务对象唯一标识
     * @return 业务返回结果
     */
    public AdminDoctorDto getAdminDoctorById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null || user.getRole() != com.healthfamily.domain.constant.UserRole.DOCTOR) {
            return null;
        }
        
        DoctorProfile profile = doctorProfileRepository.findByDoctorId(id).orElse(null);
        return toAdminDoctorDto(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 获取
     * @return 业务返回结果
     */
    public Map<String, Object> getAdminDoctorStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总医生数
        long totalDoctors = userRepository.findAll().stream()
            .filter(user -> user.getRole() == com.healthfamily.domain.constant.UserRole.DOCTOR)
            .count();
        stats.put("totalDoctors", totalDoctors);
        
        // 已认证医生数
        long certifiedDoctors = doctorProfileRepository.findByCertificationStatus("APPROVED").size();
        stats.put("certifiedDoctors", certifiedDoctors);
        
        // 待审核医生数
        long pendingDoctors = doctorProfileRepository.findByCertificationStatus("PENDING").size();
        stats.put("pendingDoctors", pendingDoctors);
        
        // 服务用户数（统计所有医生服务过的家庭数，去重）
        long serviceUsers = familyDoctorRepository.findAll().stream()
            .map(FamilyDoctor::getFamily)
            .map(Family::getId)
            .distinct()
            .count();
        stats.put("serviceUsers", serviceUsers);
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * 获取
     * @param page 分页页码
     * @param size 分页大小
     * @return 业务返回结果
     */
    public java.util.List<AdminDoctorDto> getPendingDoctors(int page, int size) {
        List<DoctorProfile> pendingProfiles = doctorProfileRepository.findByCertificationStatus("PENDING");
        
        List<AdminDoctorDto> result = new ArrayList<>();
        for (DoctorProfile profile : pendingProfiles) {
            User user = userRepository.findById(profile.getDoctorId()).orElse(null);
            if (user != null) {
                result.add(toAdminDoctorDto(user, profile));
            }
        }
        
        // 简单分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, result.size());
        if (start >= result.size()) {
            return java.util.Collections.emptyList();
        }
        return result.subList(start, end);
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param doctorId 医生唯一标识
     * @param adminId 业务对象唯一标识
     * @return 业务返回结果
     */
    public boolean approveDoctor(Long doctorId, Long adminId) {
        DoctorProfile profile = doctorProfileRepository.findByDoctorId(doctorId)
            .orElseThrow(() -> new BusinessException(40401, "医生信息不存在"));
        
        profile.setCertificationStatus("APPROVED");
        profile.setCertifiedAt(java.time.LocalDateTime.now());
        profile.setCertifiedBy(adminId);
        profile.setRejectReason(null);
        doctorProfileRepository.save(profile);
        
        return true;
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param doctorId 医生唯一标识
     * @param adminId 业务对象唯一标识
     * @param rejectReason 业务参数
     * @return 业务返回结果
     */
    public boolean rejectDoctor(Long doctorId, Long adminId, String rejectReason) {
        DoctorProfile profile = doctorProfileRepository.findByDoctorId(doctorId)
            .orElseThrow(() -> new BusinessException(40401, "医生信息不存在"));
        
        profile.setCertificationStatus("REJECTED");
        profile.setCertifiedBy(adminId);
        profile.setRejectReason(rejectReason);
        doctorProfileRepository.save(profile);
        
        return true;
    }

    @Override
    @Transactional
    /**
     * 更新
     * @param id 业务对象唯一标识
     * @param certified 业务参数
     * @return 业务返回结果
     */
    public boolean updateDoctorCertification(Long id, Boolean certified) {
        DoctorProfile profile = doctorProfileRepository.findByDoctorId(id).orElse(null);
        if (profile == null) {
            return false;
        }
        
        if (certified) {
            profile.setCertificationStatus("APPROVED");
            if (profile.getCertifiedAt() == null) {
                profile.setCertifiedAt(java.time.LocalDateTime.now());
            }
        } else {
            profile.setCertificationStatus("REJECTED");
        }
        doctorProfileRepository.save(profile);
        return true;
    }

    @Override
    @Transactional
    /**
     * 注册
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public User registerDoctor(DoctorRegisterRequest request) {
        // 检查手机号是否已注册
        userRepository.findByPhone(request.phone())
            .ifPresent(user -> {
                throw new BusinessException(40001, "手机号已注册");
            });
        
        // 创建用户
        User user = User.builder()
            .phone(request.phone())
            .nickname(request.name())
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(com.healthfamily.domain.constant.UserRole.DOCTOR)
            .status(0)  // 注册时状态为禁用，待审核通过后启用
            .build();
        
        User savedUser = userRepository.save(user);
        
        // 创建医生扩展信息（待审核状态）
        DoctorProfile profile = new DoctorProfile();
        profile.setDoctor(savedUser);
        profile.setHospital(request.hospital());
        profile.setDepartment(request.department());
        profile.setSpecialty(request.specialty());
        profile.setTitle(request.title());
        profile.setBio(request.bio());
        profile.setEmail(request.email());
        profile.setCertificationStatus("PENDING");
        profile.setLicenseNumber(request.licenseNumber());
        profile.setLicenseImage(request.licenseImage());
        profile.setIdCard(request.idCard());
        profile.setIdCardFront(request.idCardFront());
        profile.setIdCardBack(request.idCardBack());
        profile.setRating(java.math.BigDecimal.ZERO);
        profile.setRatingCount(0);
        profile.setServiceCount(0);
        
        doctorProfileRepository.save(profile);
        
        return savedUser;
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param userId 家庭成员唯一标识
     * @param doctorId 医生唯一标识
     * @param rating 业务参数
     * @param comment 业务参数
     * @return 无
     */
    public void rateDoctor(Long userId, Long doctorId, Integer rating, String comment) {
        // 1. 校验评分
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }

        // 2. 校验用户和医生是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        
        if (doctor.getRole() != com.healthfamily.domain.constant.UserRole.DOCTOR) {
            throw new BusinessException(400, "被评价用户不是医生");
        }

        // 3. 校验是否有权评价 (必须是已签约或曾经签约/咨询过的关系)
        // 简化逻辑：只要是系统用户且医生存在，即可评价 (实际业务可能需要检查 FamilyDoctor 关系)
        // 严格模式：
        // boolean hasRelation = familyDoctorRepository.existsByDoctorAndFamily_Members_User(doctor, user);
        // 这里采用宽松模式，假设前端入口已控制

        // 4. 保存评价
        com.healthfamily.domain.entity.DoctorRating ratingEntity = com.healthfamily.domain.entity.DoctorRating.builder()
                .doctorId(doctorId)
                .userId(userId)
                .rating(rating)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        
        doctorRatingRepository.save(ratingEntity);

        // 5. 更新医生平均分
        updateDoctorAverageRating(doctorId);
    }

    private void updateDoctorAverageRating(Long doctorId) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId).orElseGet(() -> {
            User doctor = userRepository.findById(doctorId).orElse(null);
            if (doctor == null) return null;
            
            DoctorProfile newProfile = new DoctorProfile();
            newProfile.setDoctor(doctor);
            newProfile.setCertificationStatus("APPROVED"); // 默认状态，或者根据业务逻辑设置
            newProfile.setRating(BigDecimal.ZERO);
            newProfile.setRatingCount(0);
            newProfile.setServiceCount(0);
            return newProfile;
        });
        
        if (profile == null) return;

        List<com.healthfamily.domain.entity.DoctorRating> ratings = doctorRatingRepository.findByDoctorId(doctorId);
        if (ratings.isEmpty()) {
            profile.setRating(BigDecimal.ZERO);
            profile.setRatingCount(0);
        } else {
            double avg = ratings.stream().mapToInt(com.healthfamily.domain.entity.DoctorRating::getRating).average().orElse(0.0);
            profile.setRating(BigDecimal.valueOf(avg));
            profile.setRatingCount(ratings.size());
        }
        doctorProfileRepository.save(profile);
    }

    @Override
    /**
     * 获取
     * @param doctorId 医生唯一标识
     * @return 业务返回结果
     */
    public List<com.healthfamily.web.dto.DoctorRatingResponse> getDoctorRatings(Long doctorId) {
        return doctorRatingRepository.findByDoctorId(doctorId).stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .map(r -> {
                User u = userRepository.findById(r.getUserId()).orElse(null);
                String name = u != null ? u.getNickname() : "未知用户";
                String avatar = readAvatar(r.getUserId());
                return new com.healthfamily.web.dto.DoctorRatingResponse(
                    r.getId(),
                    name,
                    avatar,
                    r.getRating(), 
                    r.getComment(), 
                    r.getCreatedAt()
                );
            })
            .collect(Collectors.toList());
    }

    // ==================== 新版工作台 V2 ====================

    @Override
    public com.healthfamily.web.dto.DoctorWorkbenchDto getWorkbenchDashboard(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));

        // 1. 获取医生管理的所有家庭
        List<FamilyDoctor> bindings = familyDoctorRepository.findByDoctor_Id(doctorId);
        List<Family> families = bindings.stream().map(FamilyDoctor::getFamily).collect(Collectors.toList());
        List<Long> familyIds = families.stream().map(Family::getId).collect(Collectors.toList());

        // 2. 计算顶层KPI
        int managedFamilies = bindings.size();
        int totalPatients = (int) familyMemberRepository.countByFamilyIdIn(familyIds);
        int pendingAlerts = monitoringAlertRepository.countByDoctorIdAndStatus(doctorId, AlertStatus.PENDING);
        int monthlyReports = healthReportRepository.countByCreatedAtAfter(LocalDate.now().withDayOfMonth(1).atStartOfDay());

        // 3. 基础统计
        DoctorStatsResponse stats = getStatistics(doctorId, null, LocalDate.now().minusDays(29), LocalDate.now());

        // 4. 获取高风险患者 (Top 5)，复用高风险成员结构，包含体征与中医体质风险详情
        List<HighRiskMemberDto> criticalPatients = getHighRiskMembersForWorkbench(doctorId);

        // 5. 汇总最近异常事件（近7天，跨家庭）
        List<AbnormalEventDto> abnormalEvents = buildDoctorAbnormalEvents(families);

        // 6. 获取待处理咨询 (Top 5)
        List<ConsultationTaskDto> pendingConsultations = getPendingConsultations(doctor);

        // 7. 获取今日中医时令建议（接入 Spring AI 的 SeasonalWellnessService）
        SeasonalWellnessDTO wellnessDTO = null;
        String solarTerm;
        String seasonalAdvice;
        try {
            wellnessDTO = seasonalWellnessService.getWellnessAdvice(doctorId);
        } catch (Exception e) {
            log.warn("Failed to load AI seasonal wellness advice for doctor {}, fallback to simple message: {}", doctorId, e.getMessage());
        }
        if (wellnessDTO != null) {
            solarTerm = wellnessDTO.getSolarTerm() != null ? wellnessDTO.getSolarTerm() : com.healthfamily.utils.SolarTermUtil.getCurrentSolarTerm();
            seasonalAdvice = wellnessDTO.getAdvice() != null
                    ? wellnessDTO.getAdvice()
                    : String.format("时值【%s】节气，建议根据节气特点调整生活作息和饮食习惯。", solarTerm);
        } else {
            solarTerm = com.healthfamily.utils.SolarTermUtil.getCurrentSolarTerm();
            seasonalAdvice = String.format("时值【%s】节气，建议根据节气特点调整生活作息和饮食习惯。", solarTerm);
        }

        return DoctorWorkbenchDto.builder()
                .managedFamilies(managedFamilies)
                .totalPatients(totalPatients)
                .pendingAlerts(pendingAlerts)
                .monthlyReports(monthlyReports)
                .seasonalSolarTerm(solarTerm)
                .stats(stats)
                .abnormalEvents(abnormalEvents)
                .criticalPatients(criticalPatients)
                .pendingConsultations(pendingConsultations)
                .seasonalAdvice(seasonalAdvice)
                .build();
    }

    /**
     * 构建医生视角的异常事件列表（跨家庭，近7天，包含日志异常、预警和异常提醒）
     */
    private List<AbnormalEventDto> buildDoctorAbnormalEvents(List<Family> families) {
        if (families == null || families.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> familyIds = families.stream().map(Family::getId).collect(Collectors.toList());
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<AbnormalEventDto> events = new ArrayList<>();

        // 1. 来自 HealthAlert 的预警（PENDING / ESCALATED）
        for (Long familyId : familyIds) {
            List<HealthAlert> recentAlerts = healthAlertRepository.findByFamily_IdOrderByCreatedAtDesc(familyId)
                    .stream()
                    .filter(a -> a.getCreatedAt() != null && !a.getCreatedAt().isBefore(sevenDaysAgo))
                    .filter(a -> a.getStatus() == AlertStatus.PENDING || a.getStatus() == AlertStatus.ESCALATED)
                    .limit(20)
                    .collect(Collectors.toList());

            for (HealthAlert alert : recentAlerts) {
                User user = alert.getUser();
                events.add(new AbnormalEventDto(
                        String.valueOf(alert.getId()),
                        "ALERT",
                        alert.getMetric() + "异常预警",
                        alert.getMessage(),
                        alert.getSeverity() != null ? alert.getSeverity().name() : null,
                        alert.getCreatedAt(),
                        alert.getStatus() != null ? alert.getStatus().name() : null,
                        user != null ? user.getId() : null
                ));
            }
        }

        // 2. 按时间倒序取前10条
        return events.stream()
                .sorted(Comparator.comparing(AbnormalEventDto::time, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 构建医生工作台使用的高风险患者列表（Top 5）
     * 逻辑：
     * - 体征风险：近3天体征日志中存在血压/血糖/心率异常
     * - 中医风险：最新体质为痰湿质/湿热质/血瘀质/特禀质
     * - 聚合后按风险等级 + 风险时间排序
     */
    private List<HighRiskMemberDto> getHighRiskMembersForWorkbench(Long doctorId) {
        // 1. 获取医生管理的所有家庭与成员
        List<FamilyDoctor> bindings = familyDoctorRepository.findByDoctor_Id(doctorId);
        List<Family> families = bindings.stream()
                .map(FamilyDoctor::getFamily)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (families.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> userFamilyNameMap = new HashMap<>();
        List<User> patients = new ArrayList<>();
        for (Family family : families) {
            List<FamilyMember> members = familyMemberRepository.findByFamily(family);
            for (FamilyMember member : members) {
                if (member.getUser() == null) {
                    continue;
                }
                User user = member.getUser();
                patients.add(user);
                userFamilyNameMap.putIfAbsent(user.getId(), family.getName());
            }
        }
        List<Long> patientIds = patients.stream().map(User::getId).distinct().toList();
        if (patientIds.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

        // 2. 预加载每个患者最近3天体征日志（体征风险）
        Map<Long, List<HealthLog>> patientLogsMap = new HashMap<>();
        for (Long patientId : patientIds) {
            List<HealthLog> logs = healthLogRepository
                    .findByUser_IdAndTypeOrderByLogDateDesc(patientId, com.healthfamily.domain.constant.HealthLogType.VITALS)
                    .stream()
                    .filter(l -> !l.getLogDate().isBefore(threeDaysAgo))
                    .collect(Collectors.toList());
            if (!logs.isEmpty()) {
                patientLogsMap.put(patientId, logs);
            }
        }

        // 3. 预加载中医体质结果（中医风险）
        List<User> patientEntities = userRepository.findAllById(patientIds);
        Map<Long, ConstitutionAssessment> latestTcmMap = new HashMap<>();
        for (User patient : patientEntities) {
            ConstitutionAssessment latest = assessmentRepository.findByUserOrderByCreatedAtDesc(patient).stream()
                    .findFirst()
                    .orElse(null);
            if (latest != null) {
                latestTcmMap.put(patient.getId(), latest);
            }
        }

        List<HighRiskMemberDto> result = new ArrayList<>();

        for (User patient : patientEntities) {
            Long userId = patient.getId();

            // ---------- 体征风险 ----------
            String vitalsRiskType = null;
            String vitalsRiskDesc = null;
            String vitalsRiskLevel = null;
            LocalDateTime vitalsRiskTime = null;
            boolean vitalsImmediate = false;
            int highBpCount = 0;

            List<HealthLog> logs = patientLogsMap.get(userId);
            if (logs != null && !logs.isEmpty()) {
                for (HealthLog log : logs) {
                    Map<String, Object> content = safeParse(log.getContentJson());
                    if (content.isEmpty()) {
                        continue;
                    }

                    Double systolic = extractDouble(content.get("systolic"));
                    Double diastolic = extractDouble(content.get("diastolic"));
                    Double glucose = extractDouble(content.getOrDefault("glucose", content.get("bloodSugar")));
                    Double heartRate = extractDouble(content.getOrDefault("heartRate", content.get("hr")));

                    String currentLevel = null;
                    StringBuilder currentDescBuilder = new StringBuilder();
                    boolean currentImmediate = false;

                    // 血压逻辑：>140/90 预警，>180 或 >110 危急；连续3次高血压标记需要立即干预
                    if (systolic != null || diastolic != null) {
                        boolean highBp = (systolic != null && systolic > 140) || (diastolic != null && diastolic > 90);
                        if (highBp) {
                            highBpCount++;
                            currentLevel = "WARNING";
                            currentDescBuilder.append(String.format("血压偏高：收缩压%.0f/舒张压%.0f mmHg",
                                    systolic != null ? systolic : 0,
                                    diastolic != null ? diastolic : 0));
                        }
                        if ((systolic != null && systolic > 180) || (diastolic != null && diastolic > 110)) {
                            currentLevel = "CRITICAL";
                            currentImmediate = true;
                            currentDescBuilder.setLength(0);
                            currentDescBuilder.append(String.format("血压严重升高：收缩压%.0f/舒张压%.0f mmHg",
                                    systolic != null ? systolic : 0,
                                    diastolic != null ? diastolic : 0));
                        }
                    }

                    // 血糖：简单使用硬编码阈值（可后续接入 HealthThreshold 个性化）
                    if (glucose != null && glucose > 11.1) {
                        if (currentLevel == null || "NOTICE".equals(currentLevel)) {
                            currentLevel = "WARNING";
                        }
                        if (currentDescBuilder.length() > 0) {
                            currentDescBuilder.append("；");
                        }
                        currentDescBuilder.append(String.format("血糖偏高：%.1f mmol/L", glucose));
                    }

                    // 心率
                    if (heartRate != null && (heartRate > 120 || heartRate < 50)) {
                        if (currentLevel == null || "NOTICE".equals(currentLevel)) {
                            currentLevel = "WARNING";
                        }
                        if (currentDescBuilder.length() > 0) {
                            currentDescBuilder.append("；");
                        }
                        currentDescBuilder.append(String.format("心率异常：%.0f 次/分", heartRate));
                    }

                    if (currentLevel != null) {
                        // 使用日志创建时间作为异常时间，如果缺失则退回到日志日期的起始时间
                        LocalDateTime currentTime = log.getCreatedAt() != null
                                ? log.getCreatedAt()
                                : log.getLogDate().atStartOfDay();
                        if (vitalsRiskLevel == null
                                || compareRiskLevel(currentLevel, vitalsRiskLevel) > 0
                                || (compareRiskLevel(currentLevel, vitalsRiskLevel) == 0
                                    && (vitalsRiskTime == null || currentTime.isAfter(vitalsRiskTime)))) {
                            vitalsRiskType = "VITALS_WARNING";
                            vitalsRiskLevel = currentLevel;
                            vitalsRiskDesc = currentDescBuilder.toString();
                            vitalsRiskTime = currentTime;
                            vitalsImmediate = currentImmediate;
                        }
                    }
                }

                if (highBpCount >= 3 && vitalsRiskLevel != null) {
                    vitalsImmediate = true;
                    if (vitalsRiskDesc != null && !vitalsRiskDesc.contains("连续")) {
                        vitalsRiskDesc = vitalsRiskDesc + "；近3次记录均为高血压，请尽快复查";
                    }
                }
            }

            // ---------- 中医风险 ----------
            String tcmRiskType = null;
            String tcmRiskDesc = null;
            String tcmRiskLevel = null;
            LocalDateTime tcmRiskTime = null;
            boolean tcmImmediate = false;

            ConstitutionAssessment tcm = latestTcmMap.get(userId);
            if (tcm != null && tcm.getPrimaryType() != null) {
                String primaryType = tcm.getPrimaryType();
                List<String> highRiskTcm = List.of("痰湿质", "湿热质", "血瘀质", "特禀质");
                if (highRiskTcm.contains(primaryType)) {
                    tcmRiskType = "TCM_IMBALANCE";
                    tcmRiskLevel = "WARNING";
                    tcmRiskTime = tcm.getCreatedAt();
                    tcmRiskDesc = "中医体质为" + primaryType + "，建议调整饮食与作息并加强随访";
                    tcmImmediate = false;
                }
            }

            // ---------- 聚合体征与中医风险 ----------
            String finalRiskType = null;
            String finalRiskDesc = null;
            String finalRiskLevel = null;
            LocalDateTime finalRiskTime = null;
            boolean finalImmediate = false;

            if (vitalsRiskLevel == null && tcmRiskLevel == null) {
                continue;
            } else if (vitalsRiskLevel != null && tcmRiskLevel == null) {
                finalRiskType = vitalsRiskType;
                finalRiskDesc = vitalsRiskDesc;
                finalRiskLevel = vitalsRiskLevel;
                finalRiskTime = vitalsRiskTime;
                finalImmediate = vitalsImmediate;
            } else if (vitalsRiskLevel == null && tcmRiskLevel != null) {
                finalRiskType = tcmRiskType;
                finalRiskDesc = tcmRiskDesc;
                finalRiskLevel = tcmRiskLevel;
                finalRiskTime = tcmRiskTime;
                finalImmediate = tcmImmediate;
            } else {
                int cmp = compareRiskLevel(vitalsRiskLevel, tcmRiskLevel);
                boolean vitalsHigher = cmp > 0 || (cmp == 0 && vitalsRiskTime != null && (tcmRiskTime == null || vitalsRiskTime.isAfter(tcmRiskTime)));
                if (vitalsHigher) {
                    finalRiskType = vitalsRiskType;
                    finalRiskLevel = vitalsRiskLevel;
                    finalRiskTime = vitalsRiskTime;
                    finalImmediate = vitalsImmediate;
                    if (tcmRiskDesc != null) {
                        finalRiskDesc = vitalsRiskDesc != null
                                ? vitalsRiskDesc + "；" + tcmRiskDesc
                                : tcmRiskDesc;
                    } else {
                        finalRiskDesc = vitalsRiskDesc;
                    }
                } else {
                    finalRiskType = tcmRiskType;
                    finalRiskLevel = tcmRiskLevel;
                    finalRiskTime = tcmRiskTime;
                    finalImmediate = tcmImmediate;
                    if (vitalsRiskDesc != null) {
                        finalRiskDesc = tcmRiskDesc != null
                                ? tcmRiskDesc + "；" + vitalsRiskDesc
                                : vitalsRiskDesc;
                    } else {
                        finalRiskDesc = tcmRiskDesc;
                    }
                }
            }

            // 4. 构建标签与DTO
            List<String> tags = new ArrayList<>();
            Profile profile = profileRepository.findById(userId).orElse(null);
            if (profile != null) {
                try {
                    if (profile.getHealthTags() != null && !profile.getHealthTags().isBlank()) {
                        List<String> healthTags = objectMapper.readValue(profile.getHealthTags(), STRING_LIST_TYPE_REF);
                        if (healthTags != null) {
                            tags.addAll(healthTags);
                        }
                    }
                    if (profile.getAllergies() != null && !profile.getAllergies().isBlank()) {
                        List<String> allergies = objectMapper.readValue(profile.getAllergies(), STRING_LIST_TYPE_REF);
                        if (allergies != null) {
                            tags.addAll(allergies.stream().map(a -> "过敏:" + a).collect(Collectors.toList()));
                        }
                    }
                } catch (Exception e) {
                    // 标签解析失败不影响高风险判断
                }
            }
            if (tags.isEmpty()) {
                tags.add("高风险");
            }

            LocalDateTime lastAbnormalTime = Stream.of(vitalsRiskTime, tcmRiskTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            result.add(new HighRiskMemberDto(
                    userId,
                    // 医生工作台按家庭维度展示高风险患者，这里取该患者绑定的第一个家庭ID
                    bindings.stream()
                            .map(FamilyDoctor::getFamily)
                            .filter(Objects::nonNull)
                            .map(Family::getId)
                            .findFirst()
                            .orElse(null),
                    patient.getNickname(),
                    userFamilyNameMap.getOrDefault(userId, null),
                    tags,
                    lastAbnormalTime,
                    readAvatar(userId),
                    finalRiskType,
                    finalRiskDesc,
                    finalRiskLevel,
                    finalRiskTime,
                    finalImmediate
            ));
        }

        // 5. 按风险等级 + 风险时间排序（CRITICAL > WARNING > NOTICE）并限制 Top 5
        result.sort((a, b) -> {
            int levelA = mapRiskLevelOrder(a.riskLevel());
            int levelB = mapRiskLevelOrder(b.riskLevel());
            if (levelA != levelB) {
                return Integer.compare(levelB, levelA);
            }
            LocalDateTime timeA = a.riskTime();
            LocalDateTime timeB = b.riskTime();
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        return result.stream().limit(5).collect(Collectors.toList());
    }

    private List<ConsultationTaskDto> getPendingConsultations(User doctor) {
        List<ConsultationSession> allSessions = consultationSessionRepository.findByDoctorOrderByLastMessageAtDesc(doctor);
        return allSessions.stream()
                .filter(session -> session != null && session.getPatient() != null && (session.getLastMessageAt() != null || session.getCreatedAt() != null))
                .filter(session -> "ACTIVE".equals(session.getStatus()))
                .filter(session -> session.getUnreadCountDoctor() != null && session.getUnreadCountDoctor() > 0)
                // 等待时长按最后一条消息时间计算；若为空则退回创建时间
                .sorted(Comparator.comparing(session ->
                        session.getLastMessageAt() != null ? session.getLastMessageAt() : session.getCreatedAt()
                ))
                .map(session -> {
                    User patient = session.getPatient();
                    LocalDateTime referenceTime = session.getLastMessageAt() != null
                            ? session.getLastMessageAt()
                            : session.getCreatedAt();
                    long waitingMinutes = java.time.Duration.between(referenceTime, LocalDateTime.now()).toMinutes();
                    String waitingTimeFormatted = formatDuration(waitingMinutes);

                    return ConsultationTaskDto.builder()
                            .sessionId(session.getId())
                            .patientName(patient.getNickname())
                            .avatarUrl(readAvatar(patient.getId()))
                            .requestSummary(session.getTriageSummary() != null ? session.getTriageSummary() : session.getPatientSymptoms()) // 优先使用AI摘要
                            .suggestedDepartment("待分诊") // 暂时没有具体科室信息
                            .waitingTime(waitingTimeFormatted)
                            .build();
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    private String formatDuration(long minutes) {
        if (minutes < 60) {
            return minutes + "分钟";
        } else if (minutes < 1440) {
            return (minutes / 60) + "小时" + (minutes % 60) + "分钟";
        } else {
            long days = minutes / 1440;
            long hours = (minutes % 1440) / 60;
            long mins = minutes % 60;
            return days + "天" + hours + "小时" + mins + "分钟";
        }
    }

    @Override
    public int sendSeasonalAdviceToAllPatients(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生用户不存在"));

        SeasonalWellnessDTO wellnessDTO = null;
        String solarTerm;
        String advice;
        try {
            wellnessDTO = seasonalWellnessService.getWellnessAdvice(doctorId);
        } catch (Exception e) {
            log.warn("Failed to load AI seasonal wellness advice for doctor {} when sending to patients: {}", doctorId, e.getMessage());
        }
        if (wellnessDTO != null) {
            solarTerm = wellnessDTO.getSolarTerm() != null ? wellnessDTO.getSolarTerm() : com.healthfamily.utils.SolarTermUtil.getCurrentSolarTerm();
            advice = wellnessDTO.getAdvice() != null
                    ? wellnessDTO.getAdvice()
                    : String.format("时值【%s】节气，建议根据节气特点调整生活作息和饮食习惯。", solarTerm);
        } else {
            solarTerm = com.healthfamily.utils.SolarTermUtil.getCurrentSolarTerm();
            advice = String.format("时值【%s】节气，建议根据节气特点调整生活作息和饮食习惯。", solarTerm);
        }

        List<FamilyDoctor> bindings = familyDoctorRepository.findByDoctor_Id(doctorId);
        if (bindings.isEmpty()) {
            return 0;
        }

        LocalDateTime scheduledTime = LocalDateTime.now().plusMinutes(5);
        int created = 0;

        for (FamilyDoctor binding : bindings) {
            Family family = binding.getFamily();
            List<com.healthfamily.domain.entity.FamilyMember> members = familyMemberRepository.findByFamily(family);
            for (com.healthfamily.domain.entity.FamilyMember fm : members) {
                User patient = fm.getUser();
                if (patient == null) {
                    continue;
                }
                // 不给医生自己发
                if (Objects.equals(patient.getId(), doctorId)) {
                    continue;
                }

                ReminderRequest request = new ReminderRequest(
                        "LIFESTYLE",
                        "今日时令养生小贴士",
                        advice,
                        scheduledTime,
                        "MEDIUM",
                        "APP",
                        java.util.Map.of("solarTerm", solarTerm),
                        java.util.Map.of(
                                "source", "DOCTOR_SEASONAL_ADVICE",
                                "doctorId", doctorId,
                                "familyId", family.getId(),
                                "patientUserId", patient.getId()
                        ),
                        patient.getId(),
                        family.getId()
                );
                try {
                    healthReminderService.createReminder(doctorId, request);
                    created++;
                } catch (Exception e) {
                    log.error("Failed to create seasonal reminder for patient {}: {}", patient.getId(), e.getMessage(), e);
                }
            }
        }

        log.info("Doctor {} sent seasonal advice to {} patients", doctorId, created);
        return created;
    }

    private boolean checkAnomaly(Long userId, Map<String, Object> content) {
        if (content == null) return false;
        
        // 血压
        if (content.containsKey("systolic")) {
            Double val = extractDouble(content.get("systolic"));
            if (val != null && healthDataAiService.detectAnomaly(userId, "血压_收缩压", val, null).isAnomaly()) return true;
        }
        if (content.containsKey("diastolic")) {
            Double val = extractDouble(content.get("diastolic"));
            if (val != null && healthDataAiService.detectAnomaly(userId, "血压_舒张压", val, null).isAnomaly()) return true;
        }
        
        // 血糖
        Object glucose = content.getOrDefault("glucose", content.get("bloodSugar"));
        if (glucose != null) {
            Double val = extractDouble(glucose);
            if (val != null && healthDataAiService.detectAnomaly(userId, "血糖_空腹", val, null).isAnomaly()) return true;
        }
        
        // 心率
        Object hr = content.getOrDefault("heartRate", content.get("hr"));
        if (hr != null) {
            Double val = extractDouble(hr);
            if (val != null && healthDataAiService.detectAnomaly(userId, "心率", val, null).isAnomaly()) return true;
        }
        
        // 体温
        Object temp = content.getOrDefault("temperature", content.get("temp"));
        if (temp != null) {
            Double val = extractDouble(temp);
            if (val != null && healthDataAiService.detectAnomaly(userId, "体温", val, null).isAnomaly()) return true;
        }
        
        // 体重
        Object weight = content.getOrDefault("weight", content.get("val"));
        if (weight != null) {
            Double val = extractDouble(weight);
            if (val != null && healthDataAiService.detectAnomaly(userId, "体重", val, null).isAnomaly()) return true;
        }

        return false;
    }
    
    private Double extractDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            try { return Double.valueOf((String) obj); } catch (Exception e) {}
        }
        return null;
    }

    private String buildLogSummary(Map<String, Object> content) {
        if (content.containsKey("systolic")) return "血压";
        if (content.containsKey("glucose") || content.containsKey("bloodSugar")) return "血糖";
        if (content.containsKey("heartRate") || content.containsKey("hr")) return "心率";
        if (content.containsKey("temperature") || content.containsKey("temp")) return "体温";
        if (content.containsKey("weight") || content.containsKey("val")) return "体重";
        return "体征";
    }
}
