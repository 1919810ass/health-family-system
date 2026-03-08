package com.healthfamily.service;

import com.healthfamily.domain.constant.ReminderStatus;
import com.healthfamily.domain.constant.ReminderType;
import com.healthfamily.domain.entity.HealthReminder;
import com.healthfamily.domain.repository.HealthReminderRepository;
import com.healthfamily.web.model.response.AiSuggestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基于真实健康提醒数据的 AI 运营建议服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiSuggestionService {

    private final HealthReminderRepository reminderRepository;

    /**
     * 生成 AI 建议列表（热门提醒 / 高参与度 / 潜力趋势）
     */
    public List<AiSuggestionResponse> generateSuggestions() {
        List<AiSuggestionResponse> suggestions = new ArrayList<>();

        try {
            suggestions.add(createPopularRemindersSuggestion());
        } catch (Exception e) {
            log.warn("生成热门提醒分析失败: {}", e.getMessage());
        }

        try {
            suggestions.add(createHighEngagementSuggestion());
        } catch (Exception e) {
            log.warn("生成高参与度提醒分析失败: {}", e.getMessage());
        }

        try {
            suggestions.add(createTrendingRemindersSuggestion());
        } catch (Exception e) {
            log.warn("生成潜力趋势提醒分析失败: {}", e.getMessage());
        }

        return suggestions;
    }

    /**
     * 热门提醒分析：统计最近 30 天创建次数最多的提醒（按类型 + 标题聚合）
     */
    private AiSuggestionResponse createPopularRemindersSuggestion() {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.minusDays(30).atStartOfDay();
        LocalDateTime endTime = LocalDateTime.of(today, LocalTime.MAX);

        List<HealthReminder> all = reminderRepository.findAll();
        List<HealthReminder> range = all.stream()
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> !r.getCreatedAt().isBefore(startTime) && !r.getCreatedAt().isAfter(endTime))
                .toList();

        Map<String, List<HealthReminder>> grouped = range.stream()
                .collect(Collectors.groupingBy(this::buildGroupKey));

        record PopularStat(String key, String title, ReminderType type, long total, long userCount) {}

        List<PopularStat> stats = grouped.entrySet().stream()
                .map(e -> {
                    List<HealthReminder> list = e.getValue();
                    String title = list.get(0).getTitle();
                    ReminderType type = list.get(0).getType();
                    long total = list.size();
                    long userCount = list.stream()
                            .map(r -> r.getUser() != null ? r.getUser().getId() : null)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();
                    return new PopularStat(e.getKey(), title, type, total, userCount);
                })
                .sorted(Comparator.comparingLong((PopularStat s) -> s.total).reversed())
                .limit(5)
                .toList();

        AiSuggestionResponse popular = new AiSuggestionResponse();
        popular.setSuggestionType("POPULAR_REMINDERS");
        popular.setTitle("热门提醒分析");
        popular.setDescription("基于最近30天的实际创建次数，识别用户最常用的健康提醒。");

        List<AiSuggestionResponse.SuggestionItem> items = new ArrayList<>();
        int rank = 1;
        for (PopularStat s : stats) {
            String value = String.format("创建次数: %d 次，触达用户: %d 人", s.total, s.userCount);
            String remark = String.format("Top %d · 类型: %s", rank++, mapTypeToDisplay(s.type));
            items.add(new AiSuggestionResponse.SuggestionItem(s.title, value, remark));
        }
        popular.setItems(items);

        return popular;
    }

    /**
     * 高参与度提醒：统计完成率最高的提醒（仅统计创建数大于一定阈值的项）
     */
    private AiSuggestionResponse createHighEngagementSuggestion() {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.minusDays(60).atStartOfDay();
        LocalDateTime endTime = LocalDateTime.of(today, LocalTime.MAX);

        List<HealthReminder> all = reminderRepository.findAll();
        List<HealthReminder> range = all.stream()
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> !r.getCreatedAt().isBefore(startTime) && !r.getCreatedAt().isAfter(endTime))
                .toList();

        // 只统计与用户自我行为直接相关的提醒类型
        List<ReminderType> trackedTypes = List.of(
                ReminderType.MEDICATION,
                ReminderType.MEASUREMENT,
                ReminderType.LIFESTYLE,
                ReminderType.ROUTINE
        );

        Map<String, List<HealthReminder>> grouped = range.stream()
                .filter(r -> trackedTypes.contains(r.getType()))
                .collect(Collectors.groupingBy(this::buildGroupKey));

        record Engagement(String title, ReminderType type, long total, long completed, double rate) {}

        List<Engagement> list = grouped.values().stream()
                .map(reminders -> {
                    String title = reminders.get(0).getTitle();
                    ReminderType type = reminders.get(0).getType();
                    long total = reminders.size();
                    long completed = reminders.stream()
                            .filter(r -> r.getStatus() == ReminderStatus.COMPLETED
                                    || r.getStatus() == ReminderStatus.ACKNOWLEDGED)
                            .count();
                    double rate = total == 0 ? 0.0 : (double) completed / total;
                    return new Engagement(title, type, total, completed, rate);
                })
                // 过滤掉样本量过小的提醒，避免统计噪音
                .filter(e -> e.total >= 20)
                .sorted(Comparator.comparingDouble((Engagement e) -> e.rate).reversed())
                .limit(5)
                .toList();

        AiSuggestionResponse engagement = new AiSuggestionResponse();
        engagement.setSuggestionType("HIGH_ENGAGEMENT");
        engagement.setTitle("高参与度提醒");
        engagement.setDescription("识别在最近60天内完成率最高的提醒任务，适合作为平台推荐模板。");

        List<AiSuggestionResponse.SuggestionItem> items = new ArrayList<>();
        for (Engagement e : list) {
            String value = String.format("完成率: %.0f%%（%d / %d）", e.rate * 100, e.completed, e.total);
            String remark = String.format("类型: %s · 样本量: %d", mapTypeToDisplay(e.type), e.total);
            items.add(new AiSuggestionResponse.SuggestionItem(e.title, value, remark));
        }
        engagement.setItems(items);

        return engagement;
    }

    /**
     * 潜力趋势提醒：对比最近 30 天与前一段 30 天的创建量，找到增长最快的提醒
     */
    private AiSuggestionResponse createTrendingRemindersSuggestion() {
        LocalDate today = LocalDate.now();

        LocalDateTime recentStart = today.minusDays(30).atStartOfDay();
        LocalDateTime recentEnd = LocalDateTime.of(today, LocalTime.MAX);

        LocalDateTime prevStart = today.minusDays(60).atStartOfDay();
        LocalDateTime prevEnd = recentStart.minusSeconds(1);

        List<HealthReminder> all = reminderRepository.findAll();

        List<HealthReminder> recent = all.stream()
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> !r.getCreatedAt().isBefore(recentStart) && !r.getCreatedAt().isAfter(recentEnd))
                .toList();

        List<HealthReminder> prev = all.stream()
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> !r.getCreatedAt().isBefore(prevStart) && !r.getCreatedAt().isAfter(prevEnd))
                .toList();

        Map<String, Long> recentCount = recent.stream()
                .collect(Collectors.groupingBy(this::buildGroupKey, Collectors.counting()));
        Map<String, Long> prevCount = prev.stream()
                .collect(Collectors.groupingBy(this::buildGroupKey, Collectors.counting()));

        record Trend(String key, String title, ReminderType type, long recentTotal, long prevTotal, double growth) {}

        // 构建 key -> 示例提醒 的映射，方便回填标题和类型
        Map<String, HealthReminder> sampleByKey = all.stream()
                .collect(Collectors.toMap(
                        this::buildGroupKey,
                        Function.identity(),
                        (a, b) -> a
                ));

        List<Trend> trends = recentCount.entrySet().stream()
                .map(e -> {
                    String key = e.getKey();
                    long recentTotal = e.getValue();
                    long prevTotalVal = prevCount.getOrDefault(key, 0L);
                    // 如果前一周期为 0，则视为“新增趋势”，增长率用于排序时固定为 1.0（100%）
                    double growth = prevTotalVal == 0L
                            ? 1.0
                            : (double) (recentTotal - prevTotalVal) / prevTotalVal;

                    HealthReminder sample = sampleByKey.get(key);
                    String title = sample != null ? sample.getTitle() : key;
                    ReminderType type = sample != null ? sample.getType() : ReminderType.ROUTINE;
                    return new Trend(key, title, type, recentTotal, prevTotalVal, growth);
                })
                // 过滤掉样本量太小的，避免偶发噪音
                .filter(t -> t.recentTotal >= 10)
                .sorted(Comparator.comparingDouble((Trend t) -> t.growth).reversed())
                .limit(5)
                .toList();

        AiSuggestionResponse trending = new AiSuggestionResponse();
        trending.setSuggestionType("TRENDING_REMINDERS");
        trending.setTitle("潜力趋势提醒");
        trending.setDescription("对比最近30天与之前30天的创建趋势，识别增长最快的提醒类型。");

        List<AiSuggestionResponse.SuggestionItem> items = new ArrayList<>();
        for (Trend t : trends) {
            String value = String.format("近30天: %d 次；前一周期: %d 次", t.recentTotal, t.prevTotal);
            String remark;
            if (t.prevTotal == 0L) {
                // 避免出现“增长率 +3000%”这类误导性文案，前一周期为 0 时标记为“新增趋势”
                remark = String.format("新增趋势 · 类型: %s", mapTypeToDisplay(t.type));
            } else {
                remark = String.format("增长率: %+d%% · 类型: %s",
                        (int) Math.round(t.growth * 100),
                        mapTypeToDisplay(t.type));
            }
            items.add(new AiSuggestionResponse.SuggestionItem(t.title, value, remark));
        }
        trending.setItems(items);

        return trending;
    }

    /**
     * 聚合 key：按「提醒类型 + 标题」进行聚合
     */
    private String buildGroupKey(HealthReminder reminder) {
        String type = reminder.getType() != null ? reminder.getType().name() : "UNKNOWN";
        String title = reminder.getTitle() != null ? reminder.getTitle().trim() : "未命名提醒";
        return type + "||" + title;
    }

    private String mapTypeToDisplay(ReminderType type) {
        if (type == null) {
            return "其他";
        }
        return switch (type) {
            case MEDICATION -> "用药提醒";
            case MEASUREMENT -> "测量提醒";
            case VACCINE -> "疫苗提醒";
            case LIFESTYLE -> "生活方式";
            case ABNORMAL -> "异常数据";
            case ROUTINE -> "常规提醒";
            case FOLLOW_UP -> "随访提醒";
        };
    }
}
