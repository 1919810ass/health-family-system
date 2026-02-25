package com.healthfamily.service;

import com.healthfamily.domain.entity.AiRequestLog;
import com.healthfamily.domain.repository.AiRequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiMonitorService {

    private final AiRequestLogRepository aiRequestLogRepository;

    @Async
    public void saveLog(AiRequestLog logEntity) {
        log.info("Saving AI Request Log: {}", logEntity);
        try {
            aiRequestLogRepository.save(logEntity);
            log.info("Successfully saved AI Request Log: ID={}", logEntity.getId());
        } catch (Exception e) {
            log.error("Failed to save AI request log", e);
        }
    }

    public Map<String, Object> getDashboardStats() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        Map<String, Object> stats = new HashMap<>();

        try {
            long totalRequests = aiRequestLogRepository.countByCreateTimeGreaterThanEqual(today);
            Double avgLatencyVal = aiRequestLogRepository.avgLatencySince(today);
            long avgLatency = avgLatencyVal != null ? Math.round(avgLatencyVal) : 0;
            
            long errorCount = aiRequestLogRepository.countByStatusAndCreateTimeGreaterThanEqual("FAIL", today);
            double errorRate = totalRequests > 0 ? (double) errorCount / totalRequests : 0.0;
            
            Long todayTokensSum = aiRequestLogRepository.sumTotalTokensSince(today);
            long todayTokens = todayTokensSum != null ? todayTokensSum : 0;
            
            long activeUsers = aiRequestLogRepository.countActiveUsersSince(today);

            // 1. Basic Stats
            stats.put("todayTokens", todayTokens);
            stats.put("totalCalls", totalRequests);
            stats.put("totalRequests", totalRequests); // alias
            stats.put("avgLatency", avgLatency);
            stats.put("errorRate", Math.round(errorRate * 10000.0) / 100.0); // Keep percentage format e.g. 5.23 for 5.23%
            stats.put("activeUsers", activeUsers);

            // 2. Recent logs
            List<AiRequestLog> recentLogs = aiRequestLogRepository.findTop10ByOrderByCreateTimeDesc();
            stats.put("recentLogs", recentLogs);

            // 3. Hourly Trend
            List<AiRequestLog> todayLogs = aiRequestLogRepository.findByCreateTimeGreaterThanEqual(today);
            List<Map<String, Object>> hourlyStats = calculateHourlyStats(todayLogs);
            stats.put("hourlyTrend", hourlyStats);

            // 4. Token Usage Ranking
            List<Map<String, Object>> topEndpoints = aiRequestLogRepository.getTopEndpoints(today, PageRequest.of(0, 10));
            stats.put("topEndpoints", topEndpoints);

            // 5. Latency Distribution
            List<Long> latencies = aiRequestLogRepository.getLatencies(today);
            Map<String, Long> percentiles = calculatePercentiles(latencies);
            stats.put("latencyDistribution", percentiles);

        } catch (Exception e) {
            log.error("Error generating AI dashboard stats", e);
            stats.put("error", "Failed to load stats: " + e.getMessage());
            // Provide safe defaults
            stats.putIfAbsent("todayTokens", 0L);
            stats.putIfAbsent("totalRequests", 0L);
            stats.putIfAbsent("avgLatency", 0L);
            stats.putIfAbsent("errorRate", 0.0);
            stats.putIfAbsent("activeUsers", 0L);
            stats.putIfAbsent("hourlyTrend", List.of());
            stats.putIfAbsent("topEndpoints", List.of());
            stats.putIfAbsent("latencyDistribution", Map.of("p50", 0L, "p90", 0L, "p99", 0L));
        }

        return stats;
    }

    private List<Map<String, Object>> calculateHourlyStats(List<AiRequestLog> logs) {
        Map<Integer, int[]> hourlyData = new HashMap<>(); // hour -> [count, errors]
        for (int i = 0; i < 24; i++) {
            hourlyData.put(i, new int[]{0, 0});
        }

        for (AiRequestLog log : logs) {
            int hour = log.getCreateTime().getHour();
            int[] data = hourlyData.get(hour);
            if (data != null) {
                data[0]++;
                if ("FAIL".equals(log.getStatus())) {
                    data[1]++;
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("hour", i);
            entry.put("count", hourlyData.get(i)[0]);
            entry.put("errors", hourlyData.get(i)[1]);
            result.add(entry);
        }
        return result;
    }

    private Map<String, Long> calculatePercentiles(List<Long> latencies) {
        Map<String, Long> p = new HashMap<>();
        if (latencies.isEmpty()) {
            p.put("p50", 0L);
            p.put("p90", 0L);
            p.put("p99", 0L);
            return p;
        }

        int size = latencies.size();
        p.put("p50", latencies.get((int) (size * 0.50)));
        p.put("p90", latencies.get((int) (size * 0.90)));
        p.put("p99", latencies.get((int) (size * 0.99)));
        return p;
    }
}
