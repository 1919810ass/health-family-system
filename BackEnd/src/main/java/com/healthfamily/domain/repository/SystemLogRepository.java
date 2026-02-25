package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.SystemLogType;
import com.healthfamily.domain.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
/**
 * 系统日志数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(SystemLogType type, LocalDateTime start, LocalDateTime end);
    List<SystemLog> findByLevelInAndCreatedAtBetweenOrderByCreatedAtDesc(List<String> levels, LocalDateTime start, LocalDateTime end);
    List<SystemLog> findTop20ByLevelInOrderByCreatedAtDesc(List<String> levels);
}
