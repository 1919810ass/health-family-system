package com.healthfamily.service;

import com.healthfamily.web.dto.UserLoginLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户Login日志服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.time.LocalDateTime;

public interface UserLoginLogService {
    UserLoginLogDto saveLoginLog(Long userId, String username, String role, String ipAddress, String userAgent, String status, String loginType);
    
    Page<UserLoginLogDto> getLoginLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<UserLoginLogDto> getAllLoginLogs(Pageable pageable);
}