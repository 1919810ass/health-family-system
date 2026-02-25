package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.HealthInferenceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 健康Inference报告数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
@Repository
public interface HealthInferenceReportRepository extends JpaRepository<HealthInferenceReport, Long> {

    List<HealthInferenceReport> findByUserIdOrderByReportDateDesc(Long userId);

    Optional<HealthInferenceReport> findByUserIdAndReportDate(Long userId, LocalDate reportDate);
}
