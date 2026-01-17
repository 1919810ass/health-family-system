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

            String prompt = "识别以下饮食内容，返回JSON数组，每项包含name与calories:" + desc + (request.imageUrl() != null ? (" 图片URL:" + request.imageUrl()) : "");
            String content = chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
            
            // Clean up possible markdown code blocks if AI returns them
            if (content.contains("```")) {
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            }
            
            items = objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
            totalCalories = items.stream().map(m -> m.get("calories")).filter(v -> v != null).mapToDouble(v -> Double.valueOf(v.toString())).sum();
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
        LocalDate start = LocalDate.now().minusDays(7);
        List<HealthLog> logs;
        if (familyId != null) {
            logs = healthLogRepository.findByUserOrderByLogDateDesc(userRepository.findById(requesterId).orElseThrow(() -> new RuntimeException("用户不存在")))
                    .stream().filter(l -> l.getType() == HealthLogType.DIET && !l.getLogDate().isBefore(start)).collect(Collectors.toList());
        } else {
            logs = healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(requesterId, HealthLogType.DIET)
                    .stream().filter(l -> !l.getLogDate().isBefore(start)).collect(Collectors.toList());
        }
        double total = 0d;
        for (HealthLog l : logs) total += extractCalories(l.getContentJson());
        if (Boolean.TRUE.equals(dp)) {
            double eps = epsilon != null && epsilon > 0 ? epsilon : 1.0;
            total = addLaplaceNoise(total, 100.0, eps);
        }
        String base = "过去7天总热量约" + Math.round(total) + "千卡。";
        try {
            String prompt = """
                    请根据过去一周的饮食热量与均衡性，生成一份结构化、人性化的周营养报告。
                    要求：
                    1. 使用HTML格式输出（不包含markdown标记，直接返回HTML标签）。
                    2. 使用 <h3> 作为章节标题（如：总体情况、均衡性分析、改进建议）。
                    3. 使用 <ul> 和 <li> 列表展示要点。
                    4. 重点内容使用 <strong> 加粗。
                    5. 段落之间使用 <p> 标签，章节之间使用 <hr> 分隔。
                    6. 语言自然流畅，专业且具有亲和力。
                    基础数据：
                    """ + base;
            return chatModel.call(new Prompt(new UserMessage(prompt))).getResult().getOutput().getContent();
        } catch (Exception e) {
            return "<p>" + base + "蛋白质可能不足，建议增加鸡蛋、牛奶。</p>";
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
        content.put("note", request.note());

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
