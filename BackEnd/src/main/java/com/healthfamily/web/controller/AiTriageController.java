package com.healthfamily.web.controller;

import com.healthfamily.web.dto.Result;
import com.healthfamily.service.AiTriageService;
import com.healthfamily.web.dto.TriageChatRequest;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/consultation/triage")
@RequiredArgsConstructor
public class AiTriageController {

    private final AiTriageService triageService;

    // 发送消息给 AI 导诊员
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody TriageChatRequest request) {
        String reply = triageService.chat(request.getSessionId(), request.getUserMessage());
        return Result.success(reply);
    }

    // 结束问诊并生成摘要
    @PostMapping("/finish")
    public Result<String> finish(@RequestBody TriageChatRequest request) {
        String summary = triageService.generateSummary(request.getSessionId());
        return Result.success(summary);
    }
}
