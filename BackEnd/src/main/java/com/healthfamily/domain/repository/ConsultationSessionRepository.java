package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.ConsultationSession;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, Long> {

    /**
     * 根据患者和医生查找会话
     */
    Optional<ConsultationSession> findByPatientAndDoctor(User patient, User doctor);

    /**
     * 根据患者查找所有会话（按最后消息时间倒序）
     */
    List<ConsultationSession> findByPatientOrderByLastMessageAtDesc(User patient);

    /**
     * 根据医生查找所有会话（按最后消息时间倒序）
     */
    List<ConsultationSession> findByDoctorOrderByLastMessageAtDesc(User doctor);

    /**
     * 根据家庭查找所有会话（按最后消息时间倒序）
     */
    List<ConsultationSession> findByFamilyOrderByLastMessageAtDesc(Family family);

    /**
     * 根据患者和家庭查找会话（家庭成员之间的咨询）
     */
    List<ConsultationSession> findByPatientAndFamilyAndDoctorIsNullOrderByLastMessageAtDesc(User patient, Family family);

    /**
     * 根据状态查找会话
     */
    List<ConsultationSession> findByStatusOrderByLastMessageAtDesc(String status);
}

