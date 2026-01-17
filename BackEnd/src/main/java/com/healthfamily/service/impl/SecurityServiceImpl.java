package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.*;
import com.healthfamily.domain.repository.*;
import com.healthfamily.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository; // Added
    private final HealthLogRepository healthLogRepository;
    private final HealthReminderRepository healthReminderRepository;
    private final HealthAlertRepository healthAlertRepository;
    
    // 新增注入的Repository
    private final ConstitutionAssessmentRepository assessmentRepository;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final ConsultationSessionRepository consultationSessionRepository;
    private final ConsultationMessageRepository consultationMessageRepository;
    private final HealthPlanRepository healthPlanRepository;
    private final DoctorNoteRepository doctorNoteRepository;
    private final HealthConsultationRepository healthConsultationRepository;
    private final TcmPersonalizedPlanRepository tcmPersonalizedPlanRepository;

    @Override
    public String exportUserDataBase64(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 1. 获取各类数据
        List<HealthLog> logs = healthLogRepository.findByUserOrderByLogDateDesc(user);
        List<HealthReminder> reminders = healthReminderRepository.findByUserOrderByScheduledTimeDesc(user);
        List<HealthAlert> alerts = healthAlertRepository.findByUserOrderByCreatedAtDesc(user);
        List<ConstitutionAssessment> assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(user);
        List<AiRecommendation> recommendations = aiRecommendationRepository.findByUserOrderByForDateDesc(user);
        List<HealthPlan> healthPlans = healthPlanRepository.findByPatientOrderByCreatedAtDesc(user);
        List<DoctorNote> doctorNotes = doctorNoteRepository.findByPatientOrderByCreatedAtDesc(user);
        List<HealthConsultation> aiConsultations = healthConsultationRepository.findByUserOrderByCreatedAtDesc(user);
        List<TcmPersonalizedPlan> tcmPlans = tcmPersonalizedPlanRepository.findByUserIdOrderByGeneratedAtDesc(userId);
        
        // 在线咨询会话（作为患者）
        List<ConsultationSession> sessions = consultationSessionRepository.findByPatientOrderByLastMessageAtDesc(user);
        // 为了避免N+1，这里简单处理，实际应优化。导出所有相关会话的消息
        List<Map<String, Object>> sessionExports = new ArrayList<>();
        for (ConsultationSession session : sessions) {
            List<ConsultationMessage> messages = consultationMessageRepository.findBySessionOrderByCreatedAtAsc(session);
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("session", session);
            sessionData.put("messages", messages);
            sessionExports.add(sessionData);
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            
            // 2. 生成 Manifest
            Map<String, Integer> summary = new HashMap<>();
            summary.put("health_logs", logs.size());
            summary.put("assessments", assessments.size());
            summary.put("consultations", sessions.size());
            summary.put("ai_recommendations", recommendations.size());
            
            Map<String, Object> manifest = new HashMap<>();
            manifest.put("version", "1.0");
            manifest.put("export_time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            manifest.put("user_id", userId);
            manifest.put("summary", summary);
            
            addToZip(zos, "manifest.json", manifest);
            
            // 3. 写入各类数据
            addToZip(zos, "health_logs.json", logs);
            addToZip(zos, "reminders.json", reminders);
            addToZip(zos, "alerts.json", alerts);
            addToZip(zos, "constitution_assessments.json", assessments);
            addToZip(zos, "ai_recommendations.json", recommendations);
            addToZip(zos, "health_plans.json", healthPlans);
            addToZip(zos, "doctor_notes.json", doctorNotes);
            addToZip(zos, "ai_consultations.json", aiConsultations);
            addToZip(zos, "tcm_plans.json", tcmPlans);
            addToZip(zos, "online_consultations.json", sessionExports);
            
            zos.finish();
            zos.close();
            
            byte[] zip = baos.toByteArray();
            // 移除强制加密，提升用户体验（因为前端无解密逻辑）
            
            return Base64.getEncoder().encodeToString(zip);
        } catch (Exception e) {
            log.error("导出数据失败", e);
            return Base64.getEncoder().encodeToString("{}".getBytes(StandardCharsets.UTF_8));
        }
    }

    private void addToZip(ZipOutputStream zos, String filename, Object data) throws Exception {
        zos.putNextEntry(new ZipEntry(filename));
        zos.write(objectMapper.writeValueAsBytes(data));
        zos.closeEntry();
    }

    @Override
    @Transactional
    public void deleteUserData(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        log.info("开始删除用户数据: {}", userId);

        // 1. 删除 AI 相关
        aiRecommendationRepository.deleteAll(aiRecommendationRepository.findByUserOrderByForDateDesc(user));
        healthConsultationRepository.deleteAll(healthConsultationRepository.findByUserOrderByCreatedAtDesc(user));
        tcmPersonalizedPlanRepository.deleteAll(tcmPersonalizedPlanRepository.findByUserIdOrderByGeneratedAtDesc(userId));

        // 2. 删除体质测评
        assessmentRepository.deleteAll(assessmentRepository.findByUserOrderByCreatedAtDesc(user));

        // 3. 删除健康管理数据
        healthLogRepository.deleteAll(healthLogRepository.findByUserOrderByLogDateDesc(user));
        healthReminderRepository.deleteAll(healthReminderRepository.findByUserOrderByScheduledTimeDesc(user));
        healthAlertRepository.deleteAll(healthAlertRepository.findByUserOrderByCreatedAtDesc(user));

        // 4. 删除医生协作数据（作为患者）
        healthPlanRepository.deleteAll(healthPlanRepository.findByPatientOrderByCreatedAtDesc(user));
        doctorNoteRepository.deleteAll(doctorNoteRepository.findByPatientOrderByCreatedAtDesc(user));

        // 5. 删除在线咨询
        // 先删消息，再删会话
        List<ConsultationSession> sessions = consultationSessionRepository.findByPatientOrderByLastMessageAtDesc(user);
        for (ConsultationSession session : sessions) {
            // 删除该会话下的所有消息（包括医生发的）
            List<ConsultationMessage> messages = consultationMessageRepository.findBySessionOrderByCreatedAtAsc(session);
            consultationMessageRepository.deleteAll(messages);
        }
        consultationSessionRepository.deleteAll(sessions);
        
        log.info("用户数据删除完成: {}", userId);
    }
    
    @Override
    public Map<String, Object> getPrivacySettings(Long userId) {
        return profileRepository.findById(userId)
                .map(profile -> {
                    try {
                        if (profile.getPreferences() == null || profile.getPreferences().isBlank()) {
                            return new HashMap<String, Object>();
                        }
                        return objectMapper.readValue(profile.getPreferences(), new TypeReference<Map<String, Object>>() {});
                    } catch (Exception e) {
                        log.error("解析隐私设置失败", e);
                        return new HashMap<String, Object>();
                    }
                })
                .orElse(new HashMap<>());
    }

    @Override
    @Transactional
    public void updatePrivacySettings(Long userId, Map<String, Object> settings) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户档案不存在"));
        
        try {
            // 读取现有设置，合并更新
            Map<String, Object> current = new HashMap<>();
            if (profile.getPreferences() != null && !profile.getPreferences().isBlank()) {
                current = objectMapper.readValue(profile.getPreferences(), new TypeReference<Map<String, Object>>() {});
            }
            
            current.putAll(settings);
            
            profile.setPreferences(objectMapper.writeValueAsString(current));
            profileRepository.save(profile);
        } catch (Exception e) {
            log.error("更新隐私设置失败", e);
            throw new RuntimeException("更新隐私设置失败");
        }
    }

    @Override
    public boolean isAiAnalysisAllowed(Long userId, String scope) {
        Map<String, Object> settings = getPrivacySettings(userId);
        
        // 1. Check global AI switch
        Object shareToAiObj = settings.get("shareToAi");
        boolean shareToAi = shareToAiObj instanceof Boolean ? (Boolean) shareToAiObj : false;
        
        if (!shareToAi) {
            return false;
        }
        
        // 2. Check specific scope if provided
        if (scope != null) {
            Object aiScopesObj = settings.get("aiScopes");
            if (aiScopesObj instanceof Map) {
                Map<String, Object> aiScopes = (Map<String, Object>) aiScopesObj;
                Object scopeAllowedObj = aiScopes.get(scope);
                return scopeAllowedObj instanceof Boolean ? (Boolean) scopeAllowedObj : false;
            }
            // If scopes defined but malformed, assume denied for safety
            return false;
        }
        
        return true;
    }
}
