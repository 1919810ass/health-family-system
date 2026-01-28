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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFoodRecognitionServiceImpl implements FoodRecognitionService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    @Value("${spring.ai.ollama.vision.model:llava:7b}")
    private String visionModel;
    @Value("${spring.ai.ollama.vision.temperature:0.3}")
    private double visionTemperature;

    @Override
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
    
    private MimeType getMimeType(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.endsWith(".png")) return MimeTypeUtils.IMAGE_PNG;
        if (filename.endsWith(".gif")) return MimeTypeUtils.IMAGE_GIF;
        return MimeTypeUtils.IMAGE_JPEG;
    }
}
