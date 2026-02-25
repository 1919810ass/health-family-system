package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.AiUsageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {

    @Query("SELECT COUNT(l) FROM AiUsageLog l WHERE l.createdAt >= :since")
    long countSince(LocalDateTime since);

    @Query("SELECT SUM(l.totalTokens) FROM AiUsageLog l WHERE l.createdAt >= :since")
    Long sumTokensSince(LocalDateTime since);

    @Query("SELECT AVG(l.latencyMs) FROM AiUsageLog l WHERE l.createdAt >= :since")
    Double avgLatencySince(LocalDateTime since);

    @Query("SELECT COUNT(l) FROM AiUsageLog l WHERE l.success = false AND l.createdAt >= :since")
    long countErrorsSince(LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT l.userId) FROM AiUsageLog l WHERE l.createdAt >= :since")
    long countActiveUsersSince(LocalDateTime since);

    @Query("SELECT l.endpoint AS endpoint, SUM(l.totalTokens) AS tokens FROM AiUsageLog l WHERE l.createdAt >= :since GROUP BY l.endpoint ORDER BY tokens DESC")
    List<Map<String, Object>> getTopEndpoints(LocalDateTime since, Pageable pageable);

    List<AiUsageLog> findByCreatedAtGreaterThanEqual(LocalDateTime since);

    @Query("SELECT l.latencyMs FROM AiUsageLog l WHERE l.createdAt >= :since ORDER BY l.latencyMs ASC")
    List<Long> getLatencies(LocalDateTime since);
}
