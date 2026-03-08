package com.healthfamily.web.controller;

import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.service.AdminHealthLogService;
import com.healthfamily.web.dto.HealthLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/health/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminHealthLogController {

    private final AdminHealthLogService adminHealthLogService;

    @GetMapping
    public ResponseEntity<Page<HealthLogDto>> searchHealthLogs(
            @RequestParam(required = false) String userKeyword,
            @RequestParam(required = false) HealthLogType logType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String contentKeyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<HealthLogDto> dtoPage = adminHealthLogService.searchLogs(
                userKeyword, logType, startDate, endDate, contentKeyword, pageable
        ).map(HealthLogDto::fromEntity);

        return ResponseEntity.ok(dtoPage);
    }
}
