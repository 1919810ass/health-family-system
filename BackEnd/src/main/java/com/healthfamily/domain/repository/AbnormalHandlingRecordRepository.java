package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.AbnormalHandlingRecord;
import com.healthfamily.domain.entity.HealthAlert;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * AbnormalHandlingRecord数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface AbnormalHandlingRecordRepository extends JpaRepository<AbnormalHandlingRecord, Long> {
    List<AbnormalHandlingRecord> findByAlert(HealthAlert alert);
    List<AbnormalHandlingRecord> findByDoctor(User doctor);
    List<AbnormalHandlingRecord> findByPatient(User patient);
    List<AbnormalHandlingRecord> findByAlertFamilyId(Long familyId);
    List<AbnormalHandlingRecord> findByAlertFamilyIdAndPatient(Long familyId, User patient);
}