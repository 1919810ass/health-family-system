package com.healthfamily.service.impl;

import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.HealthAlert;
import com.healthfamily.domain.entity.Profile;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.HealthAlertRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.MonitoringService;
import com.healthfamily.web.dto.AlertResponse;
import com.healthfamily.web.dto.TelemetryIngestRequest;
import com.healthfamily.web.dto.ThresholdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final HealthAlertRepository healthAlertRepository;
    private final FamilyRepository familyRepository;

    @Override
    public AlertResponse ingest(Long requesterId, TelemetryIngestRequest request) {
        // 获取用户信息
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查用户是否有权限访问指定家庭
        if (request.getFamilyId() != null) {
            Family family = familyRepository.findById(request.getFamilyId())
                    .orElseThrow(() -> new RuntimeException("家庭不存在"));
            
            // 这里应该检查用户是否属于家庭或有权限访问
            // 简化处理：假设用户有权访问
        }

        // 解析遥测数据
        Map<String, Object> data = Map.of(
            request.getMetric(), request.getValue());
        
        // 检查是否超过阈值
        List<ThresholdResponse> thresholds = getThresholds(requesterId);
        List<String> violations = new ArrayList<>();
        
        for (ThresholdResponse threshold : thresholds) {
            String metric = threshold.getMetric();
            Object value = data.get(metric);
            
            if (value instanceof Number && threshold.getUpperBound() != null) {
                double numValue = ((Number) value).doubleValue();
                if (numValue > threshold.getUpperBound()) {
                    violations.add(metric + " 超过阈值: " + numValue + " > " + threshold.getUpperBound());
                }
            }
        }

        // 如果有违规，创建告警
        if (!violations.isEmpty()) {
            HealthAlert alert = HealthAlert.builder()
                    .user(user)
                    .family(familyRepository.findById(request.getFamilyId()).orElse(null))
                    .metric(violations.get(0).split(" ")[0])
                    .value(data.get(violations.get(0).split(" ")[0]) instanceof Number ? 
                          ((Number) data.get(violations.get(0).split(" ")[0])).doubleValue() : 0.0)
                    .threshold(thresholds.stream()
                          .filter(t -> t.getMetric().equals(violations.get(0).split(" ")[0]))
                          .findFirst()
                          .map(ThresholdResponse::getUpperBound)
                          .orElse(0.0))
                    .message(String.join(", ", violations))
                    .severity(com.healthfamily.domain.constant.AlertSeverity.HIGH)
                    .status(com.healthfamily.domain.constant.AlertStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            healthAlertRepository.save(alert);
            
            return AlertResponse.builder()
                    .id(alert.getId())
                    .userId(alert.getUser().getId())
                    .familyId(alert.getFamily() != null ? alert.getFamily().getId() : null)
                    .metric(alert.getMetric())
                    .value(alert.getValue())
                    .threshold(alert.getThreshold())
                    .message(alert.getMessage())
                    .severity(alert.getSeverity().name())
                    .status(alert.getStatus().name())
                    .createdAt(alert.getCreatedAt())
                    .build();
        }

        // 没有违规，返回正常响应
        return AlertResponse.builder()
                .id(0L)
                .userId(requesterId)
                .familyId(request.getFamilyId())
                .metric("NORMAL")
                .value(0.0)
                .threshold(0.0)
                .message("数据正常")
                .severity(com.healthfamily.domain.constant.AlertSeverity.LOW.name())
                .status(com.healthfamily.domain.constant.AlertStatus.CLOSED.name())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public List<AlertResponse> getAlerts(Long requesterId, Long familyId) {
        // 获取用户
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 根据权限获取告警
        List<HealthAlert> alerts;
        if (familyId != null) {
            // 获取家庭相关的告警
            alerts = healthAlertRepository.findByFamily_IdOrderByCreatedAtDesc(familyId);
        } else {
            // 获取用户相关的告警
            alerts = healthAlertRepository.findByUserOrderByCreatedAtDesc(user);
        }
        
        // 转换为响应对象
        return alerts.stream()
                .map(alert -> AlertResponse.builder()
                        .id(alert.getId())
                        .userId(alert.getUser().getId())
                        .familyId(alert.getFamily() != null ? alert.getFamily().getId() : null)
                        .metric(alert.getMetric())
                        .value(alert.getValue())
                        .threshold(alert.getThreshold())
                        .message(alert.getMessage())
                        .severity(alert.getSeverity().name())
                        .status(alert.getStatus().name())
                        .channel(alert.getChannel())
                        .createdAt(alert.getCreatedAt())
                        .handledAt(alert.getHandledAt())
                        .escalationLevel(alert.getEscalationLevel())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public AlertResponse acknowledge(Long requesterId, Long alertId) {
        // 获取告警
        HealthAlert alert = healthAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("告警不存在"));
        
        // 检查权限
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!alert.getUser().getId().equals(requesterId)) {
            throw new RuntimeException("无权限确认此告警");
        }
        
        // 更新告警状态
        alert.setStatus(com.healthfamily.domain.constant.AlertStatus.ACKED);
        alert.setHandledAt(LocalDateTime.now());
        healthAlertRepository.save(alert);
        
        // 返回更新后的告警
        return AlertResponse.builder()
                .id(alert.getId())
                .userId(alert.getUser().getId())
                .familyId(alert.getFamily() != null ? alert.getFamily().getId() : null)
                .metric(alert.getMetric())
                .value(alert.getValue())
                .threshold(alert.getThreshold())
                .message(alert.getMessage())
                .severity(alert.getSeverity().name())
                .status(alert.getStatus().name())
                .channel(alert.getChannel())
                .createdAt(alert.getCreatedAt())
                .handledAt(alert.getHandledAt())
                .escalationLevel(alert.getEscalationLevel())
                .build();
    }

    @Override
    public List<ThresholdResponse> getThresholds(Long requesterId) {
        // 获取用户配置的阈值
        Profile profile = profileRepository.findById(requesterId).orElse(null);
        
        if (profile != null && profile.getPreferences() != null) {
            // 解析配置的阈值
            // 这里简化处理，返回默认阈值
            return List.of(
                ThresholdResponse.builder().metric("HR").upperBound(100.0).lowerBound(50.0).build(),
                ThresholdResponse.builder().metric("BP_SYS").upperBound(140.0).lowerBound(90.0).build(),
                ThresholdResponse.builder().metric("BP_DIA").upperBound(90.0).lowerBound(60.0).build(),
                ThresholdResponse.builder().metric("TEMP").upperBound(37.5).lowerBound(36.0).build(),
                ThresholdResponse.builder().metric("SPO2").upperBound(95.0).lowerBound(90.0).build()
            );
        }
        
        // 默认阈值
        return List.of(
            ThresholdResponse.builder().metric("HR").upperBound(100.0).lowerBound(50.0).build(),
            ThresholdResponse.builder().metric("BP_SYS").upperBound(140.0).lowerBound(90.0).build(),
            ThresholdResponse.builder().metric("BP_DIA").upperBound(90.0).lowerBound(60.0).build(),
            ThresholdResponse.builder().metric("TEMP").upperBound(37.5).lowerBound(36.0).build(),
            ThresholdResponse.builder().metric("SPO2").upperBound(95.0).lowerBound(90.0).build()
        );
    }

    @Override
    public List<ThresholdResponse> optimizeThresholds(Long requesterId) {
        // 获取用户健康数据以优化阈值
        // 这里简化处理，返回默认优化后的阈值
        return List.of(
            ThresholdResponse.builder().metric("HR").upperBound(95.0).lowerBound(55.0).build(),
            ThresholdResponse.builder().metric("BP_SYS").upperBound(135.0).lowerBound(85.0).build(),
            ThresholdResponse.builder().metric("BP_DIA").upperBound(85.0).lowerBound(55.0).build(),
            ThresholdResponse.builder().metric("TEMP").upperBound(37.3).lowerBound(36.0).build(),
            ThresholdResponse.builder().metric("SPO2").upperBound(96.0).lowerBound(90.0).build()
        );
    }
}