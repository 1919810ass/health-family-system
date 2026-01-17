package com.healthfamily.web.controller;

import com.healthfamily.ai.TcmAssessmentAiService;
import com.healthfamily.web.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-assessment")
@RequiredArgsConstructor
@Tag(name = "AI中医体质测评", description = "AI驱动的动态体质测评相关接口")
public class AiAssessmentController {

    private final com.healthfamily.service.AssessmentService assessmentService;

    /**
     * 开始AI驱动的体质测评对话
     */
    @PostMapping("/start")
    @Operation(summary = "开始AI测评", description = "启动AI驱动的动态体质测评对话")
    public Result<Map<String, Object>> startAssessment(@RequestHeader("X-User-Id") Long userId) {
        Map<String, Object> response = assessmentService.startAiAssessment(userId);
        return Result.success(response);
    }

    /**
     * 处理用户回答并获取下一个问题
     */
    @PostMapping("/answer")
    @Operation(summary = "处理用户回答", description = "提交用户回答并获取下一个问题")
    public Result<Map<String, Object>> processAnswer(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String sessionId,
            @RequestBody Map<String, String> request) {
        
        String userAnswer = request.get("answer");
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return Result.error(400, "回答内容不能为空");
        }

        Map<String, Object> response = assessmentService.processAiAnswer(userId, sessionId, userAnswer);
        return Result.success(response);
    }
    
    @PostMapping("/generate-result")
    @Operation(summary = "生成最终评估结果", description = "基于对话内容生成最终体质评估结果")
    public Result<Map<String, Object>> generateFinalResult(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String sessionId,
            @RequestBody Map<String, String> request) {
        
        String finalAnswers = request.get("finalAnswers");
        if (finalAnswers == null || finalAnswers.trim().isEmpty()) {
            return Result.error(400, "最终回答内容不能为空");
        }
        
        Map<String, Object> result = assessmentService.generateFinalAiAssessment(userId, sessionId, finalAnswers);
        return Result.success(result);
    }

    /**
     * 获取最终评估结果
     */
    @GetMapping("/result/{assessmentId}")
    @Operation(summary = "获取评估结果", description = "获取指定评估ID的结果")
    public Result<Object> getResult(@RequestHeader("X-User-Id") Long userId, 
                                   @PathVariable Long assessmentId) {
        var response = assessmentService.getAssessment(userId, assessmentId);
        return Result.success(response);
    }
}