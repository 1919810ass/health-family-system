package com.healthfamily.service;

import com.healthfamily.domain.constant.UserRole;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConsultationSessionRepository;
import com.healthfamily.domain.repository.HealthPlanRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.web.dto.DoctorCollaborationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.healthfamily.domain.entity.ConsultationSession;
import com.healthfamily.domain.entity.HealthPlan;
import com.healthfamily.web.dto.DoctorCollaborationDetailDto;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDoctorCollaborationService {

    private final UserRepository userRepository;
    private final ConsultationSessionRepository sessionRepository;
    private final HealthPlanRepository healthPlanRepository;

    public List<DoctorCollaborationDto> getDoctorCollaborationStats() {
        List<User> doctors = userRepository.findByRole(UserRole.DOCTOR);
        return doctors.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    public DoctorCollaborationDetailDto getDoctorDetail(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorCollaborationDetailDto detail = new DoctorCollaborationDetailDto();
        detail.setId(doctor.getId());
        detail.setNickname(doctor.getNickname());
        detail.setPhone(doctor.getPhone());

        // 1. 获取最近的活跃会话 (Top 10)
        List<ConsultationSession> sessions = sessionRepository.findByDoctorOrderByLastMessageAtDesc(doctor);
        List<DoctorCollaborationDetailDto.ConsultationSessionInfo> sessionInfos = sessions.stream()
                .limit(10)
                .map(s -> {
                    DoctorCollaborationDetailDto.ConsultationSessionInfo info = new DoctorCollaborationDetailDto.ConsultationSessionInfo();
                    info.setSessionId(s.getId());
                    info.setTitle(s.getTitle());
                    info.setPatientName(s.getPatient() != null ? s.getPatient().getNickname() : "未知患者");
                    info.setLastMessageAt(s.getLastMessageAt());
                    info.setStatus(s.getStatus());
                    return info;
                })
                .collect(Collectors.toList());
        detail.setActiveSessions(sessionInfos);

        // 2. 获取负责的健康计划 (Top 10)
        List<HealthPlan> plans = healthPlanRepository.findByDoctorOrderByCreatedAtDesc(doctor);
        List<DoctorCollaborationDetailDto.HealthPlanInfo> planInfos = plans.stream()
                .limit(10)
                .map(p -> {
                    DoctorCollaborationDetailDto.HealthPlanInfo info = new DoctorCollaborationDetailDto.HealthPlanInfo();
                    info.setPlanId(p.getId());
                    info.setTitle(p.getTitle());
                    info.setType(p.getType().name());
                    info.setPatientName(p.getPatient() != null ? p.getPatient().getNickname() : "未知患者");
                    info.setStatus(p.getStatus().name());
                    return info;
                })
                .collect(Collectors.toList());
        detail.setActivePlans(planInfos);

        return detail;
    }

    /**
     * 分配任务（简单演示：将指定医生的所有PENDING会话转移给另一位医生）
     * 实际业务中可能是针对单个患者或会话的转移
     */
    @Transactional
    public void assignTask(Long sourceDoctorId, Long targetDoctorId) {
        if (sourceDoctorId.equals(targetDoctorId)) {
            throw new RuntimeException("不能分配给自己");
        }
        User targetDoctor = userRepository.findById(targetDoctorId)
                .orElseThrow(() -> new RuntimeException("Target doctor not found"));

        // 查找源医生的所有活跃会话
        User sourceDoctor = userRepository.findById(sourceDoctorId)
                .orElseThrow(() -> new RuntimeException("Source doctor not found"));
        
        // 这里仅作为演示：转移最近的一个活跃会话
        List<ConsultationSession> sourceSessions = sessionRepository.findByDoctorOrderByLastMessageAtDesc(sourceDoctor);
        if (!sourceSessions.isEmpty()) {
            ConsultationSession session = sourceSessions.get(0);
            session.setDoctor(targetDoctor);
            sessionRepository.save(session);
        }
    }


    private DoctorCollaborationDto mapToDto(User doctor) {
        DoctorCollaborationDto dto = new DoctorCollaborationDto();
        dto.setId(doctor.getId());
        dto.setNickname(doctor.getNickname());
        dto.setPhone(doctor.getPhone());
        
        // 1. 正在进行的咨询 (ACTIVE 状态且分配给该医生)
        // 注意：数据库定义中 status 默认是 'ACTIVE'
        int activeSessions = sessionRepository.countByDoctor_IdAndStatus(doctor.getId(), "ACTIVE");
        dto.setActiveConsultations(activeSessions);

        // 2. 负责的健康计划总数
        int totalPlans = healthPlanRepository.countByDoctor_Id(doctor.getId());
        dto.setTotalHealthPlans(totalPlans);

        // 3. 最后活跃时间 (取最后一次登录或最后一条消息)
        dto.setLastActivityAt(doctor.getLastLoginAt());

        // 4. 协作状态判定逻辑 (根据工作负荷)
        if (activeSessions > 10) {
            dto.setCollaborationStatus("高负载");
        } else if (activeSessions > 5) {
            dto.setCollaborationStatus("忙碌");
        } else if (doctor.getLastLoginAt() != null && doctor.getLastLoginAt().isAfter(LocalDateTime.now().minusHours(1))) {
            dto.setCollaborationStatus("空闲/在线");
        } else {
            dto.setCollaborationStatus("离线");
        }

        return dto;
    }
}
