package com.healthfamily.web.controller;

import com.healthfamily.domain.entity.BadCase;
import com.healthfamily.domain.entity.ConsultationMessage;
import com.healthfamily.domain.entity.ConsultationSession;
import com.healthfamily.domain.repository.BadCaseRepository;
import com.healthfamily.domain.repository.ConsultationMessageRepository;
import com.healthfamily.domain.repository.ConsultationSessionRepository;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.web.dto.AuditMessageDto;
import com.healthfamily.web.dto.AuditSessionDto;
import com.healthfamily.web.dto.FlagRiskRequest;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/audit/chat")
@RequiredArgsConstructor
public class AdminChatAuditController {

    private final ConsultationSessionRepository sessionRepository;
    private final ConsultationMessageRepository messageRepository;
    private final BadCaseRepository badCaseRepository;

    @GetMapping("/sessions")
    public Result<Page<AuditSessionDto>> getSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean onlyRisky) {
        
        // In a real implementation, we would have complex filtering here
        // For now, simple pagination by last message time
        Page<ConsultationSession> sessionPage = sessionRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "lastMessageAt")));

        Page<AuditSessionDto> dtoPage = sessionPage.map(s -> new AuditSessionDto(
                s.getId(),
                s.getTitle(),
                s.getPatient() != null ? (s.getPatient().getNickname() != null ? s.getPatient().getNickname() : s.getPatient().getPhone()) : "Unknown",
                s.getDoctor() != null ? (s.getDoctor().getNickname() != null ? s.getDoctor().getNickname() : s.getDoctor().getPhone()) : "AI Assistant",
                s.getLastMessageAt(),
                0, // TODO: count messages efficiently if needed
                s.getIsAiTriaged(), // Using isAiTriaged as a proxy for potential AI interaction to review
                s.getTriageSummary()
        ));

        return Result.success(dtoPage);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<AuditMessageDto>> getSessionMessages(@PathVariable Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        List<ConsultationMessage> messages = messageRepository.findBySessionOrderByCreatedAtAsc(session);
        List<BadCase> badCases = badCaseRepository.findBySessionId(sessionId);
        List<Long> badMessageIds = badCases.stream().map(BadCase::getMessageId).collect(Collectors.toList());

        List<AuditMessageDto> dtos = messages.stream().map(m -> new AuditMessageDto(
                m.getId(),
                m.getSenderType(),
                m.getSender() != null ? (m.getSender().getNickname() != null ? m.getSender().getNickname() : m.getSender().getPhone()) : "Unknown",
                m.getContent(),
                m.getMessageType(),
                m.getCreatedAt(),
                badMessageIds.contains(m.getId())
        )).collect(Collectors.toList());

        return Result.success(dtos);
    }

    @PostMapping("/flag-risk")
    public Result<String> flagRisk(@RequestBody FlagRiskRequest request, @AuthenticationPrincipal UserPrincipal auditor) {
        BadCase badCase = BadCase.builder()
                .sessionId(request.sessionId())
                .messageId(request.messageId())
                .question(request.question())
                .aiAnswer(request.aiAnswer())
                .humanCorrection(request.humanCorrection())
                .riskType(request.riskType())
                .auditorId(auditor.getUserId())
                .build();
        
        badCaseRepository.save(badCase);
        return Result.success("Risk flagged successfully");
    }
}
