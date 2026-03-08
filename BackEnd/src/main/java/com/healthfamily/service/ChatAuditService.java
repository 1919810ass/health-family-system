package com.healthfamily.service;

import com.healthfamily.domain.entity.BadCase;
import com.healthfamily.domain.entity.ConsultationMessage;
import com.healthfamily.domain.entity.ConsultationSession;
import com.healthfamily.domain.repository.BadCaseRepository;
import com.healthfamily.domain.repository.ConsultationMessageRepository;
import com.healthfamily.domain.repository.ConsultationSessionRepository;
import com.healthfamily.web.dto.AuditMessageDto;
import com.healthfamily.web.dto.AuditSessionDto;
import com.healthfamily.web.dto.FlagRiskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAuditService {

    private final ConsultationSessionRepository sessionRepository;
    private final ConsultationMessageRepository messageRepository;
    private final BadCaseRepository badCaseRepository;

    @Transactional(readOnly = true)
    public Page<AuditSessionDto> getSessions(int page, int size, Boolean onlyRisky) {
        
        Specification<ConsultationSession> spec = (root, query, cb) -> {
            if (onlyRisky != null && onlyRisky) {
                // Subquery to check for existence in BadCase
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<BadCase> subRoot = subquery.from(BadCase.class);
                subquery.select(subRoot.get("sessionId"));
                subquery.where(cb.equal(subRoot.get("sessionId"), root.get("id")));
                return cb.exists(subquery);
            }
            return cb.conjunction(); // No filter
        };

        Page<ConsultationSession> sessionPage = sessionRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "lastMessageAt")));

        return sessionPage.map(s -> new AuditSessionDto(
                s.getId(),
                s.getTitle(),
                s.getPatient() != null ? (s.getPatient().getNickname() != null ? s.getPatient().getNickname() : s.getPatient().getPhone()) : "Unknown",
                s.getDoctor() != null ? (s.getDoctor().getNickname() != null ? s.getDoctor().getNickname() : s.getDoctor().getPhone()) : "AI Assistant",
                s.getLastMessageAt(),
                s.getMessageCount(),
                s.getIsAiTriaged(),
                s.getTriageSummary()
        ));
    }

    @Transactional(readOnly = true)
    public List<AuditMessageDto> getSessionMessages(Long sessionId) {
        log.info("[Audit] Loading messages for session ID: {}", sessionId);
        try {
            List<ConsultationMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            log.info("[Audit] Found {} messages in database for session ID: {}", messages.size(), sessionId);

            if (messages.isEmpty()) {
                log.warn("[Audit] No messages found for session ID: {}. Returning empty list.", sessionId);
                return List.of();
            }

            Set<Long> badMessageIds = badCaseRepository.findBySessionId(sessionId).stream()
                    .map(BadCase::getMessageId)
                    .collect(Collectors.toSet());
            log.info("[Audit] Found {} bad cases for session ID: {}", badMessageIds.size(), sessionId);

            List<AuditMessageDto> dtos = messages.stream().map(m -> new AuditMessageDto(
                    m.getId(),
                    m.getSenderType(),
                    m.getSender() != null ? (m.getSender().getNickname() != null ? m.getSender().getNickname() : m.getSender().getPhone()) : "Unknown",
                    m.getContent(),
                    m.getMessageType(),
                    m.getCreatedAt(),
                    badMessageIds.contains(m.getId())
            )).collect(Collectors.toList());
            
            log.info("[Audit] Successfully mapped and returning {} DTOs for session ID: {}", dtos.size(), sessionId);
            return dtos;
        } catch (Exception e) {
            log.error("[Audit] CRITICAL: An unexpected error occurred while fetching messages for session ID: {}", sessionId, e);
            // In case of error, return an empty list to prevent frontend from crashing.
            return List.of();
        }
    }

    @Transactional
    public void flagRisk(FlagRiskRequest request, Long auditorId) {
        BadCase badCase = BadCase.builder()
                .sessionId(request.sessionId())
                .messageId(request.messageId())
                .question(request.question())
                .aiAnswer(request.aiAnswer())
                .humanCorrection(request.humanCorrection())
                .riskType(request.riskType())
                .auditorId(auditorId)
                .build();
        badCaseRepository.save(badCase);
    }
}
