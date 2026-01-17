package com.healthfamily.service.impl;

import com.healthfamily.domain.constant.AuditResult;
import com.healthfamily.domain.constant.SensitivityLevel;
import com.healthfamily.domain.entity.AuditLog;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.AuditLogRepository;
import com.healthfamily.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLog(User user, String action, String resource, SensitivityLevel level, AuditResult result, String ip, String userAgent, String extraJson) {
        try {
            AuditLog log = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .resource(resource)
                    .sensitivityLevel(level)
                    .result(result)
                    .ip(ip)
                    .userAgent(userAgent)
                    .extraJson(extraJson)
                    .createdAt(LocalDateTime.now()) // Explicitly set createdAt
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to record audit log", e);
        }
    }

    @Override
    public List<AuditLog> getLogs(LocalDateTime start, LocalDateTime end, AuditResult result) {
        if (result != null) {
            return auditLogRepository.findByResultAndCreatedAtBetweenOrderByCreatedAtDesc(result, start, end);
        }
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    @Override
    public List<AuditLog> getUserLogs(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Page<AuditLog> searchLogs(Long userId, String action, String resource, AuditResult result, SensitivityLevel sensitivity, LocalDateTime startTime, LocalDateTime endTime, String keyword, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (StringUtils.hasText(resource)) {
                predicates.add(cb.like(root.get("resource"), "%" + resource + "%"));
            }
            if (result != null) {
                predicates.add(cb.equal(root.get("result"), result));
            }
            if (sensitivity != null) {
                predicates.add(cb.equal(root.get("sensitivityLevel"), sensitivity));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }
            if (StringUtils.hasText(keyword)) {
                Predicate p1 = cb.like(root.get("action"), "%" + keyword + "%");
                Predicate p2 = cb.like(root.get("resource"), "%" + keyword + "%");
                Predicate p3 = cb.like(root.get("extraJson"), "%" + keyword + "%");
                predicates.add(cb.or(p1, p2, p3));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable);
    }
}
