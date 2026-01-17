package com.healthfamily.service.impl;

import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.constant.AlertStatus;
import com.healthfamily.domain.entity.*;
import com.healthfamily.domain.repository.*;
import com.healthfamily.service.DoctorMonitoringService;
import com.healthfamily.web.dto.*;
import com.healthfamily.domain.constant.AlertSeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorMonitoringServiceImpl implements DoctorMonitoringService {

    private final HealthAlertRepository healthAlertRepository;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ProfileRepository profileRepository;
    private final AbnormalHandlingRecordRepository abnormalHandlingRecordRepository;
    private final HealthReminderRepository healthReminderRepository;
    private final HealthLogRepository healthLogRepository;
    private final FamilyDoctorRepository familyDoctorRepository;
    private final com.healthfamily.service.HealthDataAiService healthDataAiService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private static final com.fasterxml.jackson.core.type.TypeReference<List<String>> STRING_LIST_TYPE = new com.fasterxml.jackson.core.type.TypeReference<>() {};
    private static final com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>> MAP_TYPE_REF = new com.fasterxml.jackson.core.type.TypeReference<>() {};

    @Override
    @Transactional
    public EnhancedMonitoringDataResponse getEnhancedMonitoringData(Long doctorId, Long familyId) {
        // 验证医生权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(30);  // 近30天

        // 获取家庭成员列表
        List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        List<User> viewableUsers = new ArrayList<>();
        for (FamilyMember m : members) {
            User u = m.getUser();
            if (u == null || !canViewMemberData(u.getId())) continue;
            viewableUsers.add(u);
        }

        // 1. 构建趋势数据（按日期统计异常事件数量，按严重程度分组）
        Map<String, Integer> dateCountMap = new HashMap<>();
        Map<String, Integer> dateCriticalCountMap = new HashMap<>();
        Map<String, Integer> dateHighCountMap = new HashMap<>();
        Map<String, Integer> dateMediumCountMap = new HashMap<>();
        Map<String, Integer> dateLowCountMap = new HashMap<>();

        // 收集所有异常（包括Alerts和实时检测的Logs）
        List<HealthAlert> alerts = healthAlertRepository.findByFamily_IdOrderByCreatedAtDesc(familyId)
                .stream()
                // 不过滤状态，以便包含已处理的记录，并用于去重
                .filter(a -> !a.getCreatedAt().toLocalDate().isBefore(start))
                .collect(Collectors.toList());
        
        // 实时检测最近30天的日志
        for (User user : viewableUsers) {
            List<HealthLog> recentLogs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(user.getId(), com.healthfamily.domain.constant.HealthLogType.VITALS)
                    .stream()
                    .filter(l -> !l.getLogDate().isBefore(start))
                    .collect(Collectors.toList());
            
            for (HealthLog log : recentLogs) {
                Map<String, Object> content = safeParse(log.getContentJson());
                if (checkAnomaly(user.getId(), content)) {
                    // 创建虚拟Alert用于统计
                    HealthAlert virtualAlert = new HealthAlert();
                    virtualAlert.setId(-log.getId()); // 使用负ID区分
                    virtualAlert.setUser(user);
                    virtualAlert.setMetric(buildLogSummary(content));
                    virtualAlert.setValue(extractValue(content));
                    virtualAlert.setThreshold(0.0); // 暂无阈值信息
                    virtualAlert.setSeverity(AlertSeverity.HIGH); // 默认为HIGH
                    virtualAlert.setStatus(AlertStatus.PENDING);
                    virtualAlert.setCreatedAt(log.getLogDate().atStartOfDay());
                    
                    // 检查是否已存在对应的Alert，避免重复统计
                    boolean exists = alerts.stream().anyMatch(a -> 
                        a.getUser().getId().equals(user.getId()) && 
                        a.getMetric().equals(virtualAlert.getMetric()) &&
                        a.getCreatedAt().toLocalDate().equals(virtualAlert.getCreatedAt().toLocalDate())
                    );
                    
                    if (!exists) {
                        alerts.add(virtualAlert);
                    }
                }
            }
        }

        for (HealthAlert alert : alerts) {
            String dateStr = alert.getCreatedAt().toLocalDate().toString();
            dateCountMap.put(dateStr, dateCountMap.getOrDefault(dateStr, 0) + 1);

            // 按严重程度统计
            switch (alert.getSeverity()) {
                case CRITICAL -> dateCriticalCountMap.put(dateStr, dateCriticalCountMap.getOrDefault(dateStr, 0) + 1);
                case HIGH -> dateHighCountMap.put(dateStr, dateHighCountMap.getOrDefault(dateStr, 0) + 1);
                case MEDIUM -> dateMediumCountMap.put(dateStr, dateMediumCountMap.getOrDefault(dateStr, 0) + 1);
                case LOW -> dateLowCountMap.put(dateStr, dateLowCountMap.getOrDefault(dateStr, 0) + 1);
            }
        }

        // 构建趋势点列表（填充缺失的日期）
        List<EnhancedMonitoringDataResponse.AbnormalEventTrendPoint> trendData = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            trendData.add(new EnhancedMonitoringDataResponse.AbnormalEventTrendPoint(
                    dateStr,
                    dateCountMap.getOrDefault(dateStr, 0),
                    dateCriticalCountMap.getOrDefault(dateStr, 0),
                    dateHighCountMap.getOrDefault(dateStr, 0),
                    dateMediumCountMap.getOrDefault(dateStr, 0),
                    dateLowCountMap.getOrDefault(dateStr, 0)
            ));
        }

        // 2. 构建事件类型统计（饼图数据）
        Map<String, Integer> eventTypeStats = new HashMap<>();
        for (HealthAlert alert : alerts) {
            String key = alert.getMetric() + "_" + alert.getSeverity();
            eventTypeStats.put(key, eventTypeStats.getOrDefault(key, 0) + 1);
        }

        // 3. 构建预警列表（增强版）
        List<EnhancedMonitoringAlertDto> enhancedAlerts = new ArrayList<>();

        // 添加Alert类型的预警
        for (HealthAlert alert : alerts) {
            User user = alert.getUser();
            enhancedAlerts.add(new EnhancedMonitoringAlertDto(
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
                    getHandlingStatus(alert),
                    alert.getMetric(),
                    alert.getValue(),
                    alert.getThreshold(),
                    alert.getCreatedAt(),
                    alert.getHandledAt(),
                    alert.getNotificationTime(),
                    alert.getHandlingNote(),
                    alert.getId(),
                    null
            ));
        }

        // 按严重程度和创建时间排序（严重程度：CRITICAL > HIGH > MEDIUM > LOW）
        enhancedAlerts.sort((a, b) -> {
            int severityOrderA = getSeverityOrder(a.severity());
            int severityOrderB = getSeverityOrder(b.severity());
            if (severityOrderA != severityOrderB) {
                return Integer.compare(severityOrderB, severityOrderA); // 降序
            }
            return b.createdAt().compareTo(a.createdAt()); // 时间降序
        });

        // 4. 获取高风险患者列表
        List<HighRiskMemberDto> highRiskMembers = new ArrayList<>();
        
        // 基于已收集的alerts（包含数据库中的HealthAlert和实时检测的虚拟Alert）来识别高风险用户
        // 过滤条件：最近7天有异常
        LocalDate sevenDaysAgo = today.minusDays(7);
        
        // 按用户分组最近7天的异常
        Map<Long, List<HealthAlert>> userAlertsMap = alerts.stream()
                .filter(a -> !a.getCreatedAt().toLocalDate().isBefore(sevenDaysAgo))
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        for (User user : viewableUsers) {
            List<HealthAlert> userRecentAlerts = userAlertsMap.get(user.getId());

            if (userRecentAlerts == null || userRecentAlerts.isEmpty()) continue;

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

            // 获取最后异常时间
            LocalDateTime lastAbnormalTime = userRecentAlerts.stream()
                    .map(HealthAlert::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            highRiskMembers.add(new HighRiskMemberDto(
                    user.getId(),
                    user.getNickname(),
                    family.getName(),
                    tags,
                    lastAbnormalTime,
                    readAvatar(user.getId())
            ));
        }

        highRiskMembers.sort((a, b) -> {
            if (a.lastAbnormalTime() == null && b.lastAbnormalTime() == null) return 0;
            if (a.lastAbnormalTime() == null) return 1;
            if (b.lastAbnormalTime() == null) return -1;
            return b.lastAbnormalTime().compareTo(a.lastAbnormalTime());
        });
        highRiskMembers = highRiskMembers.stream().limit(10).collect(Collectors.toList());

        // 5. 统计摘要
        Map<AlertSeverity, Long> severityCount = alerts.stream()
                .collect(Collectors.groupingBy(HealthAlert::getSeverity, Collectors.counting()));

        EnhancedMonitoringDataResponse.MonitoringStatsSummary statsSummary = new EnhancedMonitoringDataResponse.MonitoringStatsSummary(
                alerts.size(),
                (int) alerts.stream().filter(a -> a.getStatus() == AlertStatus.PENDING).count(),
                (int) alerts.stream().filter(a -> a.getStatus() != AlertStatus.PENDING).count(),
                Math.toIntExact(severityCount.getOrDefault(AlertSeverity.CRITICAL, 0L)),
                Math.toIntExact(severityCount.getOrDefault(AlertSeverity.HIGH, 0L)),
                Math.toIntExact(severityCount.getOrDefault(AlertSeverity.MEDIUM, 0L)),
                Math.toIntExact(severityCount.getOrDefault(AlertSeverity.LOW, 0L))
        );

        return new EnhancedMonitoringDataResponse(
                trendData,
                eventTypeStats,
                enhancedAlerts,
                highRiskMembers,
                statsSummary
        );
    }

    @Override
    @Transactional
    public void handleAlert(Long doctorId, Long alertId, HandleAlertRequest request) {
        // 如果是虚拟Alert（ID < 0），需要先创建真实的HealthAlert
        HealthAlert alert;
        if (alertId < 0) {
            // 从HealthLog创建Alert
            long logId = -alertId;
            HealthLog log = healthLogRepository.findById(logId)
                    .orElseThrow(() -> new BusinessException(40408, "健康日志不存在"));
            
            // 验证医生权限
            FamilyMember member = familyMemberRepository.findByUser(log.getUser())
                    .stream().findFirst().orElseThrow(() -> new BusinessException(40409, "用户未绑定家庭"));
            ensureDoctorAccess(doctorId, member.getFamily());
            
            // 构建新的Alert
            Map<String, Object> content = safeParse(log.getContentJson());
            alert = new HealthAlert();
            alert.setUser(log.getUser());
            alert.setFamily(member.getFamily());
            alert.setMetric(buildLogSummary(content));
            alert.setValue(extractValue(content));
            alert.setThreshold(0.0); // 暂无阈值
            alert.setSeverity(AlertSeverity.HIGH); // 默认为HIGH
            alert.setStatus(AlertStatus.PENDING);
            alert.setCreatedAt(log.getLogDate().atStartOfDay());
            alert.setMessage("系统自动检测异常");
            alert.setChannel("SYSTEM");
            
            alert = healthAlertRepository.save(alert);
        } else {
            alert = healthAlertRepository.findById(alertId)
                    .orElseThrow(() -> new BusinessException(40407, "预警不存在"));
            
            // 验证医生权限
            if (alert.getFamily() != null) {
                ensureDoctorAccess(doctorId, alert.getFamily());
            }
        }

        // 更新alert状态和处理信息
        alert.setStatus(AlertStatus.ACKED);
        alert.setHandledAt(LocalDateTime.now());
        alert.setHandledBy(doctorId);
        alert.setHandlingNote(request.handlingNote());

        // 如果需要发送通知
        if ("notify".equals(request.action())) {
            alert.setNotificationSent("SENT");
            alert.setNotificationTime(LocalDateTime.now());
            alert.setNotificationContent(request.content());
            
            // 创建提醒记录
            HealthReminder reminder = HealthReminder.builder()
                    .user(alert.getUser())
                    .creator(userRepository.findById(doctorId).orElse(null))
                    .type(com.healthfamily.domain.constant.ReminderType.ABNORMAL)
                    .title("健康异常提醒")
                    .content(request.content())
                    .scheduledTime(LocalDateTime.now())
                    .status(com.healthfamily.domain.constant.ReminderStatus.PENDING)
                    .priority(getPriorityFromSeverity(alert.getSeverity()))
                    .channel("APP")
                    .family(alert.getFamily())
                    .build();
            healthReminderRepository.save(reminder);
        }

        // 如果需要后续跟踪，创建随访提醒
        if (Boolean.TRUE.equals(request.followUpRequired()) && request.followUpTime() != null) {
            HealthReminder followUp = HealthReminder.builder()
                    .user(alert.getUser())
                    .creator(userRepository.findById(doctorId).orElse(null))
                    .type(com.healthfamily.domain.constant.ReminderType.FOLLOW_UP)
                    .title("异常指标随访跟踪")
                    .content("针对异常指标(" + alert.getMetric() + ")的后续跟踪：" + request.content())
                    .scheduledTime(request.followUpTime())
                    .status(com.healthfamily.domain.constant.ReminderStatus.PENDING)
                    .priority(com.healthfamily.domain.constant.ReminderPriority.HIGH)
                    .channel("APP")
                    .family(alert.getFamily())
                    .build();
            healthReminderRepository.save(followUp);
        }

        healthAlertRepository.save(alert);

        // 创建处理记录
        AbnormalHandlingRecord record = AbnormalHandlingRecord.builder()
                .alert(alert)
                .doctor(userRepository.findById(doctorId).orElse(null))
                .patient(alert.getUser())
                .handlingAction(request.action())
                .handlingContent(request.content())
                .handlingNote(request.handlingNote())
                .handledAt(LocalDateTime.now())
                .followUpRequired(request.followUpRequired())
                .followUpTime(request.followUpTime())
                .build();
        abnormalHandlingRecordRepository.save(record);
    }

    @Override
    @Transactional
    public void sendPatientNotification(Long doctorId, SendNotificationRequest request) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        User patient = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));

        // 创建提醒记录
        HealthReminder reminder = HealthReminder.builder()
                .user(patient)
                .creator(doctor)
                .type(com.healthfamily.domain.constant.ReminderType.valueOf(request.type()))
                .title(request.title())
                .content(request.content())
                .scheduledTime(LocalDateTime.now())
                .status(com.healthfamily.domain.constant.ReminderStatus.PENDING)
                .priority(com.healthfamily.domain.constant.ReminderPriority.MEDIUM)
                .channel(request.channel())
                .build();
        healthReminderRepository.save(reminder);
    }

    @Override
    public List<HandlingRecordResponse> getHandlingHistory(Long doctorId, Long familyId, Long userId) {
        // 验证医生权限
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        ensureDoctorAccess(doctorId, family);

        List<AbnormalHandlingRecord> records;
        if (userId != null) {
            // 获取特定患者的处理历史
            records = abnormalHandlingRecordRepository.findByAlertFamilyIdAndPatient(familyId, 
                userRepository.findById(userId).orElse(null));
        } else {
            // 获取整个家庭的处理历史
            records = abnormalHandlingRecordRepository.findByAlertFamilyId(familyId);
        }

        return records.stream()
                .map(this::toHandlingRecordResponse)
                .sorted((a, b) -> b.handledAt().compareTo(a.handledAt())) // 按处理时间倒序
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void batchHandleAlerts(Long doctorId, BatchHandleRequest request) {
        for (Long alertId : request.alertIds()) {
            HandleAlertRequest singleRequest = new HandleAlertRequest(
                    request.action(),
                    request.content(),
                    request.followUpRequired(),
                    request.followUpTime(),
                    request.handlingNote()
            );
            handleAlert(doctorId, alertId, singleRequest);
        }
    }

    private HandlingRecordResponse toHandlingRecordResponse(AbnormalHandlingRecord record) {
        return new HandlingRecordResponse(
                record.getId(),
                record.getAlert() != null ? record.getAlert().getId() : null,
                record.getAlert() != null ? record.getAlert().getMetric() + "异常" : "未知异常",
                record.getAlert() != null ? record.getAlert().getMetric() : null,
                record.getAlert() != null ? record.getAlert().getValue() : null,
                record.getDoctor() != null ? record.getDoctor().getId() : null,
                record.getDoctor() != null ? record.getDoctor().getNickname() : "未知医生",
                record.getPatient() != null ? record.getPatient().getId() : null,
                record.getPatient() != null ? record.getPatient().getNickname() : "未知患者",
                record.getHandlingAction(),
                record.getHandlingContent(),
                record.getHandlingNote(),
                record.getHandledAt(),
                record.getFollowUpRequired(),
                record.getFollowUpTime(),
                record.getFollowUpResult()
        );
    }

    private String getHandlingStatus(HealthAlert alert) {
        if (alert.getHandledAt() == null) {
            return "UNHANDLED";
        } else if (alert.getNotificationTime() == null) {
            return "IN_PROGRESS";
        } else {
            return "COMPLETED";
        }
    }

    private int getSeverityOrder(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    private com.healthfamily.domain.constant.ReminderPriority getPriorityFromSeverity(AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> com.healthfamily.domain.constant.ReminderPriority.URGENT;
            case HIGH -> com.healthfamily.domain.constant.ReminderPriority.HIGH;
            case MEDIUM -> com.healthfamily.domain.constant.ReminderPriority.MEDIUM;
            case LOW -> com.healthfamily.domain.constant.ReminderPriority.LOW;
        };
    }

    // 以下方法是从DoctorServiceImpl复制的辅助方法
    private void ensureDoctorAccess(Long doctorId, Family family) {
        boolean isDoctor = familyDoctorRepository.findByFamily(family).stream()
                .anyMatch(fd -> java.util.Objects.equals(fd.getDoctor().getId(), doctorId));
        if (!isDoctor) {
            throw new BusinessException(40301, "无权访问该家庭");
        }
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
    
    private Double extractValue(Map<String, Object> content) {
        if (content.containsKey("systolic")) return extractDouble(content.get("systolic"));
        if (content.containsKey("glucose") || content.containsKey("bloodSugar")) return extractDouble(content.getOrDefault("glucose", content.get("bloodSugar")));
        if (content.containsKey("heartRate") || content.containsKey("hr")) return extractDouble(content.getOrDefault("heartRate", content.get("hr")));
        if (content.containsKey("temperature") || content.containsKey("temp")) return extractDouble(content.getOrDefault("temperature", content.get("temp")));
        if (content.containsKey("weight") || content.containsKey("val")) return extractDouble(content.getOrDefault("weight", content.get("val")));
        return 0.0;
    }

    private String buildLogSummary(Map<String, Object> content) {
        if (content.containsKey("systolic")) return "血压";
        if (content.containsKey("glucose") || content.containsKey("bloodSugar")) return "血糖";
        if (content.containsKey("heartRate") || content.containsKey("hr")) return "心率";
        if (content.containsKey("temperature") || content.containsKey("temp")) return "体温";
        if (content.containsKey("weight") || content.containsKey("val")) return "体重";
        return "体征";
    }
    
    private Map<String, Object> safeParse(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, MAP_TYPE_REF);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String readAvatar(Long userId) {
        return profileRepository.findById(userId).map(profile -> {
            String prefs = profile.getPreferences();
            if (prefs == null || prefs.isBlank()) return null;
            try {
                Map<?, ?> map = objectMapper.readValue(prefs, Map.class);
                Object avatar = map.get("avatar");
                return avatar != null ? avatar.toString() : null;
            } catch (Exception e) {
                return null;
            }
        }).orElse(null);
    }
}