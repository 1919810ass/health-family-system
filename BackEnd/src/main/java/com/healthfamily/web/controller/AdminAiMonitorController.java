package com.healthfamily.web.controller;

import com.healthfamily.service.AiMonitorService;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai-monitor")
@RequiredArgsConstructor
public class AdminAiMonitorController {

    private final AiMonitorService aiMonitorService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.success(aiMonitorService.getDashboardStats());
    }
}
