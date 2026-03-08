package com.healthfamily.web.controller;

import com.healthfamily.service.AiSuggestionService;
import com.healthfamily.web.model.response.AiSuggestionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员端AI建议模块的Controller
 */
@RestController
@RequestMapping("/api/admin/ai-suggestions")
public class AdminAiSuggestionController {

    @Autowired
    private AiSuggestionService aiSuggestionService;

    /**
     * 获取所有AI健康建议
     * 仅限管理员访问
     * @return AI建议列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AiSuggestionResponse>> getAiSuggestions() {
        List<AiSuggestionResponse> suggestions = aiSuggestionService.generateSuggestions();
        return ResponseEntity.ok(suggestions);
    }
}
