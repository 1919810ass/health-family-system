package com.healthfamily.web.controller;

import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.HealthDataAiService;
import com.healthfamily.service.HealthLogService;
import com.healthfamily.web.dto.DeviceSyncRequest;
import com.healthfamily.web.dto.HealthLogRequest;
import com.healthfamily.web.dto.HealthLogResponse;
import com.healthfamily.web.dto.HealthLogStatisticsResponse;
import com.healthfamily.web.dto.OcrParseRequest;
import com.healthfamily.web.dto.DietOptimizeRequest;
import com.healthfamily.web.dto.OptimizeInputRequest;
import com.healthfamily.web.dto.Result;
import com.healthfamily.web.dto.VoiceInputRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs")
public class HealthLogController {

    private final HealthLogService healthLogService;
    private final HealthDataAiService healthDataAiService;

    private Long resolveUserId(UserPrincipal principal, Long userHeader) {
        if (principal == null) return userHeader;
        if (userHeader != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR") || a.getAuthority().equals("ROLE_ADMIN"))) {
            return userHeader;
        }
        return principal.getUserId();
    }

    @PostMapping
    public Result<HealthLogResponse> createLog(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                               @Valid @RequestBody HealthLogRequest request) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(healthLogService.createLog(userId, request));
    }

    @GetMapping
    public Result<List<HealthLogResponse>> listLogs(@AuthenticationPrincipal UserPrincipal principal,
                                                    @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                    @RequestParam(value = "type", required = false) HealthLogType type,
                                                    @RequestParam(value = "startDate", required = false)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                    @RequestParam(value = "endDate", required = false)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(healthLogService.listLogs(userId, type, startDate, endDate));
    }

    @PutMapping("/{id}")
    public Result<HealthLogResponse> updateLog(@AuthenticationPrincipal UserPrincipal principal,
                                               @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                               @PathVariable("id") Long logId,
                                               @Valid @RequestBody HealthLogRequest request) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(healthLogService.updateLog(userId, logId, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteLog(@AuthenticationPrincipal UserPrincipal principal,
                                  @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                  @PathVariable("id") Long logId) {
        Long userId = resolveUserId(principal, userHeader);
        healthLogService.deleteLog(userId, logId);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result<HealthLogStatisticsResponse> getStatistics(@AuthenticationPrincipal UserPrincipal principal,
                                                             @RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(healthLogService.getStatistics(userId));
    }

    @PostMapping("/voice")
    public Result<Map<String, Object>> parseVoiceInput(@AuthenticationPrincipal UserPrincipal principal,
                                                         @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                         @Valid @RequestBody VoiceInputRequest request) {
        Long userId = resolveUserId(principal, userHeader);
        Map<String, Object> result = healthDataAiService.parseVoiceInput(request.voiceText());
        return Result.success(result);
    }

    @PostMapping("/ocr")
    public Result<Map<String, Object>> parseOcrData(@AuthenticationPrincipal UserPrincipal principal,
                                                      @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                      @Valid @RequestBody OcrParseRequest request) {
        Long userId = resolveUserId(principal, userHeader);
        Map<String, Object> result = healthDataAiService.parseMedicalDataFromImage(request.imageBase64());
        return Result.success(result);
    }

    @PostMapping("/diet/optimize")
    public Result<Map<String, Object>> optimizeDiet(@AuthenticationPrincipal UserPrincipal principal,
                                                    @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                    @Valid @RequestBody DietOptimizeRequest request) {
        Long userId = resolveUserId(principal, userHeader);
        Map<String, Object> result = healthDataAiService.optimizeDietText(request.text());
        return Result.success(result);
    }

    @PostMapping("/optimize")
    public Result<Map<String, Object>> optimizeInput(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                     @Valid @RequestBody OptimizeInputRequest request) {
        Long userId = resolveUserId(principal, userHeader);
        Map<String, Object> result = healthDataAiService.optimizeInput(request.type(), request.text());
        return Result.success(result);
    }

    @PostMapping("/device/sync")
    public Result<HealthLogResponse> syncDeviceData(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                     @Valid @RequestBody DeviceSyncRequest request) {
        Long userId = resolveUserId(principal, userHeader);
        
        // 数据清洗
        Map<String, Object> cleanedData = healthDataAiService.cleanAndNormalize(
                request.data(), 
                request.deviceType()
        );
        
        // 创建健康日志
        HealthLogRequest logRequest = new HealthLogRequest(
                request.logDate(),
                HealthLogType.VITALS,
                cleanedData,
                null
        );
        
        // 在content中添加数据源信息
        cleanedData.put("_dataSource", "DEVICE");
        cleanedData.put("_deviceId", request.deviceId());
        cleanedData.put("_deviceType", request.deviceType());
        
        HealthLogRequest finalRequest = new HealthLogRequest(
                request.logDate(),
                HealthLogType.VITALS,
                cleanedData,
                null
        );
        
        HealthLogResponse response = healthLogService.createLog(userId, finalRequest);
        return Result.success(response);
    }

    @GetMapping("/abnormal")
    public Result<List<HealthLogResponse>> getAbnormalLogs(@AuthenticationPrincipal UserPrincipal principal,
                                                            @RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = resolveUserId(principal, userHeader);
        return Result.success(healthLogService.getAbnormalLogs(userId));
    }
}

