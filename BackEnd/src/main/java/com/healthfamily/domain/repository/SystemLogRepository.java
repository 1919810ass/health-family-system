package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.SystemLogType;
import com.healthfamily.domain.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(SystemLogType type, LocalDateTime start, LocalDateTime end);
}
