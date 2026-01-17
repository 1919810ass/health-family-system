package com.healthfamily.web.controller;

import com.healthfamily.service.UserLoginLogService;
import com.healthfamily.web.dto.UserLoginLogDto;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class UserLoginLogController {
    
    private final UserLoginLogService userLoginLogService;
    
    @GetMapping("/login")
    public Result<Page<UserLoginLogDto>> getLoginLogs(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loginTime"));
        
        Page<UserLoginLogDto> logs;
        if (startTime != null && endTime != null) {
            logs = userLoginLogService.getLoginLogs(startTime, endTime, pageable);
        } else {
            logs = userLoginLogService.getAllLoginLogs(pageable);
        }
        
        return Result.success(logs);
    }
}