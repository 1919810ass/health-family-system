package com.healthfamily.service;

import com.healthfamily.web.dto.UserLoginLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface UserLoginLogService {
    UserLoginLogDto saveLoginLog(Long userId, String username, String role, String ipAddress, String userAgent, String status, String loginType);
    
    Page<UserLoginLogDto> getLoginLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<UserLoginLogDto> getAllLoginLogs(Pageable pageable);
}