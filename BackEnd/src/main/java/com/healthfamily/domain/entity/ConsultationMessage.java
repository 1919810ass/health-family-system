package com.healthfamily.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 咨询消息实体
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "consultation_messages")
public class ConsultationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属会话
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ConsultationSession session;

    /**
     * 发送者用户
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * 发送者类型：DOCTOR（医生）、FAMILY_MEMBER（家庭成员）、MEMBER（患者本人）
     */
    @Column(name = "sender_type", nullable = false, length = 16)
    private String senderType;

    /**
     * 消息内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 是否已读（医生端）
     */
    @Column(name = "read_by_doctor")
    @Builder.Default
    private Boolean readByDoctor = false;

    /**
     * 是否已读（患者端）
     */
    @Column(name = "read_by_patient")
    @Builder.Default
    private Boolean readByPatient = false;

    /**
     * 消息类型：TEXT（文本）、TEMPLATE（模板回复）
     */
    @Column(name = "message_type", length = 16)
    @Builder.Default
    private String messageType = "TEXT";

    /**
     * 模板ID（如果是模板回复）
     */
    @Column(name = "template_id", length = 64)
    private String templateId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultationMessage)) return false;
        ConsultationMessage that = (ConsultationMessage) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

