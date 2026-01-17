package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.ConstitutionTrendRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConstitutionTrendRecordRepository extends JpaRepository<ConstitutionTrendRecord, Long> {
    List<ConstitutionTrendRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT ctr FROM ConstitutionTrendRecord ctr WHERE ctr.userId = :userId AND ctr.createdAt >= :startDate ORDER BY ctr.createdAt")
    List<ConstitutionTrendRecord> findByUserIdAndCreatedAtAfter(@Param("userId") Long userId, 
                                                               @Param("startDate") java.time.LocalDateTime startDate);
}