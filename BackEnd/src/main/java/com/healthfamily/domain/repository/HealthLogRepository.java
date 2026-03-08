package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
/**
 * 健康日志数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.Optional;

public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {

    Optional<HealthLog> findByIdAndUser(Long id, User user);

    Optional<HealthLog> findByUserAndLogDateAndType(User user, LocalDate logDate, HealthLogType type);

    List<HealthLog> findByUserOrderByLogDateDesc(User user);

    List<HealthLog> findByUserAndLogDateBetweenOrderByLogDateDesc(User user, LocalDate startDate, LocalDate endDate);

    List<HealthLog> findByUser_IdAndLogDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<HealthLog> findByUser_IdAndLogDateBetweenOrderByLogDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

    List<HealthLog> findByUser_IdAndTypeOrderByLogDateDesc(Long userId, HealthLogType type);

    List<HealthLog> findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(Long userId);

    long countByUser_IdIn(List<Long> userIds);
}

