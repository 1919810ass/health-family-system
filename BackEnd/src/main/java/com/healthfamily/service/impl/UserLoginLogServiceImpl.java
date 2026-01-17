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
@RequiredArgsConstructor
public class UserLoginLogServiceImpl implements UserLoginLogService {
    
    private final UserLoginLogRepository userLoginLogRepository;
    
    @Override
    @Transactional
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