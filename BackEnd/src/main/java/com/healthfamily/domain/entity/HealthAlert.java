package com.healthfamily.domain.entity;

import com.healthfamily.domain.constant.AlertSeverity;
import com.healthfamily.domain.constant.AlertStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "health_alerts")
public class HealthAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "metric", length = 32, nullable = false)
    private String metric;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "threshold")
    private Double threshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 16, nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private AlertStatus status;

    @Column(name = "channel", length = 16)
    private String channel;

    @Column(name = "escalation_level")
    private Integer escalationLevel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;
    
    @Column(name = "handled_by")
    private Long handledBy;  // 处理人ID（医生）
    
    @Column(name = "handling_note", columnDefinition = "TEXT")
    private String handlingNote;  // 处理备注
    
    @Column(name = "notification_sent")
    private String notificationSent;  // 通知发送状态
    
    @Column(name = "notification_time")
    private LocalDateTime notificationTime;  // 通知发送时间
    
    @Column(name = "notification_content", columnDefinition = "TEXT")
    private String notificationContent;  // 通知内容
}

