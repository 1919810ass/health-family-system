package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    List<Alert> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(String type, LocalDateTime start, LocalDateTime end);
}
