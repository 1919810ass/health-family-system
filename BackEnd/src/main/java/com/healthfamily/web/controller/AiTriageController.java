package com.healthfamily.web.controller;

import com.healthfamily.web.dto.Result;
import com.healthfamily.service.AiTriageService;
import com.healthfamily.web.dto.TriageChatRequest;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/consultation/triage")
/**
 * AITriage控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequiredArgsConstructor
public class AiTriageController {

    private final AiTriageService triageService;

    // 发送消息给 AI 导诊员
    @PostMapping("/chat")
    /**
     * 执行业务操作
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<String> chat(@RequestBody TriageChatRequest request) {
        String reply = triageService.chat(request.getSessionId(), request.getUserMessage());
        return Result.success(reply);
    }

    // 结束问诊并生成摘要
    @PostMapping("/finish")
    /**
     * 执行业务操作
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<String> finish(@RequestBody TriageChatRequest request) {
        String summary = triageService.generateSummary(request.getSessionId());
        return Result.success(summary);
    }
}
