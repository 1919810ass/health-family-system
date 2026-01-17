package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.HealthThreshold;
import com.healthfamily.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthThresholdRepository extends JpaRepository<HealthThreshold, Long> {
    List<HealthThreshold> findByUser(User user);
    Optional<HealthThreshold> findByUserAndMetric(User user, String metric);
}
