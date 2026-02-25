package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.AiRequestLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {

    long countByCreateTimeGreaterThanEqual(LocalDateTime since);

    @Query("SELECT AVG(l.latency) FROM AiRequestLog l WHERE l.createTime >= :since")
    Double avgLatencySince(LocalDateTime since);

    long countByStatusAndCreateTimeGreaterThanEqual(String status, LocalDateTime since);

    List<AiRequestLog> findTop10ByOrderByCreateTimeDesc();
    
    // For compatibility with dashboard requirements
    @Query("SELECT COUNT(l) FROM AiRequestLog l WHERE l.createTime >= :since")
    long countSince(LocalDateTime since);

    @Query("SELECT COUNT(l) FROM AiRequestLog l WHERE l.status = 'FAIL' AND l.createTime >= :since")
    long countErrorsSince(LocalDateTime since);

    // New methods for full dashboard stats
    @Query("SELECT SUM(l.inputTokens + l.outputTokens) FROM AiRequestLog l WHERE l.createTime >= :since")
    Long sumTotalTokensSince(LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT l.userId) FROM AiRequestLog l WHERE l.createTime >= :since")
    long countActiveUsersSince(LocalDateTime since);

    @Query("SELECT l.serviceName AS endpoint, SUM(l.inputTokens + l.outputTokens) AS tokens FROM AiRequestLog l WHERE l.createTime >= :since GROUP BY l.serviceName ORDER BY tokens DESC")
    List<Map<String, Object>> getTopEndpoints(LocalDateTime since, Pageable pageable);

    @Query("SELECT l.latency FROM AiRequestLog l WHERE l.createTime >= :since ORDER BY l.latency ASC")
    List<Long> getLatencies(LocalDateTime since);

    // For hourly trend, fetching all logs for today is feasible if volume isn't massive.
    // If volume is massive, we should use a group by query, but that's database specific for date truncation.
    // Let's stick to fetching logs and processing in Java for consistency, as done in previous implementation.
    List<AiRequestLog> findByCreateTimeGreaterThanEqual(LocalDateTime since);
}
