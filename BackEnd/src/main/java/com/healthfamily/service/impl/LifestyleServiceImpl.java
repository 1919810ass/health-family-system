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
import com.healthfamily.service.HealthDataAiService;
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
import java.util.regex.Pattern;
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
/**
 * Lifestyle服务Impl实现类
 * <p>
 * 实现平台核心业务服务，负责业务编排、数据聚合及与 AI/规则引擎的协同。
 * </p>
 */
@RequiredArgsConstructor
public class LifestyleServiceImpl implements LifestyleService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final HealthLogRepository healthLogRepository;
    private final com.healthfamily.service.FoodRecognitionService foodRecognitionService;
    private final HealthLogService healthLogService;
    private final HealthDataAiService healthDataAiService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param file 业务参数
     * @return 业务返回结果
     */
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
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public DietIngestResponse ingestDiet(Long requesterId, DietIngestRequest request) {
        User user = userRepository.findById(request.userId() != null ? request.userId() : requesterId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Family family = null;
        if (request.familyId() != null) family = familyRepository.findById(request.familyId()).orElse(null);

        List<Map<String, Object>> items;
        double totalCalories;
        // 上传了图片则用本地视觉模型 qwen2.5vl:3b 识别卡路里；否则用 qwen2.5:7b 文字分析
        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            Path imagePath = resolveImagePathFromUrl(request.imageUrl(), requesterId);
            if (imagePath != null && imagePath.toFile().exists()) {
                var analysis = foodRecognitionService.dietAnalysisFromImage(imagePath);
                items = analysis.items() != null ? analysis.items() : new ArrayList<>();
                totalCalories = analysis.totalCalories();
            } else {
                log.warn("Diet image not found for url: {}", request.imageUrl());
                items = new ArrayList<>();
                totalCalories = 0d;
            }
        } else {
            // 纯文字：使用 HealthDataAiService（qwen2.5:7b）分析
            String desc = request.description() != null ? request.description().trim() : "";
            if (request.quantity() != null && !request.quantity().isBlank()) {
                desc = desc.isEmpty() ? "份量：" + request.quantity() : desc + "，份量：" + request.quantity();
            }
            if (desc.isEmpty()) {
                items = List.of(Map.of("name", "未填写描述", "calories", 0));
                totalCalories = 0d;
            } else {
                Map<String, Object> optimized = healthDataAiService.optimizeDietText(desc);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) optimized.getOrDefault("items", List.of());
                items = list != null ? list : new ArrayList<>();
                Object totalObj = optimized.get("totalCalories");
                totalCalories = totalObj != null ? Double.parseDouble(totalObj.toString()) : 0d;
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
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public List<RecipeRecommendResponse> recommendRecipes(Long requesterId, RecipeRecommendRequest request) {
        List<RecipeRecommendResponse> list = new ArrayList<>();
        try {
            String prompt = """
                    你是一个资深营养师。请根据以下健康标签推荐3个食谱。
                    要求：
                    1. 必须只返回一个 JSON 数组，不要包含任何前言（如"根据您的标签..."）或后记。
                    2. 每个食谱对象必须包含以下字段：
                       - title: 食谱名称
                       - items: 包含具体食材的字符串数组
                       - note: 针对该标签的推荐理由或建议
                    3. 推荐内容要健康且符合标签特征。
                    标签：%s
                    """.formatted(String.join(",", request.tags() != null ? request.tags() : List.of()));
            
            String content = chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
            
            // 更鲁棒地提取 JSON 数组部分
            String jsonArray = extractJsonArray(content);
            
            List<Map<String, Object>> arr = objectMapper.readValue(jsonArray, new TypeReference<List<Map<String, Object>>>() {});
            list = arr.stream().map(m -> new RecipeRecommendResponse(
                String.valueOf(m.getOrDefault("title", "未知食谱")),
                (List<String>) m.getOrDefault("items", List.of()),
                String.valueOf(m.getOrDefault("note", ""))
            )).collect(Collectors.toList());
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

    private String extractJsonArray(String content) {
        if (content == null || content.isBlank()) return "[]";
        
        // 尝试移除 markdown 代码块
        if (content.contains("```")) {
            content = content.replaceAll("```json", "").replaceAll("```", "").trim();
        }
        
        // 查找第一个 '[' 和最后一个 ']'
        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        
        if (start != -1 && end != -1 && end > start) {
            return content.substring(start, end + 1);
        }
        
        // 如果没找到数组，尝试找对象
        int startObj = content.indexOf('{');
        int endObj = content.lastIndexOf('}');
        if (startObj != -1 && endObj != -1 && endObj > startObj) {
            String obj = content.substring(startObj, endObj + 1);
            return "[" + obj + "]"; // 包装成数组
        }
        
        return content.trim();
    }

    @Override
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param familyId 家庭唯一标识
     * @param dp 业务参数
     * @param epsilon 业务参数
     * @return 业务返回结果
     */
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
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param request 请求体数据
     * @return 无
     */
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
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @return 业务返回结果
     */
    public String suggestExercise(Long requesterId) {
        try {
            LocalDate start = LocalDate.now().minusDays(14);
            List<HealthLog> logs = healthLogRepository.findByUser_IdAndLogDateBetweenOrderByLogDateDesc(
                    requesterId, start, LocalDate.now())
                    .stream()
                    .filter(log -> log.getType() == HealthLogType.SPORT)
                    .limit(20)
                    .collect(Collectors.toList());

            String baseInfo = logs.isEmpty() 
                ? "最近14天暂无运动日志记录。" 
                : logs.stream().map(this::formatLog).collect(Collectors.joining("\n"));

            String prompt = """
                    你是一个资深健康教练。请根据以下最近14天的运动记录提供个性化建议。
                    数据：
                    %s
                    
                    要求：
                    1. 使用HTML格式输出。
                    2. 评估运动量是否达标，指出强度和频率是否合适。
                    3. 给出后续的训练方案或调整建议。
                    4. 使用 <h3> 分隔章节，使用列表展示建议。
                    5. 语气专业且具有鼓励性。
                    """.formatted(baseInfo);
            return chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("获取运动建议失败", e);
            return "<p>建议每周3次有氧运动，每次30分钟。</p>";
        }
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param request 请求体数据
     * @return 无
     */
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
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @return 业务返回结果
     */
    public String analyzeSleep(Long requesterId) {
        try {
            LocalDate start = LocalDate.now().minusDays(14);
            List<HealthLog> logs = healthLogRepository.findByUser_IdAndLogDateBetweenOrderByLogDateDesc(
                    requesterId, start, LocalDate.now())
                    .stream()
                    .filter(log -> log.getType() == HealthLogType.SLEEP)
                    .limit(20)
                    .collect(Collectors.toList());

            String baseInfo = logs.isEmpty() 
                ? "最近14天暂无睡眠日志记录。" 
                : logs.stream().map(this::formatLog).collect(Collectors.joining("\n"));

            String prompt = """
                    你是一个资深健康管理师。请根据以下最近14天的睡眠数据进行深度分析并给出改善建议。
                    数据：
                    %s
                    
                    要求：
                    1. 使用HTML格式输出。
                    2. 若深度睡眠不足2小时或入睡过晚，请在建议中重点指出。
                    3. 评估睡眠规律性，指出可能的干扰因素。
                    4. 使用 <h3> 分隔章节，使用列表展示建议。
                    5. 语气温和且专业，像一位贴心的健康顾问。
                    """.formatted(baseInfo);
            String result = chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
            if (result.contains("```html")) {
                result = result.replace("```html", "").replace("```", "");
            }
            return result;
        } catch (Exception e) {
            log.error("分析睡眠失败", e);
            return "<p>深度睡眠不足可能影响免疫力，建议规律作息与睡前减少刺激。</p>";
        }
    }

    @Override
    /**
     * 分析情绪建议
     * @param requesterId 用户ID
     * @return 建议内容
     */
    public String analyzeMood(Long requesterId) {
        try {
            LocalDate start = LocalDate.now().minusDays(14);
            // 情绪分析需要综合考虑睡眠和运动
            List<HealthLog> logs = healthLogRepository.findByUser_IdAndLogDateBetweenOrderByLogDateDesc(
                    requesterId, start, LocalDate.now())
                    .stream()
                    .filter(log -> log.getType() == HealthLogType.MOOD || log.getType() == HealthLogType.SLEEP || log.getType() == HealthLogType.SPORT)
                    .limit(30)
                    .collect(Collectors.toList());

            String baseInfo = logs.isEmpty() 
                ? "最近14天暂无相关情绪、睡眠或运动日志记录。" 
                : logs.stream().map(this::formatLog).collect(Collectors.joining("\n"));

            String prompt = """
                    你是一个资深心理咨询师。请根据以下最近14天的情绪、睡眠和运动数据进行关联分析并给出调节建议。
                    数据：
                    %s
                    
                    要求：
                    1. 使用HTML格式输出。
                    2. 分析情绪波动与睡眠、运动之间的关联。
                    3. 给出针对性的心理疏导和生活调节方案（如冥想、具体的放松技巧等）。
                    4. 使用 <h3> 分隔章节，使用列表展示建议。
                    5. 语气专业、温和且具有共情能力。
                    """.formatted(baseInfo);

            String result = chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
            if (result.contains("```html")) {
                result = result.replace("```html", "").replace("```", "");
            }
            return result;
        } catch (Exception e) {
            log.error("分析情绪失败", e);
            return "<p>建议保持规律作息，增加户外活动，心情不佳时多与家人交流。</p>";
        }
    }

    @Override
    /**
     * 分析体征数据并给出建议
     * @param requesterId 用户ID
     * @return HTML 格式的建议
     */
    public String analyzeVitals(Long requesterId) {
        try {
            LocalDate start = LocalDate.now().minusDays(14);
            List<HealthLog> logs = healthLogRepository.findByUser_IdAndLogDateBetweenOrderByLogDateDesc(
                    requesterId, start, LocalDate.now())
                    .stream()
                    .filter(log -> log.getType() == HealthLogType.VITALS)
                    .limit(20)
                    .collect(Collectors.toList());

            String baseInfo = logs.isEmpty() 
                ? "最近14天暂无体征日志记录。" 
                : logs.stream().map(this::formatLog).collect(Collectors.joining("\n"));

            String prompt = """
                    你是一个资深健康管理专家。请根据以下最近14天的体征数据（血压、血糖、心率、体温、体重等）进行分析。
                    数据：
                    %s
                    
                    要求：
                    1. 使用HTML格式输出（不包含markdown标记）。
                    2. 评估体征是否平稳，指出任何异常或波动的趋势。
                    3. 提供针对性的生活建议（饮食、作息等）。
                    4. 使用 <h3> 作为章节标题，使用 <ul> 和 <li> 展示建议。
                    5. 语气专业且贴心，必要时提醒就医。
                    """.formatted(baseInfo);

            String result = chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
            if (result.contains("```html")) {
                result = result.replace("```html", "").replace("```", "");
            }
            return result;
        } catch (Exception e) {
            log.error("分析体征失败", e);
            return "<p>最近体征数据平稳，请继续保持良好的监测习惯。（分析服务暂不可用）</p>";
        }
    }

    @Override
    @Transactional
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param request 请求体数据
     * @return 无
     */
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
    /**
     * 执行业务操作
     * @param requesterId 业务对象唯一标识
     * @param request 请求体数据
     * @return 无
     */
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

    /** 从饮食图片 URL（如 /api/lifestyle/files/123/diet_xxx.jpg）解析出本地路径 */
    private Path resolveImagePathFromUrl(String imageUrl, long userId) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        // 匹配 /api/lifestyle/files/{userId}/{filename}
        var pattern = Pattern.compile(".*/files/(\\d+)/([^/]+)$");
        var matcher = pattern.matcher(imageUrl.trim());
        if (matcher.find()) {
            String uid = matcher.group(1);
            String filename = matcher.group(2);
            return Paths.get(uploadDir, uid, filename).toAbsolutePath().normalize();
        }
        return null;
    }

    private String formatLog(HealthLog log) {
        StringBuilder builder = new StringBuilder();
        String typeName = switch (log.getType()) {
            case DIET -> "饮食";
            case SLEEP -> "睡眠";
            case SPORT -> "运动";
            case MOOD -> "情绪";
            case VITALS -> "体征";
        };
        builder.append("日期:").append(log.getLogDate())
               .append(" 类型:").append(typeName);
        if (log.getScore() != null) {
            builder.append(" 评分:").append(log.getScore());
        }
        try {
            Map<String, Object> content = fromJson(log.getContentJson());
            if (!content.isEmpty()) {
                builder.append(" 详情:");
                content.forEach((key, value) -> {
                    if (value != null && !value.toString().isEmpty()) {
                        builder.append(" ").append(key).append("=").append(value);
                    }
                });
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return builder.toString();
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            log.warn("解析日志内容失败: {}", ex.getMessage());
            return Map.of();
        }
    }
}
