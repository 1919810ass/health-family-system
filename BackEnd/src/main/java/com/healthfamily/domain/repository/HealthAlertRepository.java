package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.AlertStatus;
import com.healthfamily.domain.entity.HealthAlert;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
/**
 * 健康Alert数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {
    List<HealthAlert> findByFamily_IdOrderByCreatedAtDesc(Long familyId);
    List<HealthAlert> findByUserOrderByCreatedAtDesc(User user);
    List<HealthAlert> findByStatusAndCreatedAtLessThanEqual(AlertStatus status, LocalDateTime time);

    long countByUserAndCreatedAtAfterAndStatusIn(User user, LocalDateTime createdAt, List<AlertStatus> statuses);
}

