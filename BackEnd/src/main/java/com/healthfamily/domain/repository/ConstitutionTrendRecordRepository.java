package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.ConstitutionTrendRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Constitution趋势Record数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface ConstitutionTrendRecordRepository extends JpaRepository<ConstitutionTrendRecord, Long> {
    List<ConstitutionTrendRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT ctr FROM ConstitutionTrendRecord ctr WHERE ctr.userId = :userId AND ctr.createdAt >= :startDate ORDER BY ctr.createdAt")
    List<ConstitutionTrendRecord> findByUserIdAndCreatedAtAfter(@Param("userId") Long userId, 
                                                               @Param("startDate") java.time.LocalDateTime startDate);
}