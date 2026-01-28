package com.healthfamily.service.impl;

import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.entity.*;
import com.healthfamily.domain.repository.*;
import com.healthfamily.service.OnlineConsultationService;
import com.healthfamily.web.dto.ConsultationMessageRequest;
import com.healthfamily.web.dto.ConsultationMessageResponse;
import com.healthfamily.web.dto.ConsultationSessionResponse;
import com.healthfamily.web.dto.CreateSessionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineConsultationServiceImpl implements OnlineConsultationService {

    private final ConsultationSessionRepository sessionRepository;
    private final ConsultationMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyDoctorRepository familyDoctorRepository;
    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ConsultationSessionResponse getOrCreateSession(Long requesterId, Long patientUserId, Long familyId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));

        // 判断请求者角色
        boolean isDoctor = requester.getRole() != null && 
                requester.getRole().name().equals("DOCTOR");
        
        ConsultationSession session;
        if (isDoctor) {
            // 医生请求：查找或创建与患者的会话
            Optional<ConsultationSession> existing = sessionRepository.findByPatientAndDoctor(patient, requester);
            if (existing.isPresent()) {
                session = existing.get();
                // 如果会话已关闭，重新激活
                if ("CLOSED".equals(session.getStatus())) {
                    session.setStatus("ACTIVE");
                    session = sessionRepository.save(session);
                }
            } else {
                // 验证医生权限
                ensureDoctorAccess(requesterId, family);
                session = createNewSession(requester, patient, family, null);
            }
        } else {
            // 患者或家庭成员请求：查找或创建与医生的会话
            Optional<FamilyDoctor> familyDoctor = familyDoctorRepository.findByFamily(family).stream().findFirst();
            if (familyDoctor.isPresent()) {
                User doctor = familyDoctor.get().getDoctor();
                Optional<ConsultationSession> existing = sessionRepository.findByPatientAndDoctor(patient, doctor);
                if (existing.isPresent()) {
                    session = existing.get();
                    if ("CLOSED".equals(session.getStatus())) {
                        session.setStatus("ACTIVE");
                        session = sessionRepository.save(session);
                    }
                } else {
                    session = createNewSession(doctor, patient, family, null);
                }
            } else {
                throw new BusinessException(40403, "该家庭未绑定医生");
            }
        }

        return toSessionResponse(session);
    }

    @Override
    @Transactional
    public ConsultationSessionResponse createSession(Long requesterId, CreateSessionRequest request) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        User patient = userRepository.findById(request.patientUserId())
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        Family family = familyRepository.findById(request.familyId())
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));

        User doctor = null;
        if (request.doctorId() != null) {
            doctor = userRepository.findById(request.doctorId())
                    .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
            ensureDoctorAccess(requesterId, family);
        }

        ConsultationSession session = createNewSession(doctor, patient, family, request.title());
        return toSessionResponse(session);
    }

    @Override
    public List<ConsultationSessionResponse> listSessionsForDoctor(Long doctorId, Long familyId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(40401, "医生不存在"));
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        
        ensureDoctorAccess(doctorId, family);
        
        List<ConsultationSession> sessions = sessionRepository.findByDoctorOrderByLastMessageAtDesc(doctor);
        return sessions.stream()
                .filter(s -> Objects.equals(s.getFamily().getId(), familyId))
                .map(this::toSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsultationSessionResponse> listSessionsForPatient(Long patientUserId, Long familyId) {
        User patient = userRepository.findById(patientUserId)
                .orElseThrow(() -> new BusinessException(40401, "患者不存在"));
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(40402, "家庭不存在"));
        
        List<ConsultationSession> sessions = sessionRepository.findByPatientOrderByLastMessageAtDesc(patient);
        return sessions.stream()
                .filter(s -> Objects.equals(s.getFamily().getId(), familyId))
                .map(this::toSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultationSessionResponse getSession(Long requesterId, Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(40406, "会话不存在"));
        
        // 验证权限
        verifySessionAccess(requesterId, session);
        
        return toSessionResponse(session);
    }

    @Override
    public List<ConsultationMessageResponse> getMessages(Long requesterId, Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(40406, "会话不存在"));
        
        verifySessionAccess(requesterId, session);
        
        List<ConsultationMessage> messages = messageRepository.findBySessionOrderByCreatedAtAsc(session);
        return messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConsultationMessageResponse sendMessage(Long requesterId, ConsultationMessageRequest request) {
        ConsultationSession session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new BusinessException(40406, "会话不存在"));
        
        User sender = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        
        // 验证权限
        verifySessionAccess(requesterId, session);
        
        // 确定发送者类型
        String senderType = determineSenderType(sender, session);
        
        // 创建消息
        ConsultationMessage message = ConsultationMessage.builder()
                .session(session)
                .sender(sender)
                .senderType(senderType)
                .content(request.content())
                .messageType(request.messageType() != null ? request.messageType() : "TEXT")
                .templateId(request.templateId())
                .readByDoctor(senderType.equals("DOCTOR"))
                .readByPatient(!senderType.equals("DOCTOR"))
                .build();
        
        message = messageRepository.save(message);
        
        // 更新会话的最后消息时间和未读数
        session.setLastMessageAt(message.getCreatedAt());
        if (senderType.equals("DOCTOR")) {
            session.setUnreadCountPatient(session.getUnreadCountPatient() + 1);
        } else {
            session.setUnreadCountDoctor(session.getUnreadCountDoctor() + 1);
        }
        sessionRepository.save(session);
        
        return toMessageResponse(message);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long requesterId, Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(40406, "会话不存在"));
        
        verifySessionAccess(requesterId, session);
        
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        
        boolean isDoctor = requester.getRole() != null && 
                requester.getRole().name().equals("DOCTOR");
        
        if (isDoctor) {
            messageRepository.markAllReadByDoctor(session);
            session.setUnreadCountDoctor(0);
        } else {
            messageRepository.markAllReadByPatient(session);
            session.setUnreadCountPatient(0);
        }
        sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void closeSession(Long requesterId, Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(40406, "会话不存在"));
        
        verifySessionAccess(requesterId, session);
        
        session.setStatus("CLOSED");
        sessionRepository.save(session);
    }

    // ==================== 私有辅助方法 ====================

    private ConsultationSession createNewSession(User doctor, User patient, Family family, String title) {
        ConsultationSession session = ConsultationSession.builder()
                .patient(patient)
                .family(family)
                .doctor(doctor)
                .title(title != null ? title : generateDefaultTitle(patient))
                .status("ACTIVE")
                .unreadCountDoctor(0)
                .unreadCountPatient(0)
                .lastMessageAt(LocalDateTime.now())
                .build();
        return sessionRepository.save(session);
    }

    private String generateDefaultTitle(User patient) {
        return "与" + patient.getNickname() + "的咨询";
    }

    private String determineSenderType(User sender, ConsultationSession session) {
        if (session.getDoctor() != null && Objects.equals(sender.getId(), session.getDoctor().getId())) {
            return "DOCTOR";
        } else if (Objects.equals(sender.getId(), session.getPatient().getId())) {
            return "MEMBER";
        } else {
            return "FAMILY_MEMBER";
        }
    }

    private void verifySessionAccess(Long requesterId, ConsultationSession session) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        
        boolean isDoctor = requester.getRole() != null && 
                requester.getRole().name().equals("DOCTOR");
        
        if (isDoctor) {
            if (session.getDoctor() == null || !Objects.equals(session.getDoctor().getId(), requesterId)) {
                throw new BusinessException(40301, "无权访问该会话");
            }
        } else {
            // 患者或家庭成员可以访问
            if (!Objects.equals(session.getPatient().getId(), requesterId) &&
                !Objects.equals(session.getFamily().getOwner().getId(), requesterId)) {
                // 检查是否是家庭成员
                boolean isFamilyMember = familyMemberRepository.findByFamilyAndUser(session.getFamily(), requester).isPresent();
                if (!isFamilyMember) {
                    throw new BusinessException(40301, "无权访问该会话");
                }
            }
        }
    }

    private void ensureDoctorAccess(Long doctorId, Family family) {
        boolean isDoctor = familyDoctorRepository.findByFamily(family).stream()
                .anyMatch(fd -> Objects.equals(fd.getDoctor().getId(), doctorId));
        if (!isDoctor) {
            throw new BusinessException(40301, "无权访问该家庭");
        }
    }

    private ConsultationSessionResponse toSessionResponse(ConsultationSession session) {
        return new ConsultationSessionResponse(
                session.getId(),
                session.getPatient().getId(),
                session.getPatient().getNickname(),
                readAvatar(session.getPatient().getId()),
                session.getFamily().getId(),
                session.getFamily().getName(),
                session.getDoctor() != null ? session.getDoctor().getId() : null,
                session.getDoctor() != null ? session.getDoctor().getNickname() : null,
                session.getDoctor() != null ? readAvatar(session.getDoctor().getId()) : null,
                session.getTitle(),
                session.getStatus(),
                session.getUnreadCountDoctor(),
                session.getUnreadCountPatient(),
                session.getLastMessageAt(),
                session.getCreatedAt(),
                session.getUpdatedAt(),
                session.getIsAiTriaged(),
                session.getTriageSummary(),
                session.getPatientSymptoms()
        );
    }

    private ConsultationMessageResponse toMessageResponse(ConsultationMessage message) {
        return new ConsultationMessageResponse(
                message.getId(),
                message.getSession().getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getSenderType(),
                message.getContent(),
                message.getMessageType(),
                message.getTemplateId(),
                message.getReadByDoctor(),
                message.getReadByPatient(),
                message.getCreatedAt()
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
}

