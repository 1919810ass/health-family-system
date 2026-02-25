package com.healthfamily.domain.repository;

import com.healthfamily.domain.constant.ReportStatus;
import com.healthfamily.domain.entity.HealthReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康报告数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
@Repository
public interface HealthReportRepository extends JpaRepository<HealthReport, Long> {
    List<HealthReport> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<HealthReport> findByStatusOrderByCreatedAtAsc(ReportStatus status);
    int countByCreatedAtAfter(LocalDateTime startDate);
}
