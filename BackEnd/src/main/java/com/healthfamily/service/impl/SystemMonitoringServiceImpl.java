package com.healthfamily.service.impl;

import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.DoctorProfileRepository;
import com.healthfamily.domain.repository.AlertRepository;
import com.healthfamily.domain.entity.Alert;
import com.healthfamily.domain.constant.AlertLevel;
import com.healthfamily.domain.constant.AlertStatus;
import com.healthfamily.service.SystemMonitoringService;
import com.healthfamily.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemMonitoringServiceImpl implements SystemMonitoringService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final HealthLogRepository healthLogRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AlertRepository alertRepository;

    @Override
    public UserActivityStatsDto getUserActivityStats(LocalDateTime startTime, LocalDateTime endTime) {
        // 计算活跃用户数
        Long dailyActiveUsers = userRepository.count();
        Long weeklyActiveUsers = userRepository.count();
        Long monthlyActiveUsers = userRepository.count();
        Long onlineUsers = 234L; // 模拟在线用户数

        // 在线用户列表
        List<Map<String, Object>> onlineUsersList = Arrays.asList(
            createUserMap(1001L, "张三", "MEMBER", "2024-01-15 14:30:22", "192.168.1.100", "Chrome/Windows", "北京"),
            createUserMap(1002L, "李四", "FAMILY_ADMIN", "2024-01-15 14:28:15", "192.168.1.101", "Safari/iOS", "上海"),
            createUserMap(1003L, "王五", "DOCTOR", "2024-01-15 14:25:43", "192.168.1.102", "Firefox/Linux", "广州"),
            createUserMap(1004L, "赵六", "MEMBER", "2024-01-15 14:22:18", "192.168.1.103", "Edge/Windows", "深圳"),
            createUserMap(1005L, "钱七", "ADMIN", "2024-01-15 14:20:33", "192.168.1.104", "Chrome/Mac", "杭州")
        );

        // 登录频次数据
        List<Map<String, Object>> loginFrequency = Arrays.asList(
            createLoginFreqMap("周一", 1200),
            createLoginFreqMap("周二", 1320),
            createLoginFreqMap("周三", 1010),
            createLoginFreqMap("周四", 1340),
            createLoginFreqMap("周五", 900),
            createLoginFreqMap("周六", 2300),
            createLoginFreqMap("周日", 2100)
        );

        // 功能使用数据
        List<Map<String, Object>> featureUsage = Arrays.asList(
            createFeatureUsageMap("健康日志", 3200),
            createFeatureUsageMap("家庭管理", 2800),
            createFeatureUsageMap("医生协作", 2100),
            createFeatureUsageMap("AI建议", 1800),
            createFeatureUsageMap("健康提醒", 1500),
            createFeatureUsageMap("体质测评", 1200)
        );

        // 登录日志数据
        List<Map<String, Object>> loginLogs = Arrays.asList(
            createLoginLogMap(1001L, "张三", "2024-01-15 14:30:22", "192.168.1.100", "Mozilla/5.0...", "success", "北京"),
            createLoginLogMap(1002L, "李四", "2024-01-15 14:28:15", "192.168.1.101", "Mozilla/5.0...", "success", "上海"),
            createLoginLogMap(1003L, "王五", "2024-01-15 14:25:43", "192.168.1.102", "Mozilla/5.0...", "failed", "广州"),
            createLoginLogMap(1004L, "赵六", "2024-01-15 14:22:18", "192.168.1.103", "Mozilla/5.0...", "success", "深圳"),
            createLoginLogMap(1005L, "钱七", "2024-01-15 14:20:33", "192.168.1.104", "Mozilla/5.0...", "success", "杭州")
        );

        return UserActivityStatsDto.builder()
                .dailyActiveUsers(dailyActiveUsers)
                .weeklyActiveUsers(weeklyActiveUsers)
                .monthlyActiveUsers(monthlyActiveUsers)
                .onlineUsers(onlineUsers)
                .onlineUsersList(onlineUsersList)
                .loginFrequency(loginFrequency)
                .featureUsage(featureUsage)
                .loginLogs(loginLogs)
                .totalLoginLogs(100)
                .build();
    }

    @Override
    public List<Map<String, Object>> getOnlineUsers() {
        return Arrays.asList(
            createUserMap(1001L, "张三", "MEMBER", "2024-01-15 14:30:22", "192.168.1.100", "Chrome/Windows", "北京"),
            createUserMap(1002L, "李四", "FAMILY_ADMIN", "2024-01-15 14:28:15", "192.168.1.101", "Safari/iOS", "上海"),
            createUserMap(1003L, "王五", "DOCTOR", "2024-01-15 14:25:43", "192.168.1.102", "Firefox/Linux", "广州")
        );
    }

    @Override
    public List<Map<String, Object>> getBehaviorAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
        return Arrays.asList(
            createFeatureUsageMap("健康日志", 3200),
            createFeatureUsageMap("家庭管理", 2800),
            createFeatureUsageMap("医生协作", 2100),
            createFeatureUsageMap("AI建议", 1800),
            createFeatureUsageMap("健康提醒", 1500)
        );
    }

    @Override
    public Map<String, Object> getLoginLogs(int page, int size, String type, LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> logs = Arrays.asList(
            createLoginLogMap(1001L, "张三", "2024-01-15 14:30:22", "192.168.1.100", "Mozilla/5.0...", "success", "北京"),
            createLoginLogMap(1002L, "李四", "2024-01-15 14:28:15", "192.168.1.101", "Mozilla/5.0...", "success", "上海"),
            createLoginLogMap(1003L, "王五", "2024-01-15 14:25:43", "192.168.1.102", "Mozilla/5.0...", "failed", "广州")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("items", logs);
        result.put("total", 100);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    public DataReportDto getDataReports(LocalDateTime startTime, LocalDateTime endTime) {
        // 统计数据
        Long totalUsers = userRepository.count();
        Long totalFamilies = familyRepository.count();
        Long totalHealthLogs = healthLogRepository.count();
        Long totalDoctors = doctorProfileRepository.count();

        // 新用户趋势
        List<Map<String, Object>> newUserTrend = Arrays.asList(
            createTrendMap("1号", 120),
            createTrendMap("5号", 132),
            createTrendMap("10号", 101),
            createTrendMap("15号", 134),
            createTrendMap("20号", 90),
            createTrendMap("25号", 230),
            createTrendMap("30号", 210)
        );

        // 功能使用统计
        List<Map<String, Object>> featureUsageStats = Arrays.asList(
            createFeatureUsageMap("健康日志", 3200),
            createFeatureUsageMap("家庭管理", 2800),
            createFeatureUsageMap("医生协作", 2100),
            createFeatureUsageMap("AI建议", 1800),
            createFeatureUsageMap("健康提醒", 1500),
            createFeatureUsageMap("体质测评", 1200)
        );

        // 健康记录趋势
        List<Map<String, Object>> healthLogTrend = Arrays.asList(
            createTrendMap("1号", 2400),
            createTrendMap("5号", 1398),
            createTrendMap("10号", 9800),
            createTrendMap("15号", 3908),
            createTrendMap("20号", 4800),
            createTrendMap("25号", 3800),
            createTrendMap("30号", 4300)
        );

        // 家庭增长趋势
        List<Map<String, Object>> familyGrowthTrend = Arrays.asList(
            createTrendMap("1号", 24),
            createTrendMap("5号", 13),
            createTrendMap("10号", 98),
            createTrendMap("15号", 39),
            createTrendMap("20号", 48),
            createTrendMap("25号", 38),
            createTrendMap("30号", 43)
        );

        // 质量报告
        List<QualityReportItemDto> qualityReport = Arrays.asList(
            QualityReportItemDto.builder().metric("数据完整性").description("数据字段完整度").value("98.5%").status("良好").build(),
            QualityReportItemDto.builder().metric("数据准确性").description("数据准确性验证").value("96.2%").status("良好").build(),
            QualityReportItemDto.builder().metric("数据一致性").description("跨表数据一致性").value("99.1%").status("优秀").build(),
            QualityReportItemDto.builder().metric("数据及时性").description("数据更新及时性").value("94.7%").status("一般").build(),
            QualityReportItemDto.builder().metric("数据可用性").description("数据可用性检查").value("97.8%").status("良好").build(),
            QualityReportItemDto.builder().metric("数据重复率").description("重复数据比例").value("1.2%").status("优秀").build()
        );

        return DataReportDto.builder()
                .totalUsers(totalUsers)
                .totalFamilies(totalFamilies)
                .totalHealthLogs(totalHealthLogs)
                .totalDoctors(totalDoctors)
                .newUserTrend(newUserTrend)
                .featureUsageStats(featureUsageStats)
                .healthLogTrend(healthLogTrend)
                .familyGrowthTrend(familyGrowthTrend)
                .qualityReport(qualityReport)
                .build();
    }

    @Override
    public List<QualityReportItemDto> getQualityReport() {
        return Arrays.asList(
            QualityReportItemDto.builder().metric("数据完整性").description("数据字段完整度").value("98.5%").status("良好").build(),
            QualityReportItemDto.builder().metric("数据准确性").description("数据准确性验证").value("96.2%").status("良好").build(),
            QualityReportItemDto.builder().metric("数据一致性").description("跨表数据一致性").value("99.1%").status("优秀").build(),
            QualityReportItemDto.builder().metric("数据及时性").description("数据更新及时性").value("94.7%").status("一般").build(),
            QualityReportItemDto.builder().metric("数据可用性").description("数据可用性检查").value("97.8%").status("良好").build(),
            QualityReportItemDto.builder().metric("数据重复率").description("重复数据比例").value("1.2%").status("优秀").build()
        );
    }

    @Override
    public CustomReportDto generateCustomReport(Map<String, Object> config) {
        List<Map<String, Object>> reportData = Arrays.asList(
            createReportItem("新增用户数", 1248L, "+12.5%"),
            createReportItem("活跃用户数", 8934L, "+8.7%"),
            createReportItem("健康日志数", 25670L, "+15.2%"),
            createReportItem("家庭数量", 3245L, "+5.8%"),
            createReportItem("医生数量", 127L, "+3.2%"),
            createReportItem("提醒发送数", 18900L, "+22.1%")
        );

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", Arrays.asList("1号", "5号", "10号", "15号", "20号", "25号", "30号"));
        chartData.put("data", Arrays.asList(120, 132, 101, 134, 90, 230, 210));

        String chartType = (String) config.getOrDefault("chartType", "line");

        return CustomReportDto.builder()
                .reportName((String) config.getOrDefault("name", "自定义报告"))
                .reportData(reportData)
                .chartData(chartData)
                .chartType(chartType)
                .exportUrl("/api/admin/monitoring/custom-reports/export")
                .build();
    }

    @Override
    public void saveReportTemplate(String templateName, Map<String, Object> config) {
        // 模拟保存模板
        System.out.println("保存报告模板: " + templateName + ", 配置: " + config);
    }

    @Override
    public List<Map<String, Object>> getSavedTemplates() {
        return Arrays.asList(
            createTemplateMap("用户增长分析", "2024-01-15 10:30:22"),
            createTemplateMap("健康趋势报告", "2024-01-14 15:45:18"),
            createTemplateMap("系统使用统计", "2024-01-13 09:20:33")
        );
    }

    @Override
    public Map<String, Object> getTemplate(String templateId) {
        Map<String, Object> template = new HashMap<>();
        template.put("id", templateId);
        template.put("name", "示例模板");
        template.put("config", Map.of(
            "name", "示例模板",
            "timeRange", Arrays.asList("2024-01-01", "2024-01-31"),
            "dimensions", Arrays.asList("users", "healthLogs"),
            "chartType", "line"
        ));
        return template;
    }

    @Override
    public void exportReport(Map<String, Object> params, jakarta.servlet.http.HttpServletResponse response) {
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=report.pdf");
            // 实际的报告生成和导出逻辑
            response.getOutputStream().write("Sample report content".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    @Transactional
    public void checkSystemHealth() {
        // 模拟系统健康检查逻辑
        // 1. 检查内存使用率
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory;

        if (memoryUsage > 0.9) {
            Alert alert = new Alert();
            alert.setType("SYSTEM");
            alert.setLevel(AlertLevel.CRITICAL);
            alert.setMessage("系统内存使用率过高: " + String.format("%.2f%%", memoryUsage * 100));
            alert.setStatus(AlertStatus.UNREAD);
            alert.setCreatedAt(LocalDateTime.now());
            alert.setPayloadJson("{}");
            alertRepository.save(alert);
        }

        // 2. 检查最近一小时的错误日志（模拟）
        boolean hasErrorSpike = new Random().nextInt(100) < 5; // 5%概率触发
        if (hasErrorSpike) {
            Alert alert = new Alert();
            alert.setType("APPLICATION");
            alert.setLevel(AlertLevel.WARNING);
            alert.setMessage("检测到异常错误日志激增");
            alert.setStatus(AlertStatus.UNREAD);
            alert.setCreatedAt(LocalDateTime.now());
            alert.setPayloadJson("{}");
            alertRepository.save(alert);
        }
    }

    @Override
    public Map<String, Object> getSystemAlerts(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        List<Alert> alerts = alertRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startTime, endTime);
        
        // 统计不同级别的告警数量
        long criticalCount = alerts.stream().filter(a -> AlertLevel.CRITICAL.equals(a.getLevel())).count();
        long warningCount = alerts.stream().filter(a -> AlertLevel.WARNING.equals(a.getLevel()) || AlertLevel.WARN.equals(a.getLevel())).count();
        long infoCount = alerts.stream().filter(a -> AlertLevel.INFO.equals(a.getLevel())).count();

        // 限制返回数量
        List<Map<String, Object>> alertList = alerts.stream()
                .limit(limit)
                .map(this::convertAlertToMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("alerts", alertList);
        result.put("stats", Map.of(
            "critical", criticalCount,
            "warning", warningCount,
            "info", infoCount,
            "total", alerts.size()
        ));
        
        return result;
    }

    @Override
    public void acknowledgeAlert(Long id) {
        Alert alert = alertRepository.findById(id).orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alertRepository.save(alert);
    }

    @Override
    public void resolveAlert(Long id) {
        Alert alert = alertRepository.findById(id).orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setStatus(AlertStatus.RESOLVED);
        alertRepository.save(alert);
    }

    private Map<String, Object> convertAlertToMap(Alert alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", alert.getId());
        map.put("type", alert.getType());
        map.put("level", alert.getLevel());
        map.put("message", alert.getMessage());
        map.put("status", alert.getStatus());
        map.put("createdAt", alert.getCreatedAt());
        return map;
    }

    // 辅助方法
    private Map<String, Object> createUserMap(Long userId, String username, String role, String lastActive, 
                                             String ipAddress, String device, String location) {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("username", username);
        user.put("role", role);
        user.put("lastActive", lastActive);
        user.put("ipAddress", ipAddress);
        user.put("device", device);
        user.put("location", location);
        return user;
    }

    private Map<String, Object> createLoginFreqMap(String day, Integer count) {
        Map<String, Object> item = new HashMap<>();
        item.put("day", day);
        item.put("count", count);
        return item;
    }

    private Map<String, Object> createFeatureUsageMap(String feature, Integer count) {
        Map<String, Object> item = new HashMap<>();
        item.put("feature", feature);
        item.put("count", count);
        return item;
    }

    private Map<String, Object> createLoginLogMap(Long userId, String username, String loginTime, 
                                                  String ipAddress, String userAgent, String status, String location) {
        Map<String, Object> log = new HashMap<>();
        log.put("userId", userId);
        log.put("username", username);
        log.put("loginTime", loginTime);
        log.put("ipAddress", ipAddress);
        log.put("userAgent", userAgent);
        log.put("status", status);
        log.put("location", location);
        return log;
    }

    private Map<String, Object> createTrendMap(String date, Integer value) {
        Map<String, Object> item = new HashMap<>();
        item.put("date", date);
        item.put("value", value);
        return item;
    }

    private Map<String, Object> createReportItem(String name, Long value, String trend) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        item.put("trend", trend);
        return item;
    }

    private Map<String, Object> createTemplateMap(String name, String updatedAt) {
        Map<String, Object> template = new HashMap<>();
        template.put("name", name);
        template.put("updatedAt", updatedAt);
        return template;
    }
}