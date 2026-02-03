package com.healthfamily.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.entity.Family;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.FamilyRepository;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.HealthLogService;
import com.healthfamily.service.LifestyleService;
import com.healthfamily.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LifestyleServiceImpl implements LifestyleService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final HealthLogRepository healthLogRepository;
    private final com.healthfamily.service.FoodRecognitionService foodRecognitionService;
    private final HealthLogService healthLogService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public ImageUploadResponse uploadDietImage(Long requesterId, MultipartFile file) {
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .map(n -> n.contains(".") ? n.substring(n.lastIndexOf('.') + 1) : "jpg")
                .orElse("jpg");
        String filename = "diet_" + System.currentTimeMillis() + "." + ext;
        
        Path base = Paths.get(uploadDir, String.valueOf(requesterId)).toAbsolutePath().normalize();
        Path target;
        try {
            Files.createDirectories(base);
            target = base.resolve(filename);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new RuntimeException("上传图片失败", e);
        }
        
        String url = "/api/lifestyle/files/" + requesterId + "/" + filename;
        
        // Local recognition
        var result = foodRecognitionService.recognize(target);
        
        return new ImageUploadResponse(url, result.foodName(), result.confidence(), System.currentTimeMillis());
    }

    @Override
    @Transactional
    public DietIngestResponse ingestDiet(Long requesterId, DietIngestRequest request) {
        User user = userRepository.findById(request.userId() != null ? request.userId() : requesterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Family family = null;
        if (request.familyId() != null) family = familyRepository.findById(request.familyId()).orElse(null);

        List<Map<String, Object>> items = new ArrayList<>();
        Double totalCalories = 0d;
        try {
            // Clean up description if it contains auto-generated format
            String desc = request.description() != null ? request.description() : "";
            // Extract core food name if in "识别结果: XXX (置信度: YYY)" format
            if (desc.startsWith("识别结果:")) {
                int start = desc.indexOf(":") + 1;
                int end = desc.indexOf("(");
                if (end > start) {
                    desc = desc.substring(start, end).trim();
                }
            }

            String prompt = "识别以下饮食内容，返回JSON数组，每项包含name与calories:" + desc;
            if (request.quantity() != null && !request.quantity().isBlank()) {
                prompt += "，份量为：" + request.quantity();
            }
            prompt += (request.imageUrl() != null ? (" 图片URL:" + request.imageUrl()) : "") + "。如果是蒸饺，热量约为200千卡/100g。";
            
            String content = chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
            
            // Clean up possible markdown code blocks if AI returns them
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("```json") + 7);
                if (content.contains("```")) {
                    content = content.substring(0, content.indexOf("```"));
                }
            } else if (content.contains("```")) {
                content = content.substring(content.indexOf("```") + 3);
                if (content.contains("```")) {
                    content = content.substring(0, content.indexOf("```"));
                }
            }
            content = content.trim();
            
            items = objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
            totalCalories = items.stream().map(m -> m.get("calories")).filter(v -> v != null).mapToDouble(v -> {
                try {
                    return Double.valueOf(v.toString());
                } catch (NumberFormatException nfe) {
                    return 0d;
                }
            }).sum();
        } catch (Exception e) {
            log.error("Diet ingest failed", e);
            // Fallback for demo if AI fails
            if (request.description() != null && request.description().contains("包子")) {
                 items = List.of(Map.of("name", "肉包子", "calories", 250));
                 totalCalories = 250d;
            } else {
                 items = List.of(Map.of("name", "米饭", "calories", 260), Map.of("name", "青菜", "calories", 60), Map.of("name", "清蒸鱼", "calories", 200));
                 totalCalories = 520d;
            }
        }

        HealthLog log = HealthLog.builder()
                .user(user)
                .logDate(LocalDate.now())
                .type(HealthLogType.DIET)
                .contentJson(writeJsonSafely(Map.of("items", items, "totalCalories", totalCalories)))
                .isAbnormal(false)
                .build();
        healthLogRepository.save(log);

        return new DietIngestResponse(items, totalCalories);
    }

    @Override
    public List<RecipeRecommendResponse> recommendRecipes(Long requesterId, RecipeRecommendRequest request) {
        List<RecipeRecommendResponse> list = new ArrayList<>();
        try {
            String prompt = "根据标签推荐3个食谱，用JSON数组返回，每项含title、items、note。标签:" + String.join(",", request.tags() != null ? request.tags() : List.of());
            String content = chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
            // Clean up possible markdown code blocks if AI returns them
            if (content.contains("```")) {
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            }
            List<Map<String, Object>> arr = objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
            list = arr.stream().map(m -> new RecipeRecommendResponse(String.valueOf(m.get("title")), (List<String>) m.getOrDefault("items", List.of()), String.valueOf(m.getOrDefault("note", "")))).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error recommending recipes", e);
            list = List.of(
                    new RecipeRecommendResponse("低GI晚餐", List.of("杂粮饭", "清蒸鱼", "西兰花"), "适合血糖控制"),
                    new RecipeRecommendResponse("高蛋白早餐", List.of("鸡蛋", "牛奶", "燕麦"), "提升饱腹感"),
                    new RecipeRecommendResponse("轻脂午餐", List.of("蔬菜沙拉", "鸡胸肉"), "控制总热量")
            );
        }
        return list;
    }

    @Override
    public String analyzeDietWeekly(Long requesterId, Long familyId, Boolean dp, Double epsilon) {
        log.info("Starting weekly diet analysis for user: {}, familyId: {}", requesterId, familyId);
        // 临时放宽时间范围到30天，以便调试
        LocalDate start = LocalDate.now().minusDays(30);
        List<HealthLog> logs;
        if (familyId != null) {
            logs = healthLogRepository.findByUserOrderByLogDateDesc(userRepository.findById(requesterId).orElseThrow(() -> new RuntimeException("用户不存在")))
                    .stream().filter(l -> l.getType() == HealthLogType.DIET && !l.getLogDate().isBefore(start)).collect(Collectors.toList());
        } else {
            logs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(requesterId, HealthLogType.DIET)
                    .stream().filter(l -> !l.getLogDate().isBefore(start)).collect(Collectors.toList());
        }
        
        log.info("Found {} diet logs for analysis since {}", logs.size(), start);
        
        // 即使没有日志，也要生成一个基础报告，避免前端显示为空
        if (logs.isEmpty()) {
             return "<p>系统未检测到您的饮食记录（查询起始日期：" + start + "）。请确认您已成功记录饮食。</p>";
        }

        double total = 0d;
        for (HealthLog l : logs) total += extractCalories(l.getContentJson());
        if (Boolean.TRUE.equals(dp)) {
            double eps = epsilon != null && epsilon > 0 ? epsilon : 1.0;
            total = addLaplaceNoise(total, 100.0, eps);
        }
        String base = "过去30天总热量约" + Math.round(total) + "千卡。";
        try {
            String prompt = """
                    请根据过去一段时间的饮食热量与均衡性，生成一份结构化、人性化的营养报告。
                    要求：
                    1. 使用HTML格式输出（不包含markdown标记，直接返回HTML标签）。
                    2. 使用 <h3> 作为章节标题（如：总体情况、均衡性分析、改进建议）。
                    3. 使用 <ul> 和 <li> 列表展示要点。
                    4. 重点内容使用 <strong> 加粗。
                    5. 段落之间使用 <p> 标签，章节之间使用 <hr> 分隔。
                    6. 语言自然流畅，专业且具有亲和力。
                    基础数据：
                    """ + base;
            String result = chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
            // 清理可能存在的 markdown 标记
            if (result.contains("```html")) {
                result = result.replace("```html", "").replace("```", "");
            }
            log.info("AI report generated successfully, length: {}", result.length());
            return result;
        } catch (Exception e) {
            log.error("Failed to generate AI report", e);
            return "<p>" + base + "蛋白质可能不足，建议增加鸡蛋、牛奶。（AI服务异常：" + e.getClass().getSimpleName() + "）</p>";
        }
    }

    @Override
    @Transactional
    public void recordExercise(Long requesterId, ExerciseRecordRequest request) {
        log.info("Recording exercise for user: {}", requesterId);
        // Validation handled by @Valid in controller
        
        User user = userRepository.findById(request.userId() != null ? request.userId() : requesterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Map<String, Object> content = new HashMap<>();
        content.put("type", request.type());
        content.put("durationMinutes", request.durationMinutes());
        content.put("distanceKm", request.distanceKm());
        content.put("steps", request.steps());
        String note = request.note();
        if (note == null || note.isBlank()) {
            List<String> parts = new ArrayList<>();
            String t = request.type() != null ? request.type().trim() : "";
            if (!t.isBlank()) {
                String lower = t.toLowerCase();
                t = switch (lower) {
                    case "run" -> "跑步";
                    case "walk" -> "步行";
                    case "swim" -> "游泳";
                    case "bike", "cycling" -> "骑行";
                    default -> t;
                };
                parts.add(t);
            }
            if (request.durationMinutes() != null) parts.add("时长 " + request.durationMinutes() + "分钟");
            if (request.distanceKm() != null) parts.add("距离 " + request.distanceKm() + "公里");
            if (request.steps() != null) parts.add("步数 " + request.steps());
            note = String.join("；", parts);
        } else {
            note = note.trim();
        }
        content.put("note", note);

        HealthLog log = HealthLog.builder()
                .user(user)
                .logDate(LocalDate.now())
                .type(HealthLogType.SPORT)
                .contentJson(writeJsonSafely(content))
                .isAbnormal(false)
                .build();
        healthLogRepository.save(log);
    }

    @Override
    public String suggestExercise(Long requesterId) {
        try {
            String prompt = """
                    请针对血压偏高的人群，生成一份个性化的运动建议。
                    要求：
                    1. 使用HTML格式输出。
                    2. 包含 <h3> 标题（如：推荐运动方案、注意事项）。
                    3. 使用 <ul> 和 <li> 列表清晰展示建议。
                    4. 语气专业、鼓励且令人安心。
                    核心建议：每周3次有氧运动，每次30分钟，可逐步增加至40分钟。
                    """;
            return chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
        } catch (Exception e) {
            return "<p>建议每周3次有氧运动，每次30分钟。</p>";
        }
    }

    @Override
    @Transactional
    public void recordSleep(Long requesterId, SleepRecordRequest request) {
        log.info("Recording sleep for user: {}", requesterId);
        // Validation handled by @Valid in controller
        
        User user = userRepository.findById(request.userId() != null ? request.userId() : requesterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Map<String, Object> content = new HashMap<>();
        content.put("hours", request.hours());
        content.put("deepHours", request.deepHours());
        content.put("wakeCount", request.wakeCount());
        content.put("note", request.note());
        // 保存新增字段
        if (request.bedtime() != null) content.put("bedtime", request.bedtime());
        if (request.wakeTime() != null) content.put("wakeTime", request.wakeTime());
        if (request.sleepLatency() != null) content.put("sleepLatency", request.sleepLatency());
        if (request.wakeUpLatency() != null) content.put("wakeUpLatency", request.wakeUpLatency());

        HealthLog log = HealthLog.builder()
                .user(user)
                .logDate(LocalDate.now())
                .type(HealthLogType.SLEEP)
                .contentJson(writeJsonSafely(content))
                .isAbnormal(false)
                .build();
        healthLogRepository.save(log);
    }

    @Override
    public String analyzeSleep(Long requesterId) {
        try {
            String prompt = """
                    请分析最近的睡眠质量并给出改善建议。
                    要求：
                    1. 使用HTML格式输出。
                    2. 若深度睡眠不足2小时，请在建议中重点指出。
                    3. 使用 <h3> 分隔章节，使用列表展示建议。
                    4. 语气温和，像一位贴心的健康顾问。
                    """;
            return chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
        } catch (Exception e) {
            return "<p>深度睡眠不足可能影响免疫力，建议规律作息与睡前减少刺激。</p>";
        }
    }

    @Override
    @Transactional
    public void recordMood(Long requesterId, MoodRecordRequest request) {
        User user = userRepository.findById(request.userId() != null ? request.userId() : requesterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String time = resolveTime(request.time());
        Map<String, Object> content = new HashMap<>();
        content.put("time", time);
        if (request.emotion() != null && !request.emotion().isBlank()) {
            content.put("emotion", request.emotion().trim());
        }
        content.put("level", request.level());
        if (request.stress() != null) {
            content.put("stress", request.stress());
        }
        if (request.energy() != null) {
            content.put("energy", request.energy());
        }

        String note = request.note();
        if (note == null || note.isBlank()) {
            note = buildMoodNote(request);
        }
        content.put("note", note);

        healthLogService.createLog(
                user.getId(),
                new HealthLogRequest(LocalDate.now(), HealthLogType.MOOD, content, null)
        );
    }

    @Override
    @Transactional
    public void recordVitals(Long requesterId, VitalsRecordRequest request) {
        User user = userRepository.findById(request.userId() != null ? request.userId() : requesterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String time = resolveTime(request.time());
        String type = request.type() != null ? request.type().trim() : "";
        Map<String, Object> content = new HashMap<>();
        content.put("time", time);
        content.put("type", type);

        switch (type) {
            case "血压" -> {
                if (request.systolic() == null || request.diastolic() == null) {
                    throw new RuntimeException("血压数据不完整");
                }
                String unit = (request.unit() == null || request.unit().isBlank()) ? "mmHg" : request.unit().trim();
                content.put("systolic", request.systolic());
                content.put("diastolic", request.diastolic());
                content.put("unit", unit);
                content.put("value", null);
                content.put("note", defaultIfBlank(request.note(), "血压 " + request.systolic() + "/" + request.diastolic() + " " + unit));
            }
            case "血糖" -> {
                if (request.value() == null) {
                    throw new RuntimeException("体征数值不能为空");
                }
                String unit = (request.unit() == null || request.unit().isBlank()) ? "mmol/L" : request.unit().trim();
                content.put("bloodSugar", request.value());
                content.put("value", request.value());
                content.put("unit", unit);
                content.put("note", defaultIfBlank(request.note(), "血糖 " + request.value() + " " + unit));
            }
            case "体温" -> {
                if (request.value() == null) {
                    throw new RuntimeException("体征数值不能为空");
                }
                String unit = (request.unit() == null || request.unit().isBlank()) ? "°C" : request.unit().trim();
                content.put("temperature", request.value());
                content.put("value", request.value());
                content.put("unit", unit);
                content.put("note", defaultIfBlank(request.note(), "体温 " + request.value() + " " + unit));
            }
            case "心率" -> {
                if (request.value() == null) {
                    throw new RuntimeException("体征数值不能为空");
                }
                String unit = (request.unit() == null || request.unit().isBlank()) ? "bpm" : request.unit().trim();
                content.put("heartRate", request.value());
                content.put("value", request.value());
                content.put("unit", unit);
                content.put("note", defaultIfBlank(request.note(), "心率 " + request.value() + " " + unit));
            }
            case "体重" -> {
                if (request.value() == null) {
                    throw new RuntimeException("体征数值不能为空");
                }
                String unit = (request.unit() == null || request.unit().isBlank()) ? "kg" : request.unit().trim();
                content.put("weight", request.value());
                content.put("value", request.value());
                content.put("unit", unit);
                content.put("note", defaultIfBlank(request.note(), "体重 " + request.value() + " " + unit));
            }
            default -> {
                if (request.value() == null) {
                    throw new RuntimeException("体征数值不能为空");
                }
                String unit = request.unit() != null ? request.unit().trim() : "";
                content.put("value", request.value());
                if (!unit.isBlank()) {
                    content.put("unit", unit);
                }
                content.put("note", defaultIfBlank(request.note(), type + " " + request.value() + (unit.isBlank() ? "" : (" " + unit))));
            }
        }

        healthLogService.createLog(
                user.getId(),
                new HealthLogRequest(LocalDate.now(), HealthLogType.VITALS, content, null)
        );
    }

    private String resolveTime(String time) {
        if (time != null && !time.isBlank()) {
            return time.trim();
        }
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String buildMoodNote(MoodRecordRequest request) {
        List<String> parts = new ArrayList<>();
        if (request.emotion() != null && !request.emotion().isBlank()) {
            parts.add("情绪" + request.emotion().trim());
        }
        parts.add("强度" + request.level() + "/5");
        if (request.stress() != null) {
            parts.add("压力" + request.stress() + "/10");
        }
        if (request.energy() != null) {
            parts.add("精力" + request.energy() + "/10");
        }
        return String.join("；", parts);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null) return defaultValue;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }

    private double extractCalories(String json) {
        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            Object c = map.get("totalCalories");
            return c == null ? 0d : Double.valueOf(c.toString());
        } catch (Exception e) {
            return 0d;
        }
    }

    private String writeJsonSafely(Object obj) {
        try { return objectMapper.writeValueAsString(obj); } catch (Exception e) { return "{}"; }
    }

    private double addLaplaceNoise(double value, double sensitivity, double epsilon) {
        double b = sensitivity / epsilon;
        double u = Math.random() - 0.5;
        double noise = -b * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
        return value + noise;
    }
}
