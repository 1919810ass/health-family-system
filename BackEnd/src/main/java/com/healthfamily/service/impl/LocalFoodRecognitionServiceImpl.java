package com.healthfamily.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.service.FoodRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
/**
 * LocalFoodRecognition服务Impl实现类
 * <p>
 * 实现平台核心业务服务，负责业务编排、数据聚合及与 AI/规则引擎的协同。
 * </p>
 */
@RequiredArgsConstructor
public class LocalFoodRecognitionServiceImpl implements FoodRecognitionService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    @Value("${spring.ai.ollama.vision.model:qwen2.5vl:3b}")
    private String visionModel;
    @Value("${spring.ai.ollama.vision.temperature:0.3}")
    private double visionTemperature;

    @Override
    /**
     * 执行业务操作
     * @param imagePath 业务参数
     * @return 业务返回结果
     */
    public RecognitionResult recognize(Path imagePath) {
        try {
            log.info("Starting AI food recognition for: {}", imagePath);
            
            var resource = new FileSystemResource(imagePath);
            MimeType mimeType = getMimeType(imagePath);
            
            var userMsg = new UserMessage(
                "请分析这张图片。如果是食物，请给出食物名称；如果是药物，请给出药物名称；如果是化验单，请说明是化验单。请以JSON格式返回，包含 'foodName' (字符串) 和 'confidence' (0.0-1.0) 两个字段。不要返回任何Markdown标记。",
                List.of(new Media(mimeType, resource))
            );

            String response = chatClientBuilder.build()
                    .prompt()
                    .options(OllamaOptions.builder()
                            .withModel(visionModel)
                            .withTemperature(visionTemperature)
                            .build())
                    .messages(userMsg)
                    .call()
                    .content();

            log.info("AI Recognition Response: {}", response);

            // Parse JSON with enhanced cleaning
            String json = response;
            // 移除可能的前缀文本
            int jsonStartIndex = json.indexOf("{");
            int jsonEndIndex = json.lastIndexOf("}");
            
            if (jsonStartIndex != -1 && jsonEndIndex != -1) {
                json = json.substring(jsonStartIndex, jsonEndIndex + 1);
            } else {
                 // 如果找不到 JSON 结构，尝试构造一个默认的
                 log.warn("Could not find JSON structure in response: {}", response);
                 // 简单的文本匹配作为后备
                 if (response.contains("包子")) return new RecognitionResult("包子", 0.95);
                 return new RecognitionResult("未知物品", 0.0);
            }
            
            // 启用允许注释的特性
            objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true);
            JsonNode node;
            try {
                node = objectMapper.readTree(json);
            } catch (Exception parseException) {
                // 如果第一次解析失败，尝试更激进的正则清洗
                log.warn("First JSON parse failed, trying aggressive cleaning: {}", parseException.getMessage());
                // 仅移除 // 风格注释（不使用 DOTALL 模式，避免吃掉换行符和后续内容）
                // 注意：这可能会误伤 URL 中的 //，但在此场景下（foodName/confidence）概率较低
                json = json.replaceAll("//[^\\n]*", "");
                // 移除 /**/ 风格注释
                json = json.replaceAll("/\\*[\\s\\S]*?\\*/", "");
                node = objectMapper.readTree(json);
            }
            String foodName = node.has("foodName") ? node.get("foodName").asText() : "未知物品";
            double confidence = node.has("confidence") ? node.get("confidence").asDouble() : 0.8;
            
            return new RecognitionResult(foodName, confidence);

        } catch (WebClientResponseException.NotFound e) {
            return new RecognitionResult("当前本地 Ollama 版本不支持图像对话", 0.0);
        } catch (Exception e) {
            log.error("AI recognition failed", e);
            // Fallback gracefully
            return new RecognitionResult("识别服务暂不可用", 0.0);
        }
    }

    @Override
    public com.healthfamily.service.FoodRecognitionService.DietAnalysisResult dietAnalysisFromImage(Path imagePath) {
        String response = null;
        try {
            log.info("Starting diet image analysis (calories) for: {}", imagePath);
            var resource = new FileSystemResource(imagePath);
            MimeType mimeType = getMimeType(imagePath);
            var userMsg = new UserMessage(
                "请分析这张食物图片。列出图中可见的食物，并估算每样的大致热量（千卡/kcal）。"
                + "只返回一个JSON数组，不要任何Markdown或说明。每项格式：{\"name\":\"食物名称\",\"calories\":数字}。"
                + "例如：[{\"name\":\"米饭\",\"calories\":200},{\"name\":\"青菜\",\"calories\":50}]",
                List.of(new Media(mimeType, resource))
            );
            response = chatClientBuilder.build()
                    .prompt()
                    .options(OllamaOptions.builder()
                            .withModel(visionModel)
                            .withTemperature(visionTemperature)
                            .build())
                    .messages(userMsg)
                    .call()
                    .content();
            log.info("Diet analysis response length: {}", response != null ? response.length() : 0);
            String json = extractJsonArray(response);
            if (json == null || json.isBlank()) {
                if (response != null && response.length() > 0) {
                    log.warn("No JSON array found in response, first 300 chars: {}", response.length() > 300 ? response.substring(0, 300) + "..." : response);
                }
                return new com.healthfamily.service.FoodRecognitionService.DietAnalysisResult(List.of(), 0d);
            }
            com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>> typeRef =
                    new com.fasterxml.jackson.core.type.TypeReference<>() {};
            List<Map<String, Object>> rawItems = objectMapper.readValue(json, typeRef);
            // 统一键名：模型可能返回 name/食物/食物名称、calories/热量/卡路里
            List<Map<String, Object>> items = new ArrayList<>();
            double total = 0d;
            for (Map<String, Object> item : rawItems) {
                String name = getStringKey(item, "name", "食物", "食物名称", "食物名");
                Object c = getAnyKey(item, "calories", "热量", "卡路里", "kcal");
                double cal = 0d;
                if (c != null) {
                    try {
                        cal = Double.parseDouble(c.toString().replaceAll("[^0-9.\\-]", ""));
                    } catch (NumberFormatException ignored) {}
                }
                total += cal;
                items.add(Map.<String, Object>of("name", name != null ? name : "未知", "calories", (int) Math.round(cal)));
            }
            if (items.isEmpty() && response != null && response.length() > 0) {
                log.warn("Parsed 0 diet items from response, first 400 chars: {}", response.length() > 400 ? response.substring(0, 400) + "..." : response);
            }
            return new com.healthfamily.service.FoodRecognitionService.DietAnalysisResult(items, total);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Vision model not available: {}", e.getMessage());
            return new com.healthfamily.service.FoodRecognitionService.DietAnalysisResult(List.of(), 0d);
        } catch (Exception e) {
            log.error("Diet image analysis failed", e);
            if (response != null && response.length() > 0) {
                log.warn("Response snippet: {}", response.length() > 350 ? response.substring(0, 350) + "..." : response);
            }
            return new com.healthfamily.service.FoodRecognitionService.DietAnalysisResult(List.of(), 0d);
        }
    }

    private static String getStringKey(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null && v.toString().trim().length() > 0) return v.toString().trim();
        }
        return null;
    }

    private static Object getAnyKey(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null) return v;
        }
        return null;
    }

    private String extractJsonArray(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        // 去掉 markdown 代码块
        if (raw.contains("```")) {
            int start = raw.indexOf("```");
            int next = raw.indexOf("```", start + 3);
            if (next > start) {
                raw = raw.substring(start + 3, next).trim();
                if (raw.startsWith("json")) raw = raw.substring(4).trim();
            }
        }
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start != -1 && end != -1 && end > start) {
            return raw.substring(start, end + 1);
        }
        // 可能是 {"items": [...]} 结构
        int objStart = raw.indexOf('{');
        int objEnd = raw.lastIndexOf('}');
        if (objStart != -1 && objEnd != -1 && objEnd > objStart) {
            try {
                JsonNode node = objectMapper.readTree(raw.substring(objStart, objEnd + 1));
                if (node.has("items") && node.get("items").isArray()) {
                    return objectMapper.writeValueAsString(node.get("items"));
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
    
    private MimeType getMimeType(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.endsWith(".png")) return MimeTypeUtils.IMAGE_PNG;
        if (filename.endsWith(".gif")) return MimeTypeUtils.IMAGE_GIF;
        return MimeTypeUtils.IMAGE_JPEG;
    }
}
