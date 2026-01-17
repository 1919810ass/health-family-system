package com.healthfamily.web.controller;

import com.healthfamily.annotation.Audit;
import com.healthfamily.domain.constant.SensitivityLevel;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.AuditLogService;
import com.healthfamily.service.SecurityService;
import com.healthfamily.web.dto.AuditLogDto;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/security")
public class SecurityController {

    private final SecurityService securityService;
    private final AuditLogService auditLogService;

    @GetMapping("/export")
    public Result<String> export(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUserId();
        return Result.success(securityService.exportUserDataBase64(userId));
    }

    @DeleteMapping("/data")
    public Result<Void> delete(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUserId();
        securityService.deleteUserData(userId);
        return Result.success();
    }

    @GetMapping("/privacy")
    public Result<Map<String, Object>> getPrivacySettings(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUserId();
        return Result.success(securityService.getPrivacySettings(userId));
    }

    @PutMapping("/privacy")
    @Audit(action = "UPDATE_PRIVACY", resource = "Security", sensitivity = SensitivityLevel.HIGH)
    public Result<Void> updatePrivacySettings(@AuthenticationPrincipal UserPrincipal principal,
                                              @RequestBody Map<String, Object> settings) {
        Long userId = principal.getUserId();
        securityService.updatePrivacySettings(userId, settings);
        return Result.success();
    }

    @GetMapping("/activities")
    public Result<Page<AuditLogDto>> getMyActivities(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Long userId = principal.getUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        return Result.success(auditLogService.searchLogs(userId, null, null, null, null, null, null, null, pageable)
                .map(log -> new AuditLogDto(
                        log.getId(),
                        log.getUser().getId(),
                        log.getUser().getNickname() != null ? log.getUser().getNickname() : log.getUser().getPhone(),
                        log.getUser().getRole().name(),
                        log.getAction(),
                        log.getResource(),
                        log.getSensitivityLevel(),
                        log.getResult(),
                        log.getIp(),
                        log.getUserAgent(),
                        null, // 隐藏详细参数，保护隐私
                        log.getCreatedAt()
                )));
    }
}
