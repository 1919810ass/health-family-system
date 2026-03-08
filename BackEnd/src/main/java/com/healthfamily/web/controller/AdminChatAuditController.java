package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.ChatAuditService;
import com.healthfamily.web.dto.AuditMessageDto;
import com.healthfamily.web.dto.AuditSessionDto;
import com.healthfamily.web.dto.FlagRiskRequest;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit/chat")
@RequiredArgsConstructor
public class AdminChatAuditController {

    private final ChatAuditService chatAuditService;

    @GetMapping("/sessions")
    public Result<Page<AuditSessionDto>> getSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean onlyRisky) {
        
        Page<AuditSessionDto> dtoPage = chatAuditService.getSessions(page, size, onlyRisky);
        return Result.success(dtoPage);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<AuditMessageDto>> getSessionMessages(@PathVariable Long sessionId) {
        List<AuditMessageDto> dtos = chatAuditService.getSessionMessages(sessionId);
        return Result.success(dtos);
    }

    @PostMapping("/flag-risk")
    public Result<String> flagRisk(@RequestBody FlagRiskRequest request, @AuthenticationPrincipal UserPrincipal auditor) {
        chatAuditService.flagRisk(request, auditor.getUserId());
        return Result.success("Risk flagged successfully");
    }
}
