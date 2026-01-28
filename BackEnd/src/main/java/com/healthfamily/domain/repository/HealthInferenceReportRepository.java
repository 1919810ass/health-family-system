package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.HealthInferenceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthInferenceReportRepository extends JpaRepository<HealthInferenceReport, Long> {

    List<HealthInferenceReport> findByUserIdOrderByReportDateDesc(Long userId);

    Optional<HealthInferenceReport> findByUserIdAndReportDate(Long userId, LocalDate reportDate);
}
