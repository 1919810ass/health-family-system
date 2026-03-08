package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.DoctorHealthReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorHealthReportRepository extends JpaRepository<DoctorHealthReport, Long> {
    List<DoctorHealthReport> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<DoctorHealthReport> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
    List<DoctorHealthReport> findByFamilyIdOrderByCreatedAtDesc(Long familyId);
}
