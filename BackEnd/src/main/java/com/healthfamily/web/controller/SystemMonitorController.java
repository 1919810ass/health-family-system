package com.healthfamily.web.controller;

import com.healthfamily.web.dto.Result;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class SystemMonitorController {

    private final ThreadPoolTaskExecutor reportExecutor;

    public SystemMonitorController(@Qualifier("reportExecutor") ThreadPoolTaskExecutor reportExecutor) {
        this.reportExecutor = reportExecutor;
    }

    @GetMapping("/metrics")
    public Result<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // CPU Load
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double cpuLoad = osBean.getCpuLoad() * 100; // 0.0 to 1.0 -> 0% to 100%
        if (Double.isNaN(cpuLoad) || cpuLoad < 0) cpuLoad = 0.0;
        
        // Memory Usage
        Runtime rt = Runtime.getRuntime();
        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory * 100;

        // Thread Count
        int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();
        
        // Report Executor Metrics
        int activeReportThreads = reportExecutor.getActiveCount();
        int reportQueueSize = reportExecutor.getThreadPoolExecutor().getQueue().size();

        metrics.put("cpuUsage", String.format("%.1f", cpuLoad));
        metrics.put("memoryUsage", String.format("%.1f", memoryUsage));
        metrics.put("activeThreads", threadCount);
        metrics.put("reportActiveThreads", activeReportThreads);
        metrics.put("reportQueueSize", reportQueueSize);
        metrics.put("processors", osBean.getAvailableProcessors());

        return Result.success(metrics);
    }
}
