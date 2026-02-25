package com.healthfamily.service.impl;

import com.healthfamily.domain.entity.UserLoginLog;
import com.healthfamily.domain.repository.UserLoginLogRepository;
import com.healthfamily.service.UserLoginLogService;
import com.healthfamily.web.dto.UserLoginLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
/**
 * 用户Login日志服务Impl实现类
 * <p>
 * 实现平台核心业务服务，负责业务编排、数据聚合及与 AI/规则引擎的协同。
 * </p>
 */
@RequiredArgsConstructor
public class UserLoginLogServiceImpl implements UserLoginLogService {
    
    private final UserLoginLogRepository userLoginLogRepository;
    
    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param userId 家庭成员唯一标识
     * @param username 业务参数
     * @param role 业务参数
     * @param ipAddress 业务参数
     * @param userAgent 业务参数
     * @param status 业务参数
     * @param loginType 业务参数
     * @return 业务返回结果
     */
    public UserLoginLogDto saveLoginLog(Long userId, String username, String role, String ipAddress, String userAgent, String status, String loginType) {
        UserLoginLog log = UserLoginLog.builder()
                .userId(userId)
                .username(username)
                .role(role)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .status(status)
                .loginType(loginType)
                .build();
        
        UserLoginLog savedLog = userLoginLogRepository.save(log);
        return convertToDto(savedLog);
    }
    
    @Override
    @Transactional(readOnly = true)
    /**
     * 获取
     * @param startTime 业务参数
     * @param endTime 业务参数
     * @param pageable 业务参数
     * @return 业务返回结果
     */
    public Page<UserLoginLogDto> getLoginLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        try {
            return userLoginLogRepository.findByLoginTimeBetweenOrderByLoginTimeDesc(startTime, endTime, pageable)
                    .map(this::convertToDto);
        } catch (Exception e) {
            // 记录错误日志，便于调试
            System.err.println("Error fetching login logs by time range: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    /**
     * 获取
     * @param pageable 业务参数
     * @return 业务返回结果
     */
    public Page<UserLoginLogDto> getAllLoginLogs(Pageable pageable) {
        try {
            return userLoginLogRepository.findAllOrderByLoginTimeDesc(pageable)
                    .map(this::convertToDto);
        } catch (Exception e) {
            // 记录错误日志，便于调试
            System.err.println("Error fetching all login logs: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常
        }
    }
    
    private UserLoginLogDto convertToDto(UserLoginLog log) {
        return UserLoginLogDto.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .role(log.getRole())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .loginTime(log.getLoginTime())
                .status(log.getStatus())
                .loginType(log.getLoginType())
                .createdAt(log.getCreatedAt())
                .build();
    }
}