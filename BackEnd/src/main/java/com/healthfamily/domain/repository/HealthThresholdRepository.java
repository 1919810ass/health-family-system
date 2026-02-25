package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.HealthThreshold;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 健康阈值数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
@Repository
public interface HealthThresholdRepository extends JpaRepository<HealthThreshold, Long> {
    List<HealthThreshold> findByUser(User user);
    Optional<HealthThreshold> findByUserAndMetric(User user, String metric);
}
