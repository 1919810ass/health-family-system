package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.AbnormalHandlingRecord;
import com.healthfamily.domain.entity.HealthAlert;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbnormalHandlingRecordRepository extends JpaRepository<AbnormalHandlingRecord, Long> {
    List<AbnormalHandlingRecord> findByAlert(HealthAlert alert);
    List<AbnormalHandlingRecord> findByDoctor(User doctor);
    List<AbnormalHandlingRecord> findByPatient(User patient);
    List<AbnormalHandlingRecord> findByAlertFamilyId(Long familyId);
    List<AbnormalHandlingRecord> findByAlertFamilyIdAndPatient(Long familyId, User patient);
}