package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.FamilyTcmHealthOverview;
/**
 * 家庭中医体质健康Overview数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyTcmHealthOverviewRepository extends JpaRepository<FamilyTcmHealthOverview, Long> {
    FamilyTcmHealthOverview findTopByFamilyIdOrderByGeneratedAtDesc(Long familyId);
}