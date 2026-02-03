package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import com.healthfamily.domain.constant.ReminderPriority;
import com.healthfamily.domain.constant.ReminderStatus;
import com.healthfamily.domain.constant.ReminderType;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.HealthReminder;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.HealthReminderRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.FamilyMemberRepository;
import com.healthfamily.service.HealthReminderService;
import com.healthfamily.web.dto.ReminderRequest;
import com.healthfamily.web.dto.ReminderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReminderServiceImpl implements HealthReminderService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final HealthReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final HealthLogRepository healthLogRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Override
    @Transactional
    public ReminderResponse createReminder(Long userId, ReminderRequest request) {
        User creator = loadUser(userId);
        
        // 确定提醒的目标用户：如果指定了分配用户，则为目标用户；否则为创建者自己
        User targetUser = (request.assignedToUserId() != null) ? 
                loadUser(request.assignedToUserId()) : creator;

        HealthReminder reminder = HealthReminder.builder()
                .user(targetUser)  // 设置目标用户为提醒的用户
                .creator(creator)  // 设置创建者
                .type(ReminderType.valueOf(request.type()))
                .title(request.title())
                .content(request.content())
                .scheduledTime(request.scheduledTime())
                .status(ReminderStatus.PENDING)
                .priority(request.priority() != null ?
                        ReminderPriority.valueOf(request.priority()) : ReminderPriority.MEDIUM)
                .channel(request.channel())
                .triggerCondition(request.triggerCondition() != null ?
                        writeJsonSafely(request.triggerCondition()) : null)
                .metadataJson(request.metadata() != null ?
                        writeJsonSafely(request.metadata()) : null)
                .build();

        // 协作任务指派（可选）
        if (request.assignedToUserId() != null) {
            reminder.setAssignedTo(loadUser(request.assignedToUserId()));
        }
        
        // 家庭归属处理：优先使用请求中的familyId，如果没有则尝试从创建者和目标用户共同的家庭中获取
        if (request.familyId() != null) {
            com.healthfamily.domain.entity.Family family = familyRepository.findById(request.familyId())
                    .orElseThrow(() -> new RuntimeException("家庭不存在"));
            reminder.setFamily(family);
        } else {
            // 如果请求中没有指定familyId，尝试获取创建者和目标用户共同的家庭
            List<com.healthfamily.domain.entity.FamilyMember> creatorFamilies = familyMemberRepository.findByUser(creator);
            List<com.healthfamily.domain.entity.FamilyMember> targetUserFamilies = familyMemberRepository.findByUser(targetUser);
            
            // 找到创建者和目标用户共同的家庭
            Optional<com.healthfamily.domain.entity.Family> commonFamily = creatorFamilies.stream()
                    .map(fm -> fm.getFamily())
                    .filter(creatorFamily -> targetUserFamilies.stream()
                            .anyMatch(tfm -> tfm.getFamily().getId().equals(creatorFamily.getId())))
                    .findFirst();
            
            if (commonFamily.isPresent()) {
                // 如果找到共同家庭，使用它
                reminder.setFamily(commonFamily.get());
            } else {
                // 如果没有找到共同家庭，尝试获取目标用户所在的家庭
                List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(targetUser);
                if (!userFamilies.isEmpty()) {
                    // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                    Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                            .filter(fm -> fm.getFamily().getOwner().getId().equals(targetUser.getId()))
                            .findFirst();
                    
                    if (familyMember.isPresent()) {
                        reminder.setFamily(familyMember.get().getFamily());
                    } else {
                        reminder.setFamily(userFamilies.get(0).getFamily());
                    }
                }
            }
        }

        reminder = reminderRepository.save(reminder);
        return toResponse(reminder);
    }

    @Override
    public List<ReminderResponse> getUserReminders(Long userId, String status) {
        List<HealthReminder> reminders;

        if (status != null && !status.isEmpty()) {
            // 查询用户作为提醒拥有者或分配者的提醒
            reminders = reminderRepository.findByUser_IdOrAssignedTo_IdOrderByScheduledTimeAsc(userId, userId);
            // 过滤指定状态的提醒
            reminders = reminders.stream()
                    .filter(reminder -> reminder.getStatus() == ReminderStatus.valueOf(status))
                    // 仅返回分配给自己的任务，或者自己创建且未分配的任务
                    .filter(r -> {
                        if (r.getAssignedTo() != null) {
                            return r.getAssignedTo().getId().equals(userId);
                        }
                        return r.getUser().getId().equals(userId);
                    })
                    .collect(Collectors.toList());
        } else {
            // 查询用户作为提醒拥有者或分配者的提醒
            reminders = reminderRepository.findByUser_IdOrAssignedTo_IdOrderByScheduledTimeAsc(userId, userId);
            reminders = reminders.stream()
                    // 仅返回分配给自己的任务，或者自己创建且未分配的任务
                    .filter(r -> {
                        if (r.getAssignedTo() != null) {
                            return r.getAssignedTo().getId().equals(userId);
                        }
                        return r.getUser().getId().equals(userId);
                    })
                    .collect(Collectors.toList());
        }

        return reminders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReminderResponse> getUserTodoReminders(Long userId) {
        // 查询用户作为提醒拥有者或分配者的提醒
        List<HealthReminder> reminders = reminderRepository.findByUser_IdOrAssignedTo_IdOrderByScheduledTimeAsc(userId, userId);
        
        // 过滤 PENDING 和 SENT 状态的提醒（即待办事项），且必须是自己的任务
        return reminders.stream()
                .filter(reminder -> reminder.getStatus() == ReminderStatus.PENDING || 
                                  reminder.getStatus() == ReminderStatus.SENT)
                .filter(r -> {
                    if (r.getAssignedTo() != null) {
                        return r.getAssignedTo().getId().equals(userId);
                    }
                    return r.getUser().getId().equals(userId);
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReminderResponse> getFamilyReminders(Long userId, Long familyId) {
        // 验证用户是否是家庭成员
        com.healthfamily.domain.entity.Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("家庭不存在"));
        
        User user = loadUser(userId);
        
        // 检查用户角色，家庭管理员、医生和系统管理员可以查看家庭的所有提醒
        // 修改：为了支持家庭协作（督促功能），允许所有家庭成员查看家庭内的所有提醒
        boolean isFamilyMember = familyMemberRepository.findByFamilyAndUser(family, user)
                .isPresent();
        
        // 如果不是家庭成员且不是系统管理员/医生（假设医生/管理员可能有特殊权限跳过成员检查，但此处逻辑主要针对家庭上下文）
        if (!isFamilyMember && user.getRole() != com.healthfamily.domain.constant.UserRole.ADMIN && user.getRole() != com.healthfamily.domain.constant.UserRole.DOCTOR) {
             throw new RuntimeException("无权访问该家庭的提醒");
        }
        
        // 所有家庭成员都可以查看家庭中的所有提醒，以便进行协作
        List<HealthReminder> reminders = reminderRepository.findByFamily_IdOrderByScheduledTimeAsc(familyId);
        
        return reminders.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReminderResponse updateReminderStatus(Long userId, Long reminderId, String status) {
        HealthReminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("提醒不存在"));

        User user = loadUser(userId);
        if (!canManageReminder(user, reminder)) {
            throw new AccessDeniedException("无权操作");
        }

        reminder.setStatus(ReminderStatus.valueOf(status));
        if (status.equals("SENT")) {
            reminder.setActualTime(LocalDateTime.now());
        }

        reminder = reminderRepository.save(reminder);
        return toResponse(reminder);
    }

    @Override
    @Transactional
    public void deleteReminder(Long userId, Long reminderId) {
        HealthReminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("提醒不存在"));

        User user = loadUser(userId);
        if (!canManageReminder(user, reminder)) {
            throw new AccessDeniedException("无权操作");
        }

        reminderRepository.delete(reminder);
    }

    private boolean canManageReminder(User user, HealthReminder reminder) {
        if (user == null || reminder == null) return false;

        Long userId = user.getId();

        boolean isOwner = reminder.getUser() != null && Objects.equals(reminder.getUser().getId(), userId);
        boolean isAssigned = reminder.getAssignedTo() != null && Objects.equals(reminder.getAssignedTo().getId(), userId);
        boolean isCreator = reminder.getCreator() != null && Objects.equals(reminder.getCreator().getId(), userId);
        if (isOwner || isAssigned || isCreator) return true;

        if (user.getRole() == com.healthfamily.domain.constant.UserRole.ADMIN ||
                user.getRole() == com.healthfamily.domain.constant.UserRole.DOCTOR) {
            return true;
        }

        if (reminder.getFamily() == null) return false;

        if (reminder.getFamily().getOwner() != null && Objects.equals(reminder.getFamily().getOwner().getId(), userId)) {
            return true;
        }

        var fmOpt = familyMemberRepository.findByFamilyAndUser(reminder.getFamily(), user);
        boolean isFamilyMember = fmOpt.isPresent();
        boolean isFamilyMemberAdmin = fmOpt.map(fm -> Boolean.TRUE.equals(fm.getAdmin())).orElse(false);
        if (isFamilyMemberAdmin) return true;
        return user.getRole() == com.healthfamily.domain.constant.UserRole.FAMILY_ADMIN && isFamilyMember;
    }

    @Override
    @Transactional
    public List<ReminderResponse> generateSmartReminders(Long userId) {
        User user = loadUser(userId);
        List<ReminderResponse> reminders = new ArrayList<>();

        log.info("=== 开始生成智能提醒，用户ID: {} ===", userId);

        // 获取最近的异常数据（最近7天）
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        List<HealthLog> abnormalLogs = healthLogRepository
                .findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(userId)
                .stream()
                .filter(healthLog -> !healthLog.getLogDate().isBefore(sevenDaysAgo))
                .limit(10)
                .collect(Collectors.toList());

        log.info("找到 {} 条异常健康日志", abnormalLogs.size());

        // 为异常数据生成提醒
        for (HealthLog healthLog : abnormalLogs) {
            log.info("处理异常日志 ID: {}, 类型: {}, 日期: {}",
                    healthLog.getId(), healthLog.getType(), healthLog.getLogDate());

            // 检查是否已经为这条日志创建过提醒
            List<HealthReminder> existingReminders = reminderRepository
                    .findByUser_IdAndStatus(userId, ReminderStatus.PENDING)
                    .stream()
                    .filter(r -> {
                        try {
                            if (r.getMetadataJson() != null) {
                                Map<String, Object> metadata = objectMapper.readValue(
                                        r.getMetadataJson(),
                                        new TypeReference<Map<String, Object>>() {}
                                );
                                if (metadata.containsKey("logId")) {
                                    return healthLog.getId().equals(Long.valueOf(metadata.get("logId").toString()));
                                }
                            }
                        } catch (Exception e) {
                            log.warn("解析metadata失败: {}", e.getMessage());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            // 如果没有已存在的提醒，则创建新提醒
            if (existingReminders.isEmpty()) {
                log.info("为日志 ID {} 生成新提醒", healthLog.getId());

                String reminderContent = generateReminderContent(healthLog);

                if (reminderContent == null || reminderContent.trim().isEmpty()) {
                    log.warn("AI 生成的提醒内容为空，使用默认内容");
                    reminderContent = String.format(
                            "您的%s数据出现异常，建议2小时后复测，如有不适请及时就医。",
                            healthLog.getType().name()
                    );
                }

                HealthReminder reminder = HealthReminder.builder()
                        .user(user)
                        .creator(user)  // 设置创建者为当前用户
                        .type(ReminderType.ABNORMAL)
                        .title(healthLog.getType().name() + "数据异常提醒")
                        .content(reminderContent)
                        .scheduledTime(LocalDateTime.now().plusHours(2))
                        .status(ReminderStatus.PENDING)
                        .priority(ReminderPriority.HIGH)
                        .channel("APP")
                        .metadataJson(writeJsonSafely(Map.of(
                                "logId", healthLog.getId(),
                                "logDate", healthLog.getLogDate().toString(),
                                "logType", healthLog.getType().name()
                        )))
                        .build();
                
                // 自动关联到用户所在的家庭
                List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                if (!userFamilies.isEmpty()) {
                    // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                    Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                            .filter(fm -> fm.getFamily().getOwner().getId().equals(userId))
                            .findFirst();
                    
                    if (familyMember.isPresent()) {
                        reminder.setFamily(familyMember.get().getFamily());
                    } else {
                        reminder.setFamily(userFamilies.get(0).getFamily());
                    }
                }

                reminder = reminderRepository.save(reminder);
                reminders.add(toResponse(reminder));
                log.info("成功创建提醒 ID: {}", reminder.getId());
            } else {
                log.info("日志 ID {} 已存在提醒，跳过", healthLog.getId());
            }
        }

        // 检查各种健康维度的缺失数据，生成常规提醒
        generateHealthDimensionReminders(user, userId, reminders, user, false);

        log.info("=== 智能提醒生成完成，共生成 {} 条提醒 ===", reminders.size());
        return reminders;
    }

    @Override
    @Transactional
    public List<ReminderResponse> generateSmartRemindersForUser(Long adminId, Long targetUserId) {
        // 验证管理员权限
        User admin = loadUser(adminId);
        log.info("用户 {} 尝试为其他用户生成健康提醒，角色为 {}", adminId, admin.getRole());
        if (!hasReminderPermission(admin.getRole())) {
            log.warn("用户 {} (角色: {}) 无权为其他用户生成健康提醒", adminId, admin.getRole());
            throw new RuntimeException("无权为其他用户生成健康提醒");
        }
        log.info("用户 {} (角色: {}) 有权限为其他用户生成健康提醒", adminId, admin.getRole());
        
        // 验证目标用户是否在管理员的家庭中
        validateUserInFamily(adminId, targetUserId);
        
        User user = loadUser(targetUserId);
        List<ReminderResponse> reminders = new ArrayList<>();

        log.info("=== 开始为用户 {} 生成智能提醒，操作员ID: {} ===", targetUserId, adminId);

        // 获取目标用户的最近异常数据（最近7天）
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        List<HealthLog> abnormalLogs = healthLogRepository
                .findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(targetUserId)
                .stream()
                .filter(healthLog -> !healthLog.getLogDate().isBefore(sevenDaysAgo))
                .limit(10)
                .collect(Collectors.toList());

        log.info("找到 {} 条异常健康日志", abnormalLogs.size());

        // 为异常数据生成提醒
        for (HealthLog healthLog : abnormalLogs) {
            log.info("处理异常日志 ID: {}, 类型: {}, 日期: {}",
                    healthLog.getId(), healthLog.getType(), healthLog.getLogDate());

            // 检查是否已经为这条日志创建过提醒
            List<HealthReminder> existingReminders = reminderRepository
                    .findByUser_IdAndStatus(targetUserId, ReminderStatus.PENDING)
                    .stream()
                    .filter(r -> {
                        try {
                            if (r.getMetadataJson() != null) {
                                Map<String, Object> metadata = objectMapper.readValue(
                                        r.getMetadataJson(),
                                        new TypeReference<Map<String, Object>>() {}
                                );
                                if (metadata.containsKey("logId")) {
                                    return healthLog.getId().equals(Long.valueOf(metadata.get("logId").toString()));
                                }
                            }
                        } catch (Exception e) {
                            log.warn("解析metadata失败: {}", e.getMessage());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            // 如果没有已存在的提醒，则创建新提醒
            if (existingReminders.isEmpty()) {
                log.info("为日志 ID {} 生成新提醒", healthLog.getId());

                String reminderContent = generateReminderContent(healthLog);

                if (reminderContent == null || reminderContent.trim().isEmpty()) {
                    log.warn("AI 生成的提醒内容为空，使用默认内容");
                    reminderContent = String.format(
                            "您的%s数据出现异常，建议2小时后复测，如有不适请及时就医。",
                            healthLog.getType().name()
                    );
                }

                HealthReminder reminder = HealthReminder.builder()
                        .user(user)
                        .creator(admin)  // 设置创建者为操作员
                        .type(ReminderType.ABNORMAL)
                        .title(healthLog.getType().name() + "数据异常提醒")
                        .content(reminderContent)
                        .scheduledTime(LocalDateTime.now().plusHours(2))
                        .status(ReminderStatus.PENDING)
                        .priority(ReminderPriority.HIGH)
                        .channel("APP")
                        .metadataJson(writeJsonSafely(Map.of(
                                "logId", healthLog.getId(),
                                "logDate", healthLog.getLogDate().toString(),
                                "logType", healthLog.getType().name()
                        )))
                        .build();
                
                // 自动关联到管理员和目标用户共同的家庭
                List<com.healthfamily.domain.entity.FamilyMember> adminFamilies = familyMemberRepository.findByUser(admin);
                List<com.healthfamily.domain.entity.FamilyMember> targetUserFamilies = familyMemberRepository.findByUser(user);
                
                // 找到管理员和目标用户共同的家庭
                Optional<com.healthfamily.domain.entity.Family> commonFamily = adminFamilies.stream()
                        .map(fm -> fm.getFamily())
                        .filter(adminFamily -> targetUserFamilies.stream()
                                .anyMatch(targetFm -> targetFm.getFamily().getId().equals(adminFamily.getId())))
                        .findFirst();
                
                if (commonFamily.isPresent()) {
                    reminder.setFamily(commonFamily.get());
                } else {
                    // 如果没有找到共同家庭，使用目标用户所在的家庭
                    List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                    if (!userFamilies.isEmpty()) {
                        // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                        Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                                .filter(fm -> fm.getFamily().getOwner().getId().equals(targetUserId))
                                .findFirst();
                        
                        if (familyMember.isPresent()) {
                            reminder.setFamily(familyMember.get().getFamily());
                        } else {
                            reminder.setFamily(userFamilies.get(0).getFamily());
                        }
                    }
                }

                reminder = reminderRepository.save(reminder);
                reminders.add(toResponse(reminder));
                log.info("成功创建提醒 ID: {}", reminder.getId());
            } else {
                log.info("日志 ID {} 已存在提醒，跳过", healthLog.getId());
            }
        }

        // 检查各种健康维度的缺失数据，生成常规提醒
        generateHealthDimensionReminders(user, targetUserId, reminders, admin, true);

        log.info("=== 为用户 {} 生成智能提醒完成，共生成 {} 条提醒 ===", targetUserId, reminders.size());
        return reminders;
    }

    private void generateHealthDimensionReminders(User user, Long userId, List<ReminderResponse> reminders, User admin, boolean isForOtherUser) {
        LocalDate today = LocalDate.now();
        
        // 检查饮食数据
        List<HealthLog> dietLogs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(
                userId, com.healthfamily.domain.constant.HealthLogType.DIET);
        boolean hasRecentDietLog = dietLogs.stream()
                .anyMatch(log -> !log.getLogDate().isBefore(today.minusDays(1)));
        
        if (!hasRecentDietLog) {
            HealthReminder dietReminder = HealthReminder.builder()
                    .user(user)
                    .creator(admin)  // 设置创建者为操作员
                    .type(ReminderType.LIFESTYLE)
                    .title("饮食记录提醒")
                    .content("请记录今天的饮食情况，有助于了解营养摄入和健康管理。")
                    .scheduledTime(LocalDateTime.now().plusHours(1))
                    .status(ReminderStatus.PENDING)
                    .priority(ReminderPriority.MEDIUM)
                    .channel("APP")
                    .build();
            
            // 根据是否为其他用户生成提醒来决定关联的家庭
            if (isForOtherUser && admin != null) {
                // 为其他用户生成提醒，关联到管理员和目标用户共同的家庭
                List<com.healthfamily.domain.entity.FamilyMember> adminFamilies = familyMemberRepository.findByUser(admin);
                List<com.healthfamily.domain.entity.FamilyMember> targetUserFamilies = familyMemberRepository.findByUser(user);
                
                // 找到管理员和目标用户共同的家庭
                Optional<com.healthfamily.domain.entity.Family> commonFamily = adminFamilies.stream()
                        .map(fm -> fm.getFamily())
                        .filter(adminFamily -> targetUserFamilies.stream()
                                .anyMatch(targetFm -> targetFm.getFamily().getId().equals(adminFamily.getId())))
                        .findFirst();
                
                if (commonFamily.isPresent()) {
                    dietReminder.setFamily(commonFamily.get());
                } else {
                    // 如果没有找到共同家庭，使用目标用户所在的家庭
                    List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                    if (!userFamilies.isEmpty()) {
                        // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                        Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                                .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                                .findFirst();
                        
                        if (familyMember.isPresent()) {
                            dietReminder.setFamily(familyMember.get().getFamily());
                        } else {
                            dietReminder.setFamily(userFamilies.get(0).getFamily());
                        }
                    }
                }
            } else {
                // 为自己生成提醒，关联到用户所在的家庭
                List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                if (!userFamilies.isEmpty()) {
                    // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                    Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                            .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                            .findFirst();
                    
                    if (familyMember.isPresent()) {
                        dietReminder.setFamily(familyMember.get().getFamily());
                    } else {
                        dietReminder.setFamily(userFamilies.get(0).getFamily());
                    }
                }
            }
            dietReminder = reminderRepository.save(dietReminder);
            reminders.add(toResponse(dietReminder));
            log.info("创建了饮食记录提醒: {}", dietReminder.getId());
        }
        
        // 检查运动数据
        List<HealthLog> sportLogs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(
                userId, com.healthfamily.domain.constant.HealthLogType.SPORT);
        boolean hasRecentSportLog = sportLogs.stream()
                .anyMatch(log -> !log.getLogDate().isBefore(today.minusDays(1)));
        
        if (!hasRecentSportLog) {
            HealthReminder sportReminder = HealthReminder.builder()
                    .user(user)
                    .creator(admin)  // 设置创建者为操作员
                    .assignedTo(user) // 默认分配给用户自己
                    .type(ReminderType.LIFESTYLE)
                    .title("运动记录提醒")
                    .content("请记录今天的运动情况，保持适量运动有益健康。")
                    .scheduledTime(LocalDateTime.now().plusHours(1))
                    .status(ReminderStatus.PENDING)
                    .priority(ReminderPriority.MEDIUM)
                    .channel("APP")
                    .build();
            
            // 根据是否为其他用户生成提醒来决定关联的家庭
            if (isForOtherUser && admin != null) {
                // 为其他用户生成提醒，关联到管理员和目标用户共同的家庭
                List<com.healthfamily.domain.entity.FamilyMember> adminFamilies = familyMemberRepository.findByUser(admin);
                List<com.healthfamily.domain.entity.FamilyMember> targetUserFamilies = familyMemberRepository.findByUser(user);
                
                // 找到管理员和目标用户共同的家庭
                Optional<com.healthfamily.domain.entity.Family> commonFamily = adminFamilies.stream()
                        .map(fm -> fm.getFamily())
                        .filter(adminFamily -> targetUserFamilies.stream()
                                .anyMatch(targetFm -> targetFm.getFamily().getId().equals(adminFamily.getId())))
                        .findFirst();
                
                if (commonFamily.isPresent()) {
                    sportReminder.setFamily(commonFamily.get());
                } else {
                    // 如果没有找到共同家庭，使用目标用户所在的家庭
                    List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                    if (!userFamilies.isEmpty()) {
                        // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                        Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                                .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                                .findFirst();
                        
                        if (familyMember.isPresent()) {
                            sportReminder.setFamily(familyMember.get().getFamily());
                        } else {
                            sportReminder.setFamily(userFamilies.get(0).getFamily());
                        }
                    }
                }
            } else {
                // 为自己生成提醒，关联到用户所在的家庭
                List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                if (!userFamilies.isEmpty()) {
                    // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                    Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                            .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                            .findFirst();
                    
                    if (familyMember.isPresent()) {
                        sportReminder.setFamily(familyMember.get().getFamily());
                    } else {
                        sportReminder.setFamily(userFamilies.get(0).getFamily());
                    }
                }
            }
            sportReminder = reminderRepository.save(sportReminder);
            reminders.add(toResponse(sportReminder));
            log.info("创建了运动记录提醒: {}", sportReminder.getId());
        }
        
        // 检查情绪数据
        List<HealthLog> moodLogs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(
                userId, com.healthfamily.domain.constant.HealthLogType.MOOD);
        boolean hasRecentMoodLog = moodLogs.stream()
                .anyMatch(log -> !log.getLogDate().isBefore(today.minusDays(1)));
        
        if (!hasRecentMoodLog) {
            HealthReminder moodReminder = HealthReminder.builder()
                    .user(user)
                    .creator(admin)  // 设置创建者为操作员
                    .type(ReminderType.LIFESTYLE)
                    .title("情绪记录提醒")
                    .content("请记录今天的情绪状态，有助于心理健康管理。")
                    .scheduledTime(LocalDateTime.now().plusHours(1))
                    .status(ReminderStatus.PENDING)
                    .priority(ReminderPriority.MEDIUM)
                    .channel("APP")
                    .build();
            
            // 根据是否为其他用户生成提醒来决定关联的家庭
            if (isForOtherUser && admin != null) {
                // 为其他用户生成提醒，关联到管理员和目标用户共同的家庭
                List<com.healthfamily.domain.entity.FamilyMember> adminFamilies = familyMemberRepository.findByUser(admin);
                List<com.healthfamily.domain.entity.FamilyMember> targetUserFamilies = familyMemberRepository.findByUser(user);
                
                // 找到管理员和目标用户共同的家庭
                Optional<com.healthfamily.domain.entity.Family> commonFamily = adminFamilies.stream()
                        .map(fm -> fm.getFamily())
                        .filter(adminFamily -> targetUserFamilies.stream()
                                .anyMatch(targetFm -> targetFm.getFamily().getId().equals(adminFamily.getId())))
                        .findFirst();
                
                if (commonFamily.isPresent()) {
                    moodReminder.setFamily(commonFamily.get());
                } else {
                    // 如果没有找到共同家庭，使用目标用户所在的家庭
                    List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                    if (!userFamilies.isEmpty()) {
                        // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                        Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                                .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                                .findFirst();
                        
                        if (familyMember.isPresent()) {
                            moodReminder.setFamily(familyMember.get().getFamily());
                        } else {
                            moodReminder.setFamily(userFamilies.get(0).getFamily());
                        }
                    }
                }
            } else {
                // 为自己生成提醒，关联到用户所在的家庭
                List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                if (!userFamilies.isEmpty()) {
                    // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                    Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                            .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                            .findFirst();
                    
                    if (familyMember.isPresent()) {
                        moodReminder.setFamily(familyMember.get().getFamily());
                    } else {
                        moodReminder.setFamily(userFamilies.get(0).getFamily());
                    }
                }
            }
            moodReminder = reminderRepository.save(moodReminder);
            reminders.add(toResponse(moodReminder));
            log.info("创建了情绪记录提醒: {}", moodReminder.getId());
        }
        
        // 检查体征数据
        List<HealthLog> vitalsLogs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(
                userId, com.healthfamily.domain.constant.HealthLogType.VITALS);
        boolean hasRecentVitalsLog = vitalsLogs.stream()
                .anyMatch(log -> !log.getLogDate().isBefore(today.minusDays(3)));
        
        if (!hasRecentVitalsLog) {
            HealthReminder vitalsReminder = HealthReminder.builder()
                    .user(user)
                    .creator(admin)  // 设置创建者为操作员
                    .type(ReminderType.MEASUREMENT)
                    .title("体征测量提醒")
                    .content("请测量并记录血压、血糖等体征数据，有助于健康监测。")
                    .scheduledTime(LocalDateTime.now().plusHours(2))
                    .status(ReminderStatus.PENDING)
                    .priority(ReminderPriority.MEDIUM)
                    .channel("APP")
                    .build();
            
            // 根据是否为其他用户生成提醒来决定关联的家庭
            if (isForOtherUser && admin != null) {
                // 为其他用户生成提醒，关联到管理员和目标用户共同的家庭
                List<com.healthfamily.domain.entity.FamilyMember> adminFamilies = familyMemberRepository.findByUser(admin);
                List<com.healthfamily.domain.entity.FamilyMember> targetUserFamilies = familyMemberRepository.findByUser(user);
                
                // 找到管理员和目标用户共同的家庭
                Optional<com.healthfamily.domain.entity.Family> commonFamily = adminFamilies.stream()
                        .map(fm -> fm.getFamily())
                        .filter(adminFamily -> targetUserFamilies.stream()
                                .anyMatch(targetFm -> targetFm.getFamily().getId().equals(adminFamily.getId())))
                        .findFirst();
                
                if (commonFamily.isPresent()) {
                    vitalsReminder.setFamily(commonFamily.get());
                } else {
                    // 如果没有找到共同家庭，使用目标用户所在的家庭
                    List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                    if (!userFamilies.isEmpty()) {
                        // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                        Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                                .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                                .findFirst();
                        
                        if (familyMember.isPresent()) {
                            vitalsReminder.setFamily(familyMember.get().getFamily());
                        } else {
                            vitalsReminder.setFamily(userFamilies.get(0).getFamily());
                        }
                    }
                }
            } else {
                // 为自己生成提醒，关联到用户所在的家庭
                List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                if (!userFamilies.isEmpty()) {
                    // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                    Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                            .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                            .findFirst();
                    
                    if (familyMember.isPresent()) {
                        vitalsReminder.setFamily(familyMember.get().getFamily());
                    } else {
                        vitalsReminder.setFamily(userFamilies.get(0).getFamily());
                    }
                }
            }
            vitalsReminder = reminderRepository.save(vitalsReminder);
            reminders.add(toResponse(vitalsReminder));
            log.info("创建了体征测量提醒: {}", vitalsReminder.getId());
        }
        
        // 检查睡眠数据
        List<HealthLog> sleepLogs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(
                userId, com.healthfamily.domain.constant.HealthLogType.SLEEP);
        boolean hasRecentSleepLog = sleepLogs.stream()
                .anyMatch(log -> !log.getLogDate().isBefore(today.minusDays(1)));
        
        if (!hasRecentSleepLog) {
            HealthReminder sleepReminder = HealthReminder.builder()
                    .user(user)
                    .creator(admin)  // 设置创建者为操作员
                    .type(ReminderType.LIFESTYLE)
                    .title("睡眠记录提醒")
                    .content("请记录今天的睡眠情况，良好睡眠对健康至关重要。")
                    .scheduledTime(LocalDateTime.now().plusHours(1))
                    .status(ReminderStatus.PENDING)
                    .priority(ReminderPriority.MEDIUM)
                    .channel("APP")
                    .build();
            
            // 根据是否为其他用户生成提醒来决定关联的家庭
            if (isForOtherUser && admin != null) {
                // 为其他用户生成提醒，关联到管理员和目标用户共同的家庭
                List<com.healthfamily.domain.entity.FamilyMember> adminFamilies = familyMemberRepository.findByUser(admin);
                List<com.healthfamily.domain.entity.FamilyMember> targetUserFamilies = familyMemberRepository.findByUser(user);
                
                // 找到管理员和目标用户共同的家庭
                Optional<com.healthfamily.domain.entity.Family> commonFamily = adminFamilies.stream()
                        .map(fm -> fm.getFamily())
                        .filter(adminFamily -> targetUserFamilies.stream()
                                .anyMatch(targetFm -> targetFm.getFamily().getId().equals(adminFamily.getId())))
                        .findFirst();
                
                if (commonFamily.isPresent()) {
                    sleepReminder.setFamily(commonFamily.get());
                } else {
                    // 如果没有找到共同家庭，使用目标用户所在的家庭
                    List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                    if (!userFamilies.isEmpty()) {
                        // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                        Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                                .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                                .findFirst();
                        
                        if (familyMember.isPresent()) {
                            sleepReminder.setFamily(familyMember.get().getFamily());
                        } else {
                            sleepReminder.setFamily(userFamilies.get(0).getFamily());
                        }
                    }
                }
            } else {
                // 为自己生成提醒，关联到用户所在的家庭
                List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                if (!userFamilies.isEmpty()) {
                    // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                    Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                            .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                            .findFirst();
                    
                    if (familyMember.isPresent()) {
                        sleepReminder.setFamily(familyMember.get().getFamily());
                    } else {
                        sleepReminder.setFamily(userFamilies.get(0).getFamily());
                    }
                }
            }
            sleepReminder = reminderRepository.save(sleepReminder);
            reminders.add(toResponse(sleepReminder));
            log.info("创建了睡眠记录提醒: {}", sleepReminder.getId());
        }
        
        // 定期健康评估提醒（每30天）
        List<HealthReminder> assessmentReminders = reminderRepository
                .findByUserAndTypeOrderByScheduledTimeDesc(user, ReminderType.ROUTINE);
        boolean hasRecentAssessmentReminder = assessmentReminders.stream()
                .anyMatch(r -> r.getTitle().contains("健康评估") && 
                              r.getScheduledTime().isAfter(LocalDateTime.now().minusDays(30)));
        
        if (!hasRecentAssessmentReminder) {
            HealthReminder assessmentReminder = HealthReminder.builder()
                    .user(user)
                    .creator(admin)  // 设置创建者为操作员
                    .type(ReminderType.ROUTINE)
                    .title("定期健康评估提醒")
                    .content("建议进行体质测评或健康评估，了解当前健康状况。")
                    .scheduledTime(LocalDateTime.now().plusDays(7))
                    .status(ReminderStatus.PENDING)
                    .priority(ReminderPriority.LOW)
                    .channel("APP")
                    .build();
            
            // 根据是否为其他用户生成提醒来决定关联的家庭
            if (isForOtherUser && admin != null) {
                // 为其他用户生成提醒，关联到管理员和目标用户共同的家庭
                List<com.healthfamily.domain.entity.FamilyMember> adminFamilies = familyMemberRepository.findByUser(admin);
                List<com.healthfamily.domain.entity.FamilyMember> targetUserFamilies = familyMemberRepository.findByUser(user);
                
                // 找到管理员和目标用户共同的家庭
                Optional<com.healthfamily.domain.entity.Family> commonFamily = adminFamilies.stream()
                        .map(fm -> fm.getFamily())
                        .filter(adminFamily -> targetUserFamilies.stream()
                                .anyMatch(targetFm -> targetFm.getFamily().getId().equals(adminFamily.getId())))
                        .findFirst();
                
                if (commonFamily.isPresent()) {
                    assessmentReminder.setFamily(commonFamily.get());
                } else {
                    // 如果没有找到共同家庭，使用目标用户所在的家庭
                    List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                    if (!userFamilies.isEmpty()) {
                        // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                        Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                                .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                                .findFirst();
                        
                        if (familyMember.isPresent()) {
                            assessmentReminder.setFamily(familyMember.get().getFamily());
                        } else {
                            assessmentReminder.setFamily(userFamilies.get(0).getFamily());
                        }
                    }
                }
            } else {
                // 为自己生成提醒，关联到用户所在的家庭
                List<com.healthfamily.domain.entity.FamilyMember> userFamilies = familyMemberRepository.findByUser(user);
                if (!userFamilies.isEmpty()) {
                    // 对于家庭管理员，优先选择其作为所有者的家庭；否则选择第一个家庭
                    Optional<com.healthfamily.domain.entity.FamilyMember> familyMember = userFamilies.stream()
                            .filter(fm -> fm.getFamily().getOwner().getId().equals(user.getId()))
                            .findFirst();
                    
                    if (familyMember.isPresent()) {
                        assessmentReminder.setFamily(familyMember.get().getFamily());
                    } else {
                        assessmentReminder.setFamily(userFamilies.get(0).getFamily());
                    }
                }
            }
            assessmentReminder = reminderRepository.save(assessmentReminder);
            reminders.add(toResponse(assessmentReminder));
            log.info("创建了健康评估提醒: {}", assessmentReminder.getId());
        }
    }

    // 生成协作提醒：针对家庭协作任务（如记录饮食）未完成的情况
    @Override
    @Transactional
    public List<ReminderResponse> generateSmartReminders(Long userId, Long familyId) {
        // 验证用户是否是家庭成员
        com.healthfamily.domain.entity.Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("家庭不存在"));
        
        boolean isFamilyMember = familyMemberRepository.findByFamilyAndUser(family, loadUser(userId))
                .isPresent();
        
        if (!isFamilyMember) {
            throw new RuntimeException("无权为该家庭生成协作提醒");
        }
        
        List<HealthReminder> familyReminders = reminderRepository.findByFamily_IdOrderByScheduledTimeAsc(familyId);

        LocalDate today = LocalDate.now();
        List<ReminderResponse> created = new ArrayList<>();

        if (familyReminders.isEmpty()) {
            List<com.healthfamily.domain.entity.FamilyMember> members = familyMemberRepository.findByFamily(family);
            for (com.healthfamily.domain.entity.FamilyMember fm : members) {
                User assignee = fm.getUser();
                
                // 检查饮食记录
                boolean hasDietLogToday = healthLogRepository
                        .findByUser_IdAndTypeOrderByLogDateDesc(assignee.getId(), com.healthfamily.domain.constant.HealthLogType.DIET)
                        .stream().anyMatch(l -> today.equals(l.getLogDate()));
                
                if (!hasDietLogToday) {
                    List<HealthReminder> existing = reminderRepository
                            .findByAssignedTo_IdOrderByScheduledTimeAsc(assignee.getId())
                            .stream()
                            .filter(r -> r.getStatus() == ReminderStatus.PENDING
                                    && r.getFamily() != null && r.getFamily().getId().equals(familyId)
                                    && r.getType() == ReminderType.ROUTINE
                                    && today.equals(r.getScheduledTime().toLocalDate()))
                            .collect(Collectors.toList());
                    if (!existing.isEmpty()) continue;

                    String promptText = String.format("请以温和口吻提醒%s：记得记录今天老人的饮食，方便医生分析营养摄入。控制在60字内。", assignee.getNickname());
                    String content;
                    try {
                        Prompt prompt = new Prompt(new UserMessage(promptText));
                        content = chatModel.call(prompt).getResult().getOutput().getContent();
                    } catch (Exception e) {
                        content = "请记得记录今天的饮食，便于后续分析。";
                    }

                    HealthReminder reminder = HealthReminder.builder()
                            .user(family.getOwner())
                            .creator(family.getOwner())  // 设置创建者为家庭管理员
                            .assignedTo(assignee)
                            .family(family)
                            .type(ReminderType.ROUTINE)
                            .title("协作提醒：饮食记录")
                            .content(content)
                            .scheduledTime(LocalDateTime.now().plusMinutes(10))
                            .status(ReminderStatus.PENDING)
                            .priority(ReminderPriority.MEDIUM)
                            .channel("APP")
                            .build();
                    reminder = reminderRepository.save(reminder);
                    created.add(toResponse(reminder));
                }
                
                // 检查体征记录
                boolean hasVitalsLogToday = healthLogRepository
                        .findByUser_IdAndTypeOrderByLogDateDesc(assignee.getId(), com.healthfamily.domain.constant.HealthLogType.VITALS)
                        .stream().anyMatch(l -> today.equals(l.getLogDate()));
                
                if (!hasVitalsLogToday) {
                    List<HealthReminder> existing = reminderRepository
                            .findByAssignedTo_IdOrderByScheduledTimeAsc(assignee.getId())
                            .stream()
                            .filter(r -> r.getStatus() == ReminderStatus.PENDING
                                    && r.getFamily() != null && r.getFamily().getId().equals(familyId)
                                    && r.getTitle().contains("体征")
                                    && today.equals(r.getScheduledTime().toLocalDate()))
                            .collect(Collectors.toList());
                    if (!existing.isEmpty()) continue;

                    String promptText = String.format("请以温和口吻提醒%s：记得测量并记录老人的血压、血糖等体征数据，方便医生监测健康状况。控制在60字内。", assignee.getNickname());
                    String content;
                    try {
                        Prompt prompt = new Prompt(new UserMessage(promptText));
                        content = chatModel.call(prompt).getResult().getOutput().getContent();
                    } catch (Exception e) {
                        content = "请记得测量并记录体征数据，便于健康监测。";
                    }

                    HealthReminder reminder = HealthReminder.builder()
                            .user(family.getOwner())
                            .creator(family.getOwner())  // 设置创建者为家庭管理员
                            .assignedTo(assignee)
                            .family(family)
                            .type(ReminderType.MEASUREMENT)
                            .title("协作提醒：体征记录")
                            .content(content)
                            .scheduledTime(LocalDateTime.now().plusMinutes(10))
                            .status(ReminderStatus.PENDING)
                            .priority(ReminderPriority.MEDIUM)
                            .channel("APP")
                            .build();
                    reminder = reminderRepository.save(reminder);
                    created.add(toResponse(reminder));
                }
            }
        } else {
            for (HealthReminder r : familyReminders) {
                if (r.getType() != ReminderType.ROUTINE && r.getType() != ReminderType.MEASUREMENT) continue;
                User assignee = Optional.ofNullable(r.getAssignedTo()).orElse(r.getUser());
                
                // 检查饮食记录
                if (r.getType() == ReminderType.ROUTINE) {
                    boolean hasLogToday = healthLogRepository
                            .findByUser_IdAndTypeOrderByLogDateDesc(assignee.getId(), com.healthfamily.domain.constant.HealthLogType.DIET)
                            .stream().anyMatch(l -> today.equals(l.getLogDate()));
                    if (!hasLogToday) {
                        String promptText = String.format("请以温和口吻提醒%s：记得记录今天老人的饮食，方便医生分析营养摄入。控制在60字内。", assignee.getNickname());
                        String content;
                        try {
                            Prompt prompt = new Prompt(new UserMessage(promptText));
                            content = chatModel.call(prompt).getResult().getOutput().getContent();
                        } catch (Exception e) {
                            content = "请记得记录今天的饮食，便于后续分析。";
                        }
                        HealthReminder reminder = HealthReminder.builder()
                                .user(r.getUser())
                                .creator(r.getUser())  // 设置创建者为原始提醒的用户
                                .assignedTo(assignee)
                                .family(family)
                                .type(ReminderType.ROUTINE)
                                .title("协作提醒：饮食记录")
                                .content(content)
                                .scheduledTime(LocalDateTime.now().plusMinutes(10))
                                .status(ReminderStatus.PENDING)
                                .priority(ReminderPriority.MEDIUM)
                                .channel("APP")
                                .build();
                        reminder = reminderRepository.save(reminder);
                        created.add(toResponse(reminder));
                    }
                }
                
                // 检查体征记录
                if (r.getType() == ReminderType.MEASUREMENT) {
                    boolean hasLogToday = healthLogRepository
                            .findByUser_IdAndTypeOrderByLogDateDesc(assignee.getId(), com.healthfamily.domain.constant.HealthLogType.VITALS)
                            .stream().anyMatch(l -> today.equals(l.getLogDate()));
                    if (!hasLogToday) {
                        String promptText = String.format("请生成一条给%s的温馨提示：提醒他/她记得测量并记录血压、血糖等体征数据，方便医生监测健康状况。直接以对他说的话的口吻输出，控制在60字内。", assignee.getNickname());
                        String content;
                        try {
                            Prompt prompt = new Prompt(new UserMessage(promptText));
                            content = chatModel.call(prompt).getResult().getOutput().getContent();
                        } catch (Exception e) {
                            content = "记得测量并记录体征数据，便于健康监测。";
                        }
                        HealthReminder reminder = HealthReminder.builder()
                                .user(assignee) // 设置所属用户为被提醒者
                                .creator(r.getUser())  // 设置创建者为原始提醒的用户
                                .assignedTo(assignee)
                                .family(family)
                                .type(ReminderType.MEASUREMENT)
                                .title("协作提醒：体征记录")
                                .content(content)
                                .scheduledTime(LocalDateTime.now().plusMinutes(10))
                                .status(ReminderStatus.PENDING)
                                .priority(ReminderPriority.MEDIUM)
                                .channel("APP")
                                .build();
                        reminder = reminderRepository.save(reminder);
                        created.add(toResponse(reminder));
                    }
                }
            }
        }

        return created;
    }

    private String generateReminderContent(HealthLog healthLog) {
        try {
            // 使用AI生成提醒内容
            String promptText = String.format(
                "根据以下健康数据异常情况，生成一条友好的提醒内容：\n" +
                "日期：%s\n" +
                "类型：%s\n" +
                "数据：%s\n" +
                "\n" +
                "要求：\n" +
                "1. 直接对用户说话（使用第二人称'您'）\n" +
                "2. 提醒关注异常数据\n" +
                "3. 建议复测时间\n" +
                "4. 提供简单的健康建议\n" +
                "5. 语气温和、专业\n" +
                "6. 不超过100字\n",
                healthLog.getLogDate(),
                healthLog.getType(),
                healthLog.getContentJson());

            // 使用 Spring AI 1.0.0-M5 的 API
            Prompt prompt = new Prompt(new UserMessage(promptText));
            String response = chatModel.call(prompt).getResult().getOutput().getContent();
                
            if (response == null || response.trim().isEmpty()) {
                log.warn("AI返回的提醒内容为空，使用默认内容");
                return String.format("您的%s数据出现异常，建议2小时后复测，如有不适请及时就医。",
                        healthLog.getType().name());
            }
                
            return response;

        } catch (Exception e) {
            log.error("AI生成提醒内容失败: {}", e.getMessage(), e);
            return String.format("您的%s数据出现异常，建议2小时后复测，如有不适请及时就医。",
                    healthLog.getType().name());
        }
    }

    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void processPendingReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<HealthReminder> pendingReminders = reminderRepository
                .findByStatusAndScheduledTimeLessThanEqual(ReminderStatus.PENDING, now);

        for (HealthReminder reminder : pendingReminders) {
            try {
                // 发送提醒（这里可以集成推送服务）
                log.info("发送提醒：{} - {}", reminder.getTitle(), reminder.getContent());

                reminder.setStatus(ReminderStatus.SENT);
                reminder.setActualTime(now);
                reminderRepository.save(reminder);
            } catch (Exception e) {
                log.error("发送提醒失败: {}", e.getMessage());
            }
        }
    }

    private ReminderResponse toResponse(HealthReminder reminder) {
        Map<String, Object> metadata = null;
        try {
            if (reminder.getMetadataJson() != null) {
                metadata = objectMapper.readValue(
                        reminder.getMetadataJson(),
                        new TypeReference<Map<String, Object>>() {}
                );
            }
        } catch (Exception e) {
            log.warn("解析metadata失败: {}", e.getMessage());
        }

        return new ReminderResponse(
                reminder.getId(),
                reminder.getType().name(),
                reminder.getTitle(),
                reminder.getContent(),
                reminder.getScheduledTime(),
                reminder.getActualTime(),
                reminder.getStatus().name(),
                reminder.getPriority().name(),
                reminder.getChannel(),
                metadata,
                reminder.getCreatedAt(),
                reminder.getAssignedTo() != null ? reminder.getAssignedTo().getId() : (reminder.getUser() != null ? reminder.getUser().getId() : null),
                reminder.getAssignedTo() != null ? reminder.getAssignedTo().getNickname() : (reminder.getUser() != null ? reminder.getUser().getNickname() : null),
                reminder.getFamily() != null ? reminder.getFamily().getId() : null,
                reminder.getCreator() != null ? reminder.getCreator().getId() : null,
                reminder.getCreator() != null ? reminder.getCreator().getNickname() : null
        );
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    private boolean hasReminderPermission(com.healthfamily.domain.constant.UserRole userRole) {
        // 只有家庭管理员、医生和系统管理员角色可以访问健康提醒功能
        boolean hasPermission = userRole == com.healthfamily.domain.constant.UserRole.FAMILY_ADMIN || 
                               userRole == com.healthfamily.domain.constant.UserRole.DOCTOR ||
                               userRole == com.healthfamily.domain.constant.UserRole.ADMIN;
        log.debug("检查用户角色 {} 的权限，结果: {}", userRole, hasPermission);
        return hasPermission;
    }
    
    private void validateUserInFamily(Long adminId, Long targetUserId) {
        User admin = loadUser(adminId);
        User targetUser = loadUser(targetUserId);
        
        // 检查目标用户是否在管理员的家庭中
        boolean isAdminInFamily = familyMemberRepository.findByUser(admin).stream()
                .anyMatch(fm -> {
                    // 检查管理员是否是家庭管理员或具有FAMILY_ADMIN角色
                    boolean isFamilyAdmin = Boolean.TRUE.equals(fm.getAdmin()) || 
                                admin.getRole() == com.healthfamily.domain.constant.UserRole.FAMILY_ADMIN;
                    // 检查目标用户是否在同一家庭中
                    boolean isTargetInSameFamily = familyMemberRepository.findByUser(targetUser).stream()
                            .anyMatch(tfm -> tfm.getFamily().getId().equals(fm.getFamily().getId()));
                    return isFamilyAdmin && isTargetInSameFamily;
                });
        
        if (!isAdminInFamily) {
            throw new RuntimeException("无权为该用户生成健康提醒，用户不在您的家庭中");
        }
    }

    @Override
    public java.util.Map<String, Object> getAdminReminderList(Long userId, String reminderType, String status, String keyword, int page, int size, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        // 分页查询健康提醒列表
        // 这里需要根据实际的分页需求实现，这里简化为返回所有提醒
        List<HealthReminder> reminders = reminderRepository.findAll();
        
        // 应用筛选条件
        if (userId != null) {
            reminders = reminders.stream()
                .filter(reminder -> {
                    boolean isForUser = reminder.getUser() != null && reminder.getUser().getId().equals(userId);
                    boolean isAssignedToUser = reminder.getAssignedTo() != null && reminder.getAssignedTo().getId().equals(userId);
                    return isForUser || isAssignedToUser;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (reminderType != null && !reminderType.isEmpty()) {
            try {
                ReminderType type = ReminderType.valueOf(reminderType.toUpperCase());
                reminders = reminders.stream()
                    .filter(reminder -> reminder.getType() == type)
                    .collect(java.util.stream.Collectors.toList());
            } catch (IllegalArgumentException e) {
                // 忽略无效的提醒类型
            }
        }
        
        if (status != null && !status.isEmpty()) {
            try {
                ReminderStatus reminderStatus = ReminderStatus.valueOf(status.toUpperCase());
                reminders = reminders.stream()
                    .filter(reminder -> reminder.getStatus() == reminderStatus)
                    .collect(java.util.stream.Collectors.toList());
            } catch (IllegalArgumentException e) {
                // 忽略无效的提醒状态
            }
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            reminders = reminders.stream()
                .filter(reminder -> {
                    boolean titleContains = reminder.getTitle() != null && reminder.getTitle().contains(keyword);
                    boolean contentContains = reminder.getContent() != null && reminder.getContent().contains(keyword);
                    return titleContains || contentContains;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (startTime != null || endTime != null) {
            reminders = reminders.stream()
                .filter(reminder -> {
                    java.time.LocalDateTime reminderCreatedAt = reminder.getCreatedAt();
                    if (reminderCreatedAt == null) {
                        reminderCreatedAt = reminder.getScheduledTime();
                    }
                    
                    boolean afterStart = startTime == null || !reminderCreatedAt.isBefore(startTime);
                    boolean beforeEnd = endTime == null || !reminderCreatedAt.isAfter(endTime);
                    
                    return afterStart && beforeEnd;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 计算分页数据
        int total = reminders.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        if (start >= total) {
            reminders = java.util.Collections.emptyList();
        } else {
            reminders = reminders.subList(start, end);
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", reminders);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));
        
        return result;
    }

    @Override
    public HealthReminder findById(Long id) {
        return reminderRepository.findById(id).orElse(null);
    }

    @Override
    public HealthReminder create(HealthReminder reminder) {
        return reminderRepository.save(reminder);
    }

    @Override
    public HealthReminder update(Long id, HealthReminder reminder) {
        HealthReminder existingReminder = reminderRepository.findById(id).orElse(null);
        if (existingReminder == null) {
            return null;
        }
        
        // 更新提醒信息
        existingReminder.setTitle(reminder.getTitle());
        existingReminder.setContent(reminder.getContent());
        existingReminder.setScheduledTime(reminder.getScheduledTime());
        existingReminder.setType(reminder.getType());
        existingReminder.setStatus(reminder.getStatus());
        existingReminder.setPriority(reminder.getPriority());
        existingReminder.setChannel(reminder.getChannel());
        existingReminder.setMetadataJson(reminder.getMetadataJson());
        existingReminder.setUpdatedAt(java.time.LocalDateTime.now());
        
        return reminderRepository.save(existingReminder);
    }

    @Override
    public boolean deleteById(Long id) {
        if (reminderRepository.existsById(id)) {
            reminderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        HealthReminder reminder = reminderRepository.findById(id).orElse(null);
        if (reminder == null) {
            return false;
        }
        
        try {
            ReminderStatus reminderStatus = ReminderStatus.valueOf(status.toUpperCase());
            reminder.setStatus(reminderStatus);
            reminderRepository.save(reminder);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String writeJsonSafely(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("序列化JSON失败: {}", e.getMessage());
            return "{}";
        }
    }
}
