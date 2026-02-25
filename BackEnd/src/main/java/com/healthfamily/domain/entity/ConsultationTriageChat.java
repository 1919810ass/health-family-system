package com.healthfamily.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * AI预问诊对话详情
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "consultation_triage_chat", indexes = {
        @Index(name = "idx_session", columnList = "session_id")
/**
 * 问诊TriageChat实体类
 * <p>
 * 用于持久化领域数据，通常与数据库表一一对应，承载业务状态与属性。
 * </p>
 */
})
public class ConsultationTriageChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /**
     * 发送者: AI, USER
     */
    @Column(name = "sender_role", nullable = false, length = 20)
    private String senderRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "gmt_create", updatable = false)
    private LocalDateTime gmtCreate;
}
