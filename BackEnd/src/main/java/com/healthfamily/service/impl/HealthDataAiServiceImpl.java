package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.HealthThreshold;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.HealthThresholdRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.HealthDataAiService;
import com.healthfamily.ai.OllamaLegacyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthDataAiServiceImpl implements HealthDataAiService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    private final HealthLogRepository healthLogRepository;
    private final ProfileRepository profileRepository;
    private final HealthThresholdRepository thresholdRepository;
    private final UserRepository userRepository;
    private final OllamaLegacyClient ollamaLegacyClient;

    // 数据范围定义（正常值范围）
    private static final Map<String, Range> NORMAL_RANGES = Map.of(
            "血压_收缩压", new Range(90, 140),
            "血压_舒张压", new Range(60, 90),
            "血糖_空腹", new Range(3.9, 6.1),
            "血糖_餐后", new Range(4.4, 7.8),
            "体温", new Range(36.0, 37.3),
            "心率", new Range(60, 100),
            "体重", new Range(40, 150) // kg
    );

    @Override
    public Map<String, Object> cleanAndNormalize(Object rawData, String dataType) {
        Map<String, Object> result = new HashMap<>();
        
        if (rawData == null) {
            return result;
        }

        try {
            // 如果是字符串，尝试解析
            if (rawData instanceof String) {
                String text = ((String) rawData).trim();
                result = parseTextInput(text, dataType);
            } else if (rawData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) rawData;
                result = normalizeMapData(map, dataType);
            } else {
                result.put("value", rawData);
                result.put("unit", getDefaultUnit(dataType));
            }
        } catch (Exception e) {
            log.warn("数据清洗失败: {}", e.getMessage());
            result.put("raw", rawData);
            result.put("error", "数据格式异常");
        }

        return result;
    }

    private Map<String, Object> parseTextInput(String text, String dataType) {
        Map<String, Object> result = new HashMap<>();
        
        // 使用正则表达式提取数值
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(text);
        List<Double> values = new ArrayList<>();
        while (matcher.find()) {
            values.add(Double.parseDouble(matcher.group(1)));
        }

        if (values.isEmpty()) {
            result.put("raw", text);
            return result;
        }

        // 根据数据类型处理
        switch (dataType.toUpperCase()) {
            case "血压", "BLOOD_PRESSURE", "BP":
                if (values.size() >= 2) {
                    result.put("systolic", values.get(0)); // 收缩压
                    result.put("diastolic", values.get(1)); // 舒张压
                    result.put("unit", "mmHg");
                } else if (values.size() == 1) {
                    result.put("value", values.get(0));
                    result.put("unit", "mmHg");
                }
                break;
            case "血糖", "BLOOD_SUGAR", "GLUCOSE":
                result.put("value", values.get(0));
                // 判断单位（默认mmol/L，如果数值很大可能是mg/dL）
                if (values.get(0) > 20) {
                    result.put("value", values.get(0) / 18.0); // 转换为mmol/L
                    result.put("originalUnit", "mg/dL");
                }
                result.put("unit", "mmol/L");
                break;
            case "体温", "TEMPERATURE", "TEMP":
                result.put("value", values.get(0));
                // 判断单位（默认摄氏度，如果数值很大可能是华氏度）
                if (values.get(0) > 50) {
                    result.put("value", (values.get(0) - 32) * 5.0 / 9.0); // 转换为摄氏度
                    result.put("originalUnit", "°F");
                }
                result.put("unit", "°C");
                break;
            case "心率", "HEART_RATE", "HR":
                result.put("value", values.get(0));
                result.put("unit", "bpm");
                break;
            case "体重", "WEIGHT":
                result.put("value", values.get(0));
                // 判断单位（默认kg，如果数值很大可能是磅）
                if (values.get(0) > 100) {
                    result.put("value", values.get(0) * 0.453592); // 转换为kg
                    result.put("originalUnit", "lb");
                }
                result.put("unit", "kg");
                break;
            default:
                result.put("value", values.get(0));
                result.put("unit", getDefaultUnit(dataType));
        }

        result.put("sourceText", text);
        return result;
    }

    private Map<String, Object> normalizeMapData(Map<String, Object> map, String dataType) {
        Map<String, Object> result = new HashMap<>(map);
        
        // 统一单位转换
        if (map.containsKey("value")) {
            Object value = map.get("value");
            if (value instanceof Number) {
                double numValue = ((Number) value).doubleValue();
                String unit = (String) map.getOrDefault("unit", getDefaultUnit(dataType));
                
                // 单位转换
                if ("体重".equals(dataType) || "WEIGHT".equals(dataType)) {
                    if ("lb".equals(unit) || "磅".equals(unit)) {
                        result.put("value", numValue * 0.453592);
                        result.put("unit", "kg");
                        result.put("originalUnit", unit);
                    }
                } else if ("体温".equals(dataType) || "TEMPERATURE".equals(dataType)) {
                    if ("°F".equals(unit) || "F".equals(unit)) {
                        result.put("value", (numValue - 32) * 5.0 / 9.0);
                        result.put("unit", "°C");
                        result.put("originalUnit", unit);
                    }
                } else if ("血糖".equals(dataType) || "BLOOD_SUGAR".equals(dataType)) {
                    if ("mg/dL".equals(unit)) {
                        result.put("value", numValue / 18.0);
                        result.put("unit", "mmol/L");
                        result.put("originalUnit", unit);
                    }
                }
            }
        }
        
        return result;
    }

    private String getDefaultUnit(String dataType) {
        return switch (dataType.toUpperCase()) {
            case "血压", "BLOOD_PRESSURE", "BP" -> "mmHg";
            case "血糖", "BLOOD_SUGAR", "GLUCOSE" -> "mmol/L";
            case "体温", "TEMPERATURE", "TEMP" -> "°C";
            case "心率", "HEART_RATE", "HR" -> "bpm";
            case "体重", "WEIGHT" -> "kg";
            default -> "";
        };
    }

    @Override
    public AnomalyResult detectAnomaly(Long userId, String dataType, Double value, Map<String, Object> historicalData) {
        if (value == null) {
            return new AnomalyResult(false, "数值为空", "LOW", null);
        }

        // 获取历史数据用于对比
        List<HealthLog> recentLogs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(
                userId, 
                com.healthfamily.domain.constant.HealthLogType.VITALS
        ).stream()
                .limit(30) // 最近30条
                .collect(Collectors.toList());

        // 计算历史平均值和标准差
        List<Double> historicalValues = new ArrayList<>();
        for (HealthLog log : recentLogs) {
            try {
                Map<String, Object> content = objectMapper.readValue(
                        log.getContentJson(), 
                        new TypeReference<Map<String, Object>>() {}
                );
                Double logValue = extractValue(content, dataType);
                if (logValue != null) {
                    historicalValues.add(logValue);
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        // 检查是否超出正常范围
        String rangeKey = getRangeKey(dataType);
        Range normalRange = null;

        // 优先使用医生/用户自定义的阈值
        if (rangeKey != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                Optional<HealthThreshold> threshold = thresholdRepository.findByUserAndMetric(userOpt.get(), rangeKey);
                if (threshold.isPresent()) {
                    HealthThreshold t = threshold.get();
                    if (t.getLowerBound() != null && t.getUpperBound() != null) {
                        normalRange = new Range(t.getLowerBound(), t.getUpperBound());
                    }
                }
            }
        }

        if (normalRange == null && rangeKey != null) {
            normalRange = NORMAL_RANGES.get(rangeKey);
        }
        boolean outOfRange = false;
        String severity = "LOW";
        String reason = null;

        if (normalRange != null) {
            if (value < normalRange.min || value > normalRange.max) {
                outOfRange = true;
                if (value < normalRange.min * 0.8 || value > normalRange.max * 1.2) {
                    severity = "HIGH";
                } else {
                    severity = "MEDIUM";
                }
                reason = String.format("数值 %.2f 超出正常范围 [%.2f, %.2f]", 
                        value, normalRange.min, normalRange.max);
            }
        }

        // 检查是否异常波动（与历史数据对比）
        if (!historicalValues.isEmpty() && historicalValues.size() >= 3) {
            double avg = historicalValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double std = calculateStdDev(historicalValues, avg);
            
            // 如果当前值偏离平均值超过2个标准差，认为是异常
            if (Math.abs(value - avg) > 2 * std && std > 0) {
                if (!outOfRange) {
                    severity = "MEDIUM";
                }
                reason = String.format("数值 %.2f 与历史平均值 %.2f 差异较大（标准差: %.2f）", 
                        value, avg, std);
                outOfRange = true;
            }
        }

        String suggestion = null;
        if (outOfRange) {
            suggestion = generateSuggestion(dataType, value, normalRange);
        }

        return new AnomalyResult(outOfRange, reason, severity, suggestion);
    }

    private Double extractValue(Map<String, Object> content, String dataType) {
        if (content == null) return null;
        
        // 尝试多种可能的键名
        Object value = content.get("value");
        if (value == null) {
            value = content.get(dataType.toLowerCase());
        }
        if (value == null && "血压".equals(dataType)) {
            // 血压取平均值
            Object sys = content.get("systolic");
            Object dia = content.get("diastolic");
            if (sys instanceof Number && dia instanceof Number) {
                return (((Number) sys).doubleValue() + ((Number) dia).doubleValue()) / 2.0;
            }
        }
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private String getRangeKey(String dataType) {
        if (NORMAL_RANGES.containsKey(dataType)) {
            return dataType;
        }
        return switch (dataType.toUpperCase()) {
            case "血压", "BLOOD_PRESSURE", "BP" -> "血压_收缩压"; // 简化处理
            case "血糖", "BLOOD_SUGAR", "GLUCOSE" -> "血糖_空腹";
            case "体温", "TEMPERATURE", "TEMP" -> "体温";
            case "心率", "HEART_RATE", "HR" -> "心率";
            case "体重", "WEIGHT" -> "体重";
            default -> null;
        };
    }

    private double calculateStdDev(List<Double> values, double mean) {
        if (values.size() < 2) return 0;
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    private String generateSuggestion(String dataType, Double value, Range normalRange) {
        if (normalRange == null) return null;
        
        if (value < normalRange.min) {
            return String.format("当前值 %.2f 低于正常范围，建议关注并咨询医生", value);
        } else {
            return String.format("当前值 %.2f 高于正常范围，建议关注并咨询医生", value);
        }
    }

    @Override
    public Map<String, Object> parseMedicalDataFromImage(String imageBase64) {
        try {
            // 使用AI模型解析医疗数据
            String prompt = """
                    请分析这张医疗检查报告图片，提取以下关键信息：
                    1. 检查项目名称
                    2. 检查数值和单位
                    3. 检查日期
                    4. 正常值范围（如果有）
                    5. 异常标记（如果有）
                    
                    请以JSON格式返回，格式如下：
                    {
                      "items": [
                        {
                          "name": "项目名称",
                          "value": 数值,
                          "unit": "单位",
                          "normalRange": "正常范围",
                          "isAbnormal": true/false
                        }
                      ],
                      "checkDate": "检查日期",
                      "hospital": "医院名称（如果有）"
                    }
                    """;

            String content = callChatWithFallback(prompt);
            
            if (content == null || content.isEmpty()) {
                return Map.of("error", "AI未返回内容");
            }
            
            // 尝试提取JSON（AI可能返回markdown代码块）
            String jsonContent = extractJsonFromResponse(content);
            
            // 解析AI返回的JSON
            try {
                return objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("解析AI返回的JSON失败，尝试直接解析: {}", e.getMessage());
                // 如果解析失败，返回原始内容
                return Map.of("raw", content, "error", "JSON解析失败");
            }
        } catch (Exception e) {
            log.error("OCR解析失败: {}", e.getMessage());
            return Map.of("error", "解析失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> optimizeInput(String type, String text) {
        if (!StringUtils.hasText(text)) return Map.of();
        String t = type != null ? type.toUpperCase() : "";
        switch (t) {
            case "DIET":
                return optimizeDietText(text);
            case "SLEEP":
                return optimizeSleepText(text);
            case "SPORT":
                return optimizeSportText(text);
            case "MOOD":
                return optimizeMoodText(text);
            case "VITALS":
                return optimizeVitalsText(text);
            default:
                return Map.of("raw", text);
        }
    }

    private Map<String, Object> optimizeVitalsText(String text) {
        try {
            String prompt = String.format("""
                    将以下健康体征记录解析为结构化JSON列表：
                    "%s"
                    
                    支持识别：血压(BP)、血糖(GLUCOSE)、心率(HR)、体温(TEMP)、体重(WEIGHT)等。
                    请返回包含"items"数组的JSON，每个item格式如下：
                    {
                      "type": "血压/血糖/心率/体温/体重",
                      "value": 数值或字符串(如120/80),
                      "unit": "单位(如mmHg, mmol/L, bpm, °C, kg)"
                    }
                    如果是血压，value请保持"120/80"格式；其他数值保持数字。
                    """, text);
            String json = extractJsonFromResponse(callChatWithFallback(prompt));
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // 降级处理：尝试使用简单正则提取（仅支持单一数据，或尽力解析）
            Map<String, Object> fallback = parseTextInput(text, "AUTO");
            // 包装成 items 格式以保持一致性
            return Map.of("items", List.of(fallback), "raw", text);
        }
    }

    private Map<String, Object> optimizeSleepText(String text) {
        try {
            String prompt = String.format("""
                    将以下睡眠记录解析为结构化JSON：
                    "%s"
                    
                    返回：
                    {
                      "bedtime": "HH:mm",
                      "wakeTime": "HH:mm",
                      "durationHours": 数值,
                      "quality": "优/良/一般/差"
                    }
                    """, text);
            String json = extractJsonFromResponse(callChatWithFallback(prompt));
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> r = new HashMap<>();
            Matcher m1 = Pattern.compile("(\\d{1,2}:\\d{2}).*?(\\d{1,2}:\\d{2})").matcher(text);
            if (m1.find()) {
                r.put("bedtime", m1.group(1));
                r.put("wakeTime", m1.group(2));
            }
            Matcher m2 = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*小时").matcher(text);
            if (m2.find()) {
                r.put("durationHours", Double.parseDouble(m2.group(1)));
            }
            if (text.contains("优")) r.put("quality", "优");
            else if (text.contains("良")) r.put("quality", "良");
            else if (text.contains("一般")) r.put("quality", "一般");
            else if (text.contains("差")) r.put("quality", "差");
            return r;
        }
    }

    private Map<String, Object> optimizeSportText(String text) {
        try {
            String prompt = String.format("""
                    将以下运动记录解析为结构化JSON：
                    "%s"
                    
                    返回：
                    {
                      "type": "跑步/步行/游泳等",
                      "durationMinutes": 数值,
                      "intensity": "轻/中/重",
                      "distanceKm": 可选数值
                    }
                    """, text);
            String json = extractJsonFromResponse(callChatWithFallback(prompt));
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> r = new HashMap<>();
            String[] types = {"跑步","步行","游泳","骑行","健走","瑜伽","力量训练"};
            for (String tp : types) { if (text.contains(tp)) { r.put("type", tp); break; } }
            Matcher mDur = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*分钟").matcher(text);
            if (mDur.find()) r.put("durationMinutes", Double.parseDouble(mDur.group(1)));
            if (text.contains("轻")) r.put("intensity", "轻");
            else if (text.contains("中")) r.put("intensity", "中");
            else if (text.contains("重")) r.put("intensity", "重");
            Matcher mDist = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(公里|km)").matcher(text);
            if (mDist.find()) r.put("distanceKm", Double.parseDouble(mDist.group(1)));
            return r;
        }
    }

    private Map<String, Object> optimizeMoodText(String text) {
        try {
            String prompt = String.format("""
                    将以下心理/情绪记录解析为结构化JSON：
                    "%s"
                    
                    返回：
                    {
                      "emotion": "开心/焦虑/沮丧/愤怒等",
                      "level": 1-5,
                      "note": "触发事件或描述"
                    }
                    """, text);
            String json = extractJsonFromResponse(callChatWithFallback(prompt));
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> r = new HashMap<>();
            String[] emos = {"开心","焦虑","沮丧","愤怒","紧张","平静","疲惫"};
            for (String em : emos) { if (text.contains(em)) { r.put("emotion", em); break; } }
            Matcher lvl = Pattern.compile("(\\d+)").matcher(text);
            if (lvl.find()) {
                try { r.put("level", Integer.parseInt(lvl.group(1))); } catch (Exception ignore) {}
            }
            r.put("note", text);
            return r;
        }
    }

    @Override
    public Map<String, Object> parseVoiceInput(String voiceText) {
        if (!StringUtils.hasText(voiceText)) {
            return Map.of("error", "语音文本为空");
        }

        try {
            // 使用AI解析语音文本
            String prompt = String.format("""
                    请解析以下语音录入的健康数据文本，提取关键信息：
                    "%s"
                    
                    请识别：
                    1. 数据类型（血压、血糖、体温、心率、体重等）
                    2. 数值
                    3. 单位（如果有）
                    4. 时间（如果有，如"今天"、"昨天"等）
                    
                    返回JSON格式：
                    {
                      "type": "数据类型",
                      "value": 数值,
                      "unit": "单位",
                      "date": "日期（YYYY-MM-DD格式，如果未指定则为今天）",
                      "confidence": 0.9
                    }
                    """, voiceText);

            String content = callChatWithFallback(prompt);
            
            if (content == null || content.isEmpty()) {
                // 如果AI解析失败，尝试简单的正则匹配
                return parseTextInput(voiceText, "AUTO");
            }
            
            // 尝试提取JSON
            String jsonContent = extractJsonFromResponse(content);
            
            // 解析AI返回的JSON
            try {
                Map<String, Object> result = objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
                
                // 如果没有日期，设置为今天
                if (!result.containsKey("date")) {
                    result.put("date", LocalDate.now().toString());
                }
                
                return result;
            } catch (Exception e) {
                log.warn("解析AI返回的JSON失败，使用正则匹配: {}", e.getMessage());
                // 如果AI解析失败，尝试简单的正则匹配
                return parseTextInput(voiceText, "AUTO");
            }
        } catch (Exception e) {
            log.error("语音解析失败: {}", e.getMessage());
            // 如果AI解析失败，尝试简单的正则匹配
            return parseTextInput(voiceText, "AUTO");
        }
    }

    @Override
    public Map<String, Object> optimizeDietText(String text) {
        if (!StringUtils.hasText(text)) {
            return Map.of("items", List.of(), "totalCalories", 0);
        }
        try {
            String prompt = String.format("""
                    请将以下中文饮食记录解析为结构化JSON：
                    "%s"
                    
                    要求：
                    - 识别每种食物的名称name、数量quantity、单位unit（如g、克、碗、杯、片等）
                    - 如能估算热量，给出calories；否则可省略
                    - 计算总热量totalCalories（如无法估算则为0）
                    
                    返回JSON：
                    {
                      "items": [
                        { "name": "食物名", "quantity": 数量, "unit": "单位", "calories": 可选 }
                      ],
                      "totalCalories": 数值
                    }
                    """, text);
            String content = callChatWithFallback(prompt);
            String json = extractJsonFromResponse(content);
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            Object itemsObj = data.getOrDefault("items", List.of());
            List<Map<String, Object>> items = new ArrayList<>();
            if (itemsObj instanceof List<?>) {
                for (Object o : (List<?>) itemsObj) {
                    if (o instanceof Map<?, ?> mm) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> item = new HashMap<>((Map<String, Object>) mm);
                        // 本地补全缺失热量
                        if (item.get("calories") == null) {
                            Double qty = null;
                            try {
                                Object q = item.get("quantity");
                                if (q != null) qty = Double.valueOf(q.toString());
                            } catch (Exception ignored) {}
                            String unit = Optional.ofNullable(item.get("unit")).map(Object::toString).orElse("");
                            String name = Optional.ofNullable(item.get("name")).map(Object::toString).orElse("");
                            Integer base = lookupCalories(name);
                            if (base != null) {
                                double calories;
                                if ("g".equalsIgnoreCase(unit) || "克".equals(unit)) {
                                    double q = qty != null ? qty : 0d;
                                    calories = base * (q / 100.0);
                                } else {
                                    double q = qty != null ? qty : 1.0;
                                    calories = base * q;
                                }
                                item.put("calories", Math.round(calories));
                            }
                        }
                        items.add(item);
                    }
                }
            }
            Object totalObj = data.getOrDefault("totalCalories", 0);
            double total = 0d;
            try {
                total = Double.parseDouble(String.valueOf(totalObj));
            } catch (Exception ignored) {}
            if (total <= 0d) {
                total = items.stream()
                        .map(i -> i.get("calories"))
                        .filter(Objects::nonNull)
                        .mapToDouble(v -> Double.parseDouble(v.toString()))
                        .sum();
            }
            Map<String, Object> result = new HashMap<>();
            result.put("items", items);
            result.put("totalCalories", total);
            return result;
        } catch (Exception e) {
            List<Map<String, Object>> items = simpleDietParse(text);
            double total = items.stream()
                    .map(m -> m.get("calories"))
                    .filter(Objects::nonNull)
                    .mapToDouble(v -> Double.parseDouble(v.toString()))
                    .sum();
            return Map.of("items", items, "totalCalories", total);
        }
    }

    private List<Map<String, Object>> simpleDietParse(String text) {
        List<Map<String, Object>> items = new ArrayList<>();
        String normalized = text.replaceAll("[，,。;；、\\s]+", " ").trim();
        String[] parts = normalized.split(" ");
        Map<String, Integer> calorieDict = getCalorieDict();

        Pattern qtyUnit = Pattern.compile("(\\d+(?:\\.\\d+)?)(g|克|碗|杯|片|个|份|盘|勺|袋|盒)?");
        for (String p : parts) {
            String name = p.replaceAll("(\\d+(?:\\.\\d+)?)(g|克|碗|杯|片|个|份|盘|勺|袋|盒)?", "").trim();
            if (name.isEmpty()) continue;
            Matcher m = qtyUnit.matcher(p);
            Double qty = 1.0;
            String unit = "份";
            if (m.find()) {
                String q = m.group(1);
                if (q != null) qty = Double.parseDouble(q);
                unit = m.group(2);
                if (unit == null) unit = "份";
            }
            Map<String, Object> item = new HashMap<>();
            item.put("name", name);
            item.put("quantity", qty);
            item.put("unit", unit);
            
            // 简单估算逻辑
            Integer baseCal = lookupCalories(name);
            
            if (baseCal != null) {
                double calories = 0;
                if ("g".equalsIgnoreCase(unit) || "克".equals(unit)) {
                    calories = baseCal * (qty / 100.0);
                } else {
                    // 默认按份计算，简单乘法
                    calories = baseCal * qty;
                }
                item.put("calories", Math.round(calories));
            }
            items.add(item);
        }
        return items;
    }

    // 词典查找（模糊包含匹配）
    private Integer lookupCalories(String name) {
        Map<String, Integer> dict = getCalorieDict();
        for (Map.Entry<String, Integer> entry : dict.entrySet()) {
            String key = entry.getKey();
            if (name.contains(key) || key.contains(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    // 常见食物热量词典，单位：每100g或典型份
    private Map<String, Integer> getCalorieDict() {
        Map<String, Integer> calorieDict = new HashMap<>(Map.of(
                "米饭", 130,
                "鸡肉", 165,
                "牛肉", 250,
                "鱼", 120,
                "青菜", 20,
                "面包", 265,
                "鸡蛋", 70,
                "苹果", 50,
                "香蕉", 90,
                "牛奶", 65
        ));
        calorieDict.put("汉堡", 450);
        calorieDict.put("薯条", 300);
        calorieDict.put("可乐", 140);
        calorieDict.put("炸鸡", 280);
        calorieDict.put("韩式炸鸡", 300);
        calorieDict.put("鳗鱼饭", 650);
        calorieDict.put("披萨", 260);
        calorieDict.put("沙拉", 150);
        calorieDict.put("咖啡", 10);
        calorieDict.put("拿铁", 150);
        calorieDict.put("饺子", 70);
        calorieDict.put("煎饺", 75);
        calorieDict.put("鸭腿", 300);
        calorieDict.put("花生饼", 160);
        return calorieDict;
    }

    private String extractJsonFromResponse(String content) {
        if (content == null || content.isEmpty()) {
            return "{}";
        }
        // 移除可能的markdown代码块标记
        content = content.trim();
        content = content.replaceAll("^```json\\s*", "");
        content = content.replaceAll("^```\\s*", "");
        content = content.replaceAll("```\\s*$", "");
        
        // 提取JSON部分
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private String callChatWithFallback(String prompt) {
        try {
            ChatClient client = chatClientBuilder.build();
            var response = client.prompt(new Prompt(prompt)).call();
            return response.content();
        } catch (WebClientResponseException.NotFound ex) {
            return ollamaLegacyClient.generate(prompt, null, null);
        }
    }

    private static class Range {
        final double min;
        final double max;
        
        Range(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }
}

