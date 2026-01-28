package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.domain.constant.HealthDataSource;
import com.healthfamily.service.HealthDataAiService;
import com.healthfamily.service.HealthLogService;
import com.healthfamily.web.dto.HealthLogRequest;
import com.healthfamily.web.dto.HealthLogResponse;
import com.healthfamily.web.dto.HealthLogStatisticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthLogServiceImpl implements HealthLogService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final HealthLogRepository healthLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final HealthDataAiService healthDataAiService;

    @Override
    @Transactional
    public HealthLogResponse createLog(Long userId, HealthLogRequest request) {
        if (request == null) {
            throw new BusinessException(40001, "请求参数不能为空");
        }
        
        User user = loadUser(userId);
        ensureUnique(user, request.type(), request.logDate(), null);

        // AI数据清洗和标准化
        Map<String, Object> cleanedContent;
        try {
            cleanedContent = healthDataAiService.cleanAndNormalize(
                    request.content(), 
                    extractDataType(request.content())
            );
        } catch (Exception e) {
            log.error("数据清洗失败: {}", e.getMessage());
            cleanedContent = request.content() != null ? new HashMap<>(request.content()) : new HashMap<>();
        }

        // 异常检测（如果是体征数据）
        Boolean isAbnormal = null;
        String metadataJson = null;
        if (request.type() == com.healthfamily.domain.constant.HealthLogType.VITALS) {
            try {
                Double value = extractNumericValue(cleanedContent);
                if (value != null) {
                    HealthDataAiService.AnomalyResult anomaly = healthDataAiService.detectAnomaly(
                            userId, 
                            extractDataType(request.content()), 
                            value, 
                            null
                    );
                    isAbnormal = anomaly.isAnomaly();
                    
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("anomaly", Map.of(
                            "isAnomaly", anomaly.isAnomaly(),
                            "reason", anomaly.reason() != null ? anomaly.reason() : "",
                            "severity", anomaly.severity(),
                            "suggestion", anomaly.suggestion() != null ? anomaly.suggestion() : ""
                    ));
                    metadataJson = objectMapper.writeValueAsString(metadata);
                }
            } catch (Exception e) {
                log.error("异常检测失败: {}", e.getMessage(), e);
                // 异常检测失败不影响日志保存，仅记录日志
            }
        }

        // 从请求中获取数据源（如果content中包含）
        HealthDataSource dataSource = HealthDataSource.MANUAL;
        if (request.content() != null && request.content().containsKey("_dataSource")) {
            try {
                String sourceStr = String.valueOf(request.content().get("_dataSource"));
                dataSource = HealthDataSource.valueOf(sourceStr);
            } catch (Exception e) {
                // 忽略，使用默认值
            }
        }
        
        HealthLog log = HealthLog.builder()
                .user(user)
                .logDate(request.logDate())
                .type(request.type())
                .contentJson(toJson(cleanedContent))
                .score(request.score())
                .dataSource(dataSource)
                .isAbnormal(isAbnormal)
                .metadataJson(metadataJson)
                .build();
        HealthLog saved = healthLogRepository.save(log);
        return toResponse(saved);
    }

    private String extractDataType(Map<String, Object> content) {
        if (content == null) return "UNKNOWN";
        // 尝试从content中识别数据类型
        if (content.containsKey("systolic") || content.containsKey("diastolic")) {
            return "血压";
        }
        if (content.containsKey("glucose") || content.containsKey("bloodSugar")) {
            return "血糖";
        }
        if (content.containsKey("temperature") || content.containsKey("temp")) {
            return "体温";
        }
        if (content.containsKey("heartRate") || content.containsKey("hr")) {
            return "心率";
        }
        if (content.containsKey("weight")) {
            return "体重";
        }
        return "UNKNOWN";
    }

    private Double extractNumericValue(Map<String, Object> content) {
        if (content == null) return null;
        Object value = content.get("value");
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        // 如果是血压，取平均值
        if (content.containsKey("systolic") && content.containsKey("diastolic")) {
            Object sys = content.get("systolic");
            Object dia = content.get("diastolic");
            if (sys instanceof Number && dia instanceof Number) {
                return (((Number) sys).doubleValue() + ((Number) dia).doubleValue()) / 2.0;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public HealthLogResponse updateLog(Long userId, Long logId, HealthLogRequest request) {
        User user = loadUser(userId);
        HealthLog log = healthLogRepository.findByIdAndUser(logId, user)
                .orElseThrow(() -> new BusinessException(40411, "日志不存在"));

        if (!log.getLogDate().equals(request.logDate()) || log.getType() != request.type()) {
            ensureUnique(user, request.type(), request.logDate(), log.getId());
        }

        log.setLogDate(request.logDate());
        log.setType(request.type());
        log.setContentJson(toJson(request.content()));
        log.setScore(request.score());
        HealthLog saved = healthLogRepository.save(log);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteLog(Long userId, Long logId) {
        log.info("Request to delete health log. UserId: {}, LogId: {}", userId, logId);
        User user = loadUser(userId);
        HealthLog logToDelete = healthLogRepository.findByIdAndUser(logId, user)
                .orElseThrow(() -> {
                    log.warn("Delete failed - log not found or permission denied. UserId: {}, LogId: {}", userId, logId);
                    return new BusinessException(40411, "日志不存在");
                });
        healthLogRepository.delete(logToDelete);
        log.info("Health log deleted successfully. LogId: {}", logId);
    }

    @Override
    public List<HealthLogResponse> listLogs(Long userId, HealthLogType type, LocalDate startDate, LocalDate endDate) {
        User user = loadUser(userId);
        List<HealthLog> logs = healthLogRepository.findByUserOrderByLogDateDesc(user);
        return logs.stream()
                .filter(log -> type == null || log.getType() == type)
                .filter(log -> withinRange(log.getLogDate(), startDate, endDate))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HealthLogStatisticsResponse getStatistics(Long userId) {
        User user = loadUser(userId);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(29);
        List<HealthLog> logs = healthLogRepository.findByUserAndLogDateBetweenOrderByLogDateDesc(user, start, today);

        HealthLogStatisticsResponse.TrendRange last7Days = buildTrendRange("last_7_days", logs, today.minusDays(6), today);
        HealthLogStatisticsResponse.TrendRange last14Days = buildTrendRange("last_14_days", logs, today.minusDays(13), today);
        HealthLogStatisticsResponse.TrendRange last30Days = buildTrendRange("last_30_days", logs, today.minusDays(29), today);

        return new HealthLogStatisticsResponse(last7Days, last14Days, last30Days);
    }

    private HealthLogStatisticsResponse.TrendRange buildTrendRange(String label,
                                                                   List<HealthLog> logs,
                                                                   LocalDate start,
                                                                   LocalDate end) {
        Map<String, Map<LocalDate, Aggregation>> typeDateMap = new HashMap<>();
        for (HealthLog log : logs) {
            if (log.getLogDate().isBefore(start) || log.getLogDate().isAfter(end)) {
                continue;
            }
            String typeStr = log.getType().name();
            double value = extractValue(log);
            
            typeDateMap
                    .computeIfAbsent(typeStr, key -> new HashMap<>())
                    .computeIfAbsent(log.getLogDate(), key -> new Aggregation())
                    .accumulate(value, log.getType());
        }

        Map<String, List<HealthLogStatisticsResponse.TrendValue>> series = new HashMap<>();
        typeDateMap.forEach((type, dateMap) -> {
            List<HealthLogStatisticsResponse.TrendValue> values = dateMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> new HealthLogStatisticsResponse.TrendValue(
                            entry.getKey(),
                            entry.getValue().getResult(),
                            entry.getValue().count
                    ))
                    .collect(Collectors.toList());
            series.put(type, values);
        });

        return new HealthLogStatisticsResponse.TrendRange(label, series);
    }

    private double extractValue(HealthLog log) {
        try {
            Map<String, Object> content = fromJson(log.getContentJson());
            if (content.isEmpty()) return 0.0;

            switch (log.getType()) {
                case DIET:
                    // 优先取 totalCalories
                    if (content.containsKey("totalCalories")) {
                        return Double.parseDouble(content.get("totalCalories").toString());
                    }
                    // 其次遍历 items 累加 calories
                    if (content.containsKey("items")) {
                        Object itemsObj = content.get("items");
                        if (itemsObj instanceof java.util.List) {
                            java.util.List<?> items = (java.util.List<?>) itemsObj;
                            double total = 0;
                            for (Object itemObj : items) {
                                if (itemObj instanceof Map) {
                                    Map<?, ?> item = (Map<?, ?>) itemObj;
                                    if (item.containsKey("calories")) {
                                        total += Double.parseDouble(item.get("calories").toString());
                                    }
                                }
                            }
                            return total;
                        }
                    }
                    return 0.0;
                case SLEEP:
                    // durationHours
                    if (content.containsKey("durationHours")) {
                        return Double.parseDouble(content.get("durationHours").toString());
                    }
                    // 如果没有 durationHours，尝试通过 bedtime 和 wakeTime 计算
                    if (content.containsKey("bedtime") && content.containsKey("wakeTime")) {
                         String bed = content.get("bedtime").toString();
                         String wake = content.get("wakeTime").toString();
                         try {
                             java.time.LocalTime bedTime = java.time.LocalTime.parse(bed);
                             java.time.LocalTime wakeTime = java.time.LocalTime.parse(wake);
                             // 简单计算跨天
                             long minutes = java.time.temporal.ChronoUnit.MINUTES.between(bedTime, wakeTime);
                             if (minutes < 0) {
                                 minutes += 24 * 60;
                             }
                             double hours = minutes / 60.0;
                             return Math.round(hours * 10.0) / 10.0;
                         } catch (Exception ignore) {}
                    }
                    return 0.0;
                case SPORT:
                    // durationMinutes
                    if (content.containsKey("durationMinutes")) {
                        return Double.parseDouble(content.get("durationMinutes").toString());
                    }
                    return 0.0;
                case MOOD:
                    // level
                    if (content.containsKey("level")) {
                        return Double.parseDouble(content.get("level").toString());
                    }
                    return 0.0;
                case VITALS:
                    // value (数值类型)
                    if (content.containsKey("value") && content.get("value") instanceof Number) {
                        return Double.parseDouble(content.get("value").toString());
                    }
                    // 血压: systolic (仅取收缩压作为趋势展示)
                    if (content.containsKey("systolic")) {
                        return Double.parseDouble(content.get("systolic").toString());
                    }
                    return 0.0;
                default:
                    return 0.0;
            }
        } catch (Exception e) {
            // 解析失败或转换失败，忽略该条记录的数值
            return 0.0;
        }
    }

    private boolean withinRange(LocalDate date, LocalDate start, LocalDate end) {
        if (start != null && date.isBefore(start)) {
            return false;
        }
        if (end != null && date.isAfter(end)) {
            return false;
        }
        return true;
    }

    private void ensureUnique(User user, HealthLogType type, LocalDate date, Long currentId) {
        // 允许同一天同类型多条记录（前端支持一天多次记录同类事件）
        // 如需限制，可在此根据内容时间等字段做更细粒度判重
        return;
    }

    private User loadUser(Long userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseGet(this::resolveDefaultUser);
        }
        return resolveDefaultUser();
    }

    private User resolveDefaultUser() {
        return userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
    }

    @Override
    public List<HealthLogResponse> getAbnormalLogs(Long userId) {
        List<HealthLog> abnormalLogs = healthLogRepository.findByUser_IdAndIsAbnormalTrueOrderByLogDateDesc(userId);
        return abnormalLogs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private HealthLogResponse toResponse(HealthLog healthLog) {
        Map<String, Object> content = fromJson(healthLog.getContentJson());
        Map<String, Object> metadata = null;
        if (healthLog.getMetadataJson() != null && !healthLog.getMetadataJson().isEmpty()) {
            try {
                metadata = objectMapper.readValue(healthLog.getMetadataJson(), MAP_TYPE);
            } catch (Exception e) {
                log.warn("解析metadata失败: {}", e.getMessage());
            }
        }
        
        // 将metadata合并到content中，方便前端使用
        if (metadata != null) {
            content.put("_metadata", metadata);
        }
        if (healthLog.getIsAbnormal() != null) {
            content.put("_isAbnormal", healthLog.getIsAbnormal());
        }
        if (healthLog.getDataSource() != null) {
            content.put("_dataSource", healthLog.getDataSource().name());
        }
        
        return new HealthLogResponse(
                healthLog.getId(),
                healthLog.getLogDate(),
                healthLog.getType(),
                content,
                healthLog.getScore(),
                healthLog.getCreatedAt()
        );
    }

    private String toJson(Map<String, Object> content) {
        if (CollectionUtils.isEmpty(content)) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(content);
        } catch (IOException ex) {
            throw new BusinessException(50011, "日志内容序列化失败", ex);
        }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (IOException ex) {
            throw new BusinessException(50012, "日志内容解析失败", ex);
        }
    }

    @Override
    public java.util.Map<String, Object> getAdminHealthLogList(Long userId, String logType, String keyword, int page, int size, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        // 分页查询健康日志列表
        // 这里需要根据实际的分页需求实现，这里简化为返回所有日志
        List<HealthLog> logs = healthLogRepository.findAll();
        
        // 应用筛选条件
        if (userId != null) {
            logs = logs.stream()
                .filter(log -> log.getUser().getId().equals(userId))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (logType != null && !logType.isEmpty()) {
            try {
                HealthLogType type = HealthLogType.valueOf(logType.toUpperCase());
                logs = logs.stream()
                    .filter(log -> log.getType() == type)
                    .collect(java.util.stream.Collectors.toList());
            } catch (IllegalArgumentException e) {
                // 忽略无效的日志类型
            }
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            logs = logs.stream()
                .filter(log -> {
                    String content = log.getContentJson();
                    return content != null && content.contains(keyword);
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (startTime != null || endTime != null) {
            logs = logs.stream()
                .filter(log -> {
                    java.time.LocalDateTime logCreatedAt = log.getCreatedAt();
                    if (logCreatedAt == null) {
                        logCreatedAt = java.time.LocalDateTime.of(log.getLogDate(), java.time.LocalTime.MIDNIGHT);
                    }
                    
                    boolean afterStart = startTime == null || !logCreatedAt.isBefore(startTime);
                    boolean beforeEnd = endTime == null || !logCreatedAt.isAfter(endTime);
                    
                    return afterStart && beforeEnd;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 计算分页数据
        int total = logs.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        if (start >= total) {
            logs = java.util.Collections.emptyList();
        } else {
            logs = logs.subList(start, end);
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));
        
        return result;
    }

    @Override
    public HealthLog findById(Long id) {
        return healthLogRepository.findById(id).orElse(null);
    }

    @Override
    public HealthLog create(HealthLog healthLog) {
        return healthLogRepository.save(healthLog);
    }

    @Override
    public HealthLog update(Long id, HealthLog healthLog) {
        HealthLog existingLog = healthLogRepository.findById(id).orElse(null);
        if (existingLog == null) {
            return null;
        }
        
        // 更新日志信息
        existingLog.setLogDate(healthLog.getLogDate());
        existingLog.setType(healthLog.getType());
        existingLog.setContentJson(healthLog.getContentJson());
        existingLog.setScore(healthLog.getScore());
        existingLog.setIsAbnormal(healthLog.getIsAbnormal());
        existingLog.setMetadataJson(healthLog.getMetadataJson());
        
        return healthLogRepository.save(existingLog);
    }

    @Override
    public boolean deleteById(Long id) {
        if (healthLogRepository.existsById(id)) {
            healthLogRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总日志数
        long totalLogs = healthLogRepository.count();
        stats.put("totalLogs", totalLogs);
        
        // 活跃用户数（有健康日志的不同用户数）
        List<HealthLog> allLogs = healthLogRepository.findAll();
        long activeUsers = allLogs.stream()
                .map(HealthLog::getUser)
                .map(User::getId)
                .distinct()
                .count();
        stats.put("activeUsers", activeUsers);
        
        // 活跃家庭数（简化实现：统计有日志的用户所属的家庭数）
        // 注意：这里需要 FamilyMemberRepository 才能准确统计，暂时设为0
        // 后续可以注入 FamilyMemberRepository 来完善
        stats.put("activeFamilies", 0L);
        
        // 今日新增日志数（使用 logDate 字段判断）
        LocalDate today = LocalDate.now();
        long todayNew = allLogs.stream()
                .filter(log -> log.getLogDate() != null && log.getLogDate().equals(today))
                .count();
        stats.put("todayNew", todayNew);
        
        return stats;
    }

    private static final class Aggregation {
        private long count = 0;
        private double totalValue = 0;
        private long valueCount = 0;
        private HealthLogType type;

        private void accumulate(double value, HealthLogType type) {
            this.type = type;
            count++;
            // 只有当 value > 0 时才计入统计（避免无效记录拉低平均值，或者 0 值的处理）
            // 对于饮食、运动、睡眠，0值求和无影响。对于情绪、体征，0值可能有问题（除非真的有0强度情绪）
            // 这里简单处理：总是累加
            totalValue += value;
            valueCount++;
        }

        private Double getResult() {
            if (valueCount == 0) return 0.0;
            
            // 饮食、运动、睡眠：求和
            if (type == HealthLogType.DIET || type == HealthLogType.SPORT || type == HealthLogType.SLEEP) {
                return Math.round(totalValue * 100d) / 100d;
            }
            
            // 情绪、体征：求平均
            double avg = totalValue / valueCount;
            return Math.round(avg * 100d) / 100d;
        }
    }
}

