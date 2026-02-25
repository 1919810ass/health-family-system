package com.healthfamily.web.controller;

import com.healthfamily.web.dto.Result;
import com.healthfamily.domain.entity.HealthThreshold;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.HealthThresholdRepository;
import com.healthfamily.domain.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctor/thresholds")
/**
 * 健康阈值控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequiredArgsConstructor
public class HealthThresholdController {

    private final HealthThresholdRepository thresholdRepository;
    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 获取
     * @param userId 家庭成员唯一标识
     * @return 业务返回结果
     */
    public Result<List<HealthThreshold>> getThresholds(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return Result.success(thresholdRepository.findByUser(user));
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    /**
     * 执行业务操作
     * @param userId 家庭成员唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<HealthThreshold> saveThreshold(@PathVariable Long userId, @RequestBody ThresholdRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        HealthThreshold threshold = thresholdRepository.findByUserAndMetric(user, request.getMetric())
                .orElse(HealthThreshold.builder()
                        .user(user)
                        .metric(request.getMetric())
                        .build());
        
        threshold.setLowerBound(request.getLowerBound());
        threshold.setUpperBound(request.getUpperBound());
        threshold.setUpdatedAt(LocalDateTime.now());
        
        return Result.success(thresholdRepository.save(threshold));
    }

    @Data
    public static class ThresholdRequest {
        private String metric;
        private Double lowerBound;
        private Double upperBound;
    }
}
