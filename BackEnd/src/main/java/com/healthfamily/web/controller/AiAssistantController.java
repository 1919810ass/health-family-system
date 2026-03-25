package com.healthfamily.web.controller;

import com.healthfamily.service.AiAssistantService;
import com.healthfamily.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
/**
 * AIAssistant控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@Tag(name = "AI 助手接口", description = "提供流式对话能力")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @Operation(summary = "流式对话", description = "基于 RAG 的流式问答接口")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    /**
     * 执行业务操作
     * @param request 请求体数据
     * @param userDetails 业务参数
     * @return 业务返回结果
     */
    public Flux<String> chatStream(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String message = request.get("message");
        // 这里假设 UserDetails 中可以获取 ID，或者通过 Service 层根据 username 查 ID
        // 为简化演示，这里暂不强依赖 ID 逻辑，实际业务中应传入真实 User ID
        Long userId = 1L; 
        
        return aiAssistantService.chatStream(message, userId, Boolean.parseBoolean(request.get("ragEnabled")));
    }

    @Operation(summary = "多模态流式对话", description = "支持图片上传的流式问答接口")
    @PostMapping(value = "/image-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    /**
     * 执行业务操作
     * @param request 请求体数据
     * @param userDetails 业务参数
     * @return 业务返回结果
     */
    public Flux<String> chatImageStream(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String message = request.get("message");
        String image = request.get("image"); // Base64 string
        Long userId = 1L; 
        
        return aiAssistantService.chatImageStream(message, image, userId);
    }

    @Operation(summary = "非流式对话", description = "一次性返回完整回答，用于测试")
    @PostMapping("/blocking")
    public Map<String, String> chatBlocking(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String message = request.get("message");
        Long userId = 1L;
        boolean ragEnabled = Boolean.parseBoolean(request.get("ragEnabled"));
        
        String reply = aiAssistantService.chatBlocking(message, userId, ragEnabled);
        
        return Map.of("reply", reply);
    }
}
