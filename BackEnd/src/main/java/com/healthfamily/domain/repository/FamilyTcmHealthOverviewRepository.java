package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.FamilyTcmHealthOverview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyTcmHealthOverviewRepository extends JpaRepository<FamilyTcmHealthOverview, Long> {
    FamilyTcmHealthOverview findTopByFamilyIdOrderByGeneratedAtDesc(Long familyId);
}