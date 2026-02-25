package com.healthfamily.service;

import com.healthfamily.ai.OllamaLegacyClient;
import com.healthfamily.modules.recommendationv2.service.DocRagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
/**
 * AIAssistant服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
@RequiredArgsConstructor
public class AiAssistantService {

    private final ChatClient.Builder chatClientBuilder;
    private final DocRagService docRagService;
    private final OllamaLegacyClient ollamaLegacyClient;
    @Value("${spring.ai.ollama.vision.model:llava:7b}")
    private String visionModel;
    @Value("${spring.ai.ollama.vision.temperature:0.3}")
    private double visionTemperature;

    /**

     * 执行业务操作

     * @param userMessage 业务参数

     * @param userId 家庭成员唯一标识

     * @return 业务返回结果

     */

    public Flux<String> chatStream(String userMessage, Long userId) {
        // 1. RAG 检索上下文 (增加异常处理，避免 RAG 失败导致整个对话崩溃)
        List<Map<String, Object>> docs = List.of();
        try {
            docs = docRagService.search(userMessage);
        } catch (Exception e) {
            log.warn("RAG retrieval failed: {}", e.getMessage());
            // 继续执行，降级为普通对话
        }

        String context = "";
        if (!docs.isEmpty()) {
            context = docs.stream()
                .map(d -> "标题: " + d.get("title") + "\n内容: " + d.get("snippet"))
                .collect(Collectors.joining("\n\n"));
        }

        // 2. 构建 Prompt
        String systemPrompt = """
                你是一个专业的家庭健康助手。请根据提供的上下文信息回答用户的问题。
                如果上下文信息不足以回答问题，请根据你的医学知识进行补充，但要说明这是基于通用知识。
                请用温和、专业的语气回答。
                使用 Markdown 格式输出。
                """;
        
        String userPrompt;
        if (!context.isEmpty()) {
            userPrompt = "上下文信息：\n" + context + "\n\n用户问题：" + userMessage;
        } else {
            userPrompt = userMessage; // 如果没有上下文，直接发送用户问题
        }

        // 3. 流式调用
        String legacyPrompt = systemPrompt + "\n\n" + userPrompt;

        Flux<String> responseStream = chatClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .stream()
                .content();
        responseStream = responseStream.onErrorResume(WebClientResponseException.NotFound.class,
                ex -> ollamaLegacyClient.generateStream(legacyPrompt, null, null));

        // 4. 追加 RAG 引用源信息 (用于前端展示)
        if (!docs.isEmpty()) {
            StringBuilder refBuilder = new StringBuilder("\n\n---\n> 📚 **知识库引用**:\n");
            for (int i = 0; i < docs.size(); i++) {
                refBuilder.append(String.format("> %d. %s\n", i + 1, docs.get(i).get("title")));
            }
            return responseStream.concatWith(Flux.just(refBuilder.toString()));
        } else if (context.isEmpty()) {
             // 如果 RAG 失败或无结果，可以追加一个提示（可选）
             // return responseStream.concatWith(Flux.just("\n\n*(当前回答基于通用模型知识，未引用本地知识库)*"));
        }

        return responseStream;
    }

    /**

     * 执行业务操作

     * @param userMessage 业务参数

     * @param base64Image 业务参数

     * @param userId 家庭成员唯一标识

     * @return 业务返回结果

     */

    public Flux<String> chatImageStream(String userMessage, String base64Image, Long userId) {
        String systemPrompt = """
                你是一个专业的中医健康助手。用户上传了一张图片（可能是舌象、食物或药品）。
                请仔细分析图片内容，并结合用户的问题进行回答。
                如果是舌象，请尝试分析舌质、舌苔，并给出体质判断建议。
                如果是食物，请分析其营养成分或中医属性（寒热温凉）。
                请用温和、专业的语气回答。
                使用 Markdown 格式输出。
                """;
        
        // 解码 Base64
        byte[] imageBytes;
        try {
             // 移除可能存在的 Data URI scheme 前缀 (e.g. "data:image/png;base64,")
            String cleanBase64 = base64Image;
            if (base64Image.contains(",")) {
                cleanBase64 = base64Image.split(",")[1];
            }
            imageBytes = Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 image", e);
            return Flux.just("图片上传失败，请重试。");
        }

        // 构建带图片的消息
        var userMsg = new UserMessage(userMessage, List.of(
                new Media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(imageBytes))
        ));

        Flux<String> responseStream = chatClientBuilder.build()
                .prompt()
                .options(OllamaOptions.builder()
                        .withModel(visionModel)
                        .withTemperature(visionTemperature)
                        .build())
                .system(systemPrompt)
                .messages(userMsg)
                .stream()
                .content();
        responseStream = responseStream.onErrorResume(WebClientResponseException.NotFound.class,
                ex -> Flux.just("当前本地 Ollama 版本不支持图像对话，请升级 Ollama 后重试。"));

        // 追加模型信息
        return responseStream.concatWith(Flux.just("\n\n---\n> 🖼️ **Vision Model**: `" + visionModel + "`"));
    }
}
