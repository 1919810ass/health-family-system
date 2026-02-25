package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.FamilyInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 家庭Interaction数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
@Repository
public interface FamilyInteractionRepository extends JpaRepository<FamilyInteraction, Long> {
    List<FamilyInteraction> findByFamilyIdOrderByCreatedAtDesc(Long familyId);
    List<FamilyInteraction> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);
    List<FamilyInteraction> findByFamilyIdAndCreatedAtAfter(Long familyId, LocalDateTime date);
}
