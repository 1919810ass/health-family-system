package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {

    Optional<HealthLog> findByIdAndUser(Long id, User user);

    Optional<HealthLog> findByUserAndLogDateAndType(User user, LocalDate logDate, HealthLogType type);

    List<HealthLog> findByUserOrderByLogDateDesc(User user);

    List<HealthLog> findByUserAndLogDateBetweenOrderByLogDateDesc(User user, LocalDate startDate, LocalDate endDate);

    List<HealthLog> findByUser_IdAndTypeOrderByLogDateDesc(Long userId, HealthLogType type);

    List<HealthLog> findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(Long userId);

    long countByUser_IdIn(List<Long> userIds);
}

