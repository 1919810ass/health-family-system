package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.ReportStatus;
import com.healthfamily.domain.entity.HealthReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthReportRepository extends JpaRepository<HealthReport, Long> {
    List<HealthReport> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<HealthReport> findByStatusOrderByCreatedAtAsc(ReportStatus status);
}
