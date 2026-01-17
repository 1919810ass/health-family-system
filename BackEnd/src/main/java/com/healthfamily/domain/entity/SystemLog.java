package com.healthfamily.domain.entity;

import com.healthfamily.domain.constant.SystemLogType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SystemLogType type;

    @Column(name = "level", length = 16)
    private String level;

    @Column(name = "module", length = 64)
    private String module;

    @Column(name = "action", length = 128)
    private String action;

    @Column(name = "detail", columnDefinition = "text")
    private String detail;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
