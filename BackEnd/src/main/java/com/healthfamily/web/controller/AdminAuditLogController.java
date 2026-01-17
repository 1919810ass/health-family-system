package com.healthfamily.web.controller;

import com.healthfamily.domain.constant.AuditResult;
import com.healthfamily.domain.constant.SensitivityLevel;
import com.healthfamily.domain.entity.AuditLog;
import com.healthfamily.service.AuditLogService;
import com.healthfamily.web.dto.AuditLogDto;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/logs")
    public Result<Page<AuditLogDto>> getLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) AuditResult result,
            @RequestParam(required = false) SensitivityLevel sensitivity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<AuditLog> logPage = auditLogService.searchLogs(userId, action, resource, result, sensitivity, startTime, endTime, keyword, pageable);
        
        Page<AuditLogDto> dtoPage = logPage.map(log -> new AuditLogDto(
                log.getId(),
                log.getUser() != null ? log.getUser().getId() : null,
                log.getUser() != null ? (log.getUser().getNickname() != null ? log.getUser().getNickname() : log.getUser().getPhone()) : "Unknown",
                log.getUser() != null ? log.getUser().getRole().name() : null,
                log.getAction(),
                log.getResource(),
                log.getSensitivityLevel(),
                log.getResult(),
                log.getIp(),
                log.getUserAgent(),
                log.getExtraJson(),
                log.getCreatedAt()
        ));

        return Result.success(dtoPage);
    }
}
