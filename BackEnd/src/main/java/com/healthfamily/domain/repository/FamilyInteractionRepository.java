package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.FamilyInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FamilyInteractionRepository extends JpaRepository<FamilyInteraction, Long> {
    List<FamilyInteraction> findByFamilyIdOrderByCreatedAtDesc(Long familyId);
    List<FamilyInteraction> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);
    List<FamilyInteraction> findByFamilyIdAndCreatedAtAfter(Long familyId, LocalDateTime date);
}
