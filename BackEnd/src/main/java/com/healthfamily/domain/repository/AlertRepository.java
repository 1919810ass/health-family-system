package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.AlertStatus;
import com.healthfamily.domain.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Alert数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    List<Alert> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(String type, LocalDateTime start, LocalDateTime end);

    @Query("SELECT count(a) FROM Alert a JOIN a.family f JOIN FamilyDoctor fd ON f.id = fd.family.id WHERE fd.doctor.id = :doctorId AND a.status = :status")
    int countByDoctorIdAndStatus(@org.springframework.data.repository.query.Param("doctorId") Long doctorId, @org.springframework.data.repository.query.Param("status") AlertStatus status);
}
