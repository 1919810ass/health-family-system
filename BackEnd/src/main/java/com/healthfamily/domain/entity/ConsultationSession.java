package com.healthfamily.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 在线咨询会话实体
 * 按家庭成员维度建立会话
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "consultation_sessions")
public class ConsultationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 患者用户（家庭成员）
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_user_id", nullable = false)
    private User patient;

    /**
     * 家庭
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    /**
     * 医生（可选，如果为空则表示是家庭成员之间的咨询）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    /**
     * 会话标题（自动生成或手动设置）
     */
    @Column(length = 255)
    private String title;

    /**
     * 会话状态：ACTIVE（进行中）、CLOSED（已关闭）
     */
    @Column(nullable = false, length = 16)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 最后一条消息的时间（用于排序）
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * 未读消息数（医生端）
     */
    @Column(name = "unread_count_doctor")
    @Builder.Default
    private Integer unreadCountDoctor = 0;

    /**
     * 未读消息数（患者端）
     */
    @Column(name = "unread_count_patient")
    @Builder.Default
    private Integer unreadCountPatient = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultationSession)) return false;
        ConsultationSession that = (ConsultationSession) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

