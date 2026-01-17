package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.DoctorNote;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorNoteRepository extends JpaRepository<DoctorNote, Long> {

    /**
     * 根据患者和医生查询病历记录（按创建时间倒序）
     */
    List<DoctorNote> findByPatientAndDoctorOrderByCreatedAtDesc(User patient, User doctor);

    /**
     * 根据患者查询所有病历记录（按创建时间倒序）
     */
    List<DoctorNote> findByPatientOrderByCreatedAtDesc(User patient);

    /**
     * 根据家庭查询所有病历记录（按创建时间倒序）
     */
    List<DoctorNote> findByFamilyOrderByCreatedAtDesc(Family family);

    /**
     * 根据患者和医生查询病历记录（按问诊日期倒序）
     */
    List<DoctorNote> findByPatientAndDoctorOrderByConsultationDateDesc(User patient, User doctor);

    /**
     * 根据ID和医生查询（用于权限验证）
     */
    Optional<DoctorNote> findByIdAndDoctor(Long id, User doctor);

    /**
     * 根据ID和患者查询（用于家庭端查看）
     */
    Optional<DoctorNote> findByIdAndPatient(Long id, User patient);
}

