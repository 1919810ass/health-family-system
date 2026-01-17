package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.AlertStatus;
import com.healthfamily.domain.entity.HealthAlert;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {
    List<HealthAlert> findByFamily_IdOrderByCreatedAtDesc(Long familyId);
    List<HealthAlert> findByUserOrderByCreatedAtDesc(User user);
    List<HealthAlert> findByStatusAndCreatedAtLessThanEqual(AlertStatus status, LocalDateTime time);
}

